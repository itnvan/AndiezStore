package com.example.andiezstore.user.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.andiezstore.databinding.FragmentHomeChatBinding
import com.example.andiezstore.user.adapter.HomeChatAdapter
import com.example.andiezstore.user.model.HomeChat
import com.example.andiezstore.user.model.Message // Import Message model
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.ArrayList
import java.util.concurrent.CountDownLatch // Dùng để đồng bộ hóa các tác vụ bất đồng bộ
import androidx.navigation.fragment.findNavController // Import for navigation

@Suppress("CAST_NEVER_SUCCEEDS")
class HomeChatFragment : Fragment() {
    private lateinit var binding: FragmentHomeChatBinding
    private lateinit var adapter: HomeChatAdapter
    private var allUserList = ArrayList<HomeChat>() // Danh sách đầy đủ tất cả người dùng
    private var displayedUserList = ArrayList<HomeChat>() // Danh sách người dùng đang hiển thị (đã lọc)
    private lateinit var auth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference // Tham chiếu đến gốc của Realtime Database
    private lateinit var storageRef: StorageReference // Tham chiếu đến Firebase Storage
    private var usersValueEventListener: ValueEventListener? = null // Listener cho Realtime Database

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()
        storageRef = FirebaseStorage.getInstance().reference

        adapter = HomeChatAdapter(displayedUserList, requireContext()) { clickedUser ->
            val bundle = Bundle().apply { clickedUser.userId?.let { putString("receiverId", it) } }
            findNavController().navigate(com.example.andiezstore.R.id.action_homeChatFragment_to_chatFragment, bundle) // Đã sửa ID action
        }

        binding.rcvChat.layoutManager = LinearLayoutManager(context)
        binding.rcvChat.adapter = adapter

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterUsers(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        fetchUsersAndImages()
    }

    private fun fetchUsersAndImages() {
        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid == null) {
            Toast.makeText(requireContext(), "User not logged in. Cannot fetch images.", Toast.LENGTH_LONG).show()
            Log.e("HomeChatFragment", "Current user UID is null. User is not authenticated.")
            return
        } else {
            Log.d("HomeChatFragment", "Current authenticated user UID: $currentUserUid")
        }

        usersValueEventListener?.let {
            mDbRef.child("Users").removeEventListener(it)
        }

        usersValueEventListener = object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                allUserList.clear()
                val usersToProcess = snapshot.children.filter { it.key != currentUserUid }
                val totalUsers = usersToProcess.size

                if (totalUsers == 0) {
                    displayedUserList.clear()
                    adapter.notifyDataSetChanged()
                    return
                }

                val latch = CountDownLatch(totalUsers) // Dùng để đồng bộ hóa

                for (userSnapshot in usersToProcess) {
                    val userId = userSnapshot.key
                    val userInfoSnapshot = userSnapshot.child("information")
                    val name = userInfoSnapshot.child("name").getValue(String::class.java)
                    val presentTextPlaceholder = "No messages yet"

                    if (name != null && userId != null) {
                        val imageRef = storageRef.child("users/$userId/profile_image.jpg")
                        imageRef.downloadUrl
                            .addOnSuccessListener { uri ->
                                val profileImageUrl = uri.toString()
                                allUserList.add(HomeChat(name = name, presentText = presentTextPlaceholder, userId = userId, profileImageUrl = profileImageUrl))
                                fetchLatestMessageForUser(currentUserUid, userId) { latestMsg ->
                                    val index = allUserList.indexOfFirst { it.userId == userId }
                                    if (index != -1) {
                                        allUserList[index] = allUserList[index].copy(presentText = latestMsg?.text ?: presentTextPlaceholder)
                                    }
                                    latch.countDown()
                                    if (latch.count == 0L) {
                                        filterUsers(binding.edtSearch.text.toString())
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.w("HomeChatFragment", "Failed to load image for user $userId: ${e.message}. Current user authenticated: ${auth.currentUser?.uid != null}", e)
                                allUserList.add(HomeChat(name = name, presentText = presentTextPlaceholder, userId = userId, profileImageUrl = null))
                                fetchLatestMessageForUser(currentUserUid, userId) { latestMsg ->
                                    val index = allUserList.indexOfFirst { it.userId == userId }
                                    if (index != -1) {
                                        allUserList[index] = allUserList[index].copy(presentText = latestMsg?.text ?: presentTextPlaceholder)
                                    }
                                    latch.countDown()
                                    if (latch.count == 0L) {
                                        filterUsers(binding.edtSearch.text.toString())
                                    }
                                }
                            }
                    } else {
                        Log.w("HomeChatFragment", "Skipping user due to null name or userId: ${userSnapshot.key}")
                        latch.countDown()
                        if (latch.count == 0L) {
                            filterUsers(binding.edtSearch.text.toString())
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeChatFragment", "Failed to fetch users from Realtime Database: ${error.message}", error.toException())
                Toast.makeText(requireContext(), "Failed to load users: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        mDbRef.child("Users").addValueEventListener(usersValueEventListener!!)
    }

    // Hàm để lấy tin nhắn cuối cùng cho một cặp người dùng
    private fun fetchLatestMessageForUser(currentUserUid: String, otherUserId: String, callback: (Message?) -> Unit) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("messages")

        // Xây dựng chatRoomId: Sắp xếp ID của hai người dùng và nối chúng lại
        val chatRoomId = if (currentUserUid < otherUserId) {
            "${currentUserUid}_${otherUserId}"
        } else {
            "${otherUserId}_${currentUserUid}"
        }

        // Lắng nghe tin nhắn cuối cùng từ cả hai phía trong phòng chat này
        // Dựa trên cấu trúc bạn cung cấp: messages/{chat_room_id}/{sender_or_receiver_id}/{message_key}
        val messagesRef = databaseRef.child(chatRoomId)

        messagesRef.orderByChild("timestamp").limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var latestMessage: Message? = null
                    if (snapshot.exists()) {
                        for (userMessageSnapshot in snapshot.children) {
                            for (messageSnapshot in userMessageSnapshot.children) {
                                val message = messageSnapshot.getValue(Message::class.java)
                                if (message != null) {
                                    if (latestMessage == null || message.timestamp!! > latestMessage.timestamp!!) {
                                        latestMessage = message
                                    }
                                }
                            }
                        }
                    }
                    callback(latestMessage)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("HomeChatFragment", "Failed to load latest message for $otherUserId: ${error.message}")
                    callback(null)
                }
            })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterUsers(query: String) {
        displayedUserList.clear()
        if (query.isEmpty()) {
            displayedUserList.addAll(allUserList)
        } else {
            val lowerCaseQuery = query.lowercase()
            for (user in allUserList) {
                if (user.name?.lowercase()?.contains(lowerCaseQuery) == true) {
                    displayedUserList.add(user)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        usersValueEventListener?.let {
            mDbRef.child("Users").removeEventListener(it)
        }
        usersValueEventListener = null
    }
}
