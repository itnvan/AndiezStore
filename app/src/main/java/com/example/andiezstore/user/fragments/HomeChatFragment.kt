package com.example.andiezstore.user.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy.ALL
import com.bumptech.glide.request.RequestOptions
import com.example.andiezstore.R
import com.example.andiezstore.databinding.FragmentHomeChatBinding
import com.example.andiezstore.user.adapter.HomeChatAdapter
import com.example.andiezstore.user.model.HomeChat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

@Suppress("CAST_NEVER_SUCCEEDS")
class HomeChatFragment : Fragment() {
    private lateinit var binding: FragmentHomeChatBinding
    private lateinit var adapter: HomeChatAdapter
    private var userList = ArrayList<HomeChat>()
    private lateinit var auth: FirebaseAuth
    private var mDbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference()
    private lateinit var storageRef: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeChatBinding.inflate(layoutInflater)
        userList = ArrayList()
        adapter = HomeChatAdapter(userList)
        binding.rcvChat.layoutManager = LinearLayoutManager(context)
        binding.rcvChat.adapter = adapter
        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().reference

        fetchUsersAndImages()
        return binding.root
    }

    private fun fetchUsersAndImages() {
        mDbRef.child("Users").addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (postSnapshot in snapshot.children) {
                    val userId = postSnapshot.key
                    val userName =
                        postSnapshot.child("information").child("name").getValue(String::class.java)
                    if (userName != null && userId != null) { // Đảm bảo cả hai đều không null
                        val profileImageRef = storageRef.child("users/$userId/profile_image.jpg") //đường dẫn ảnh đại diện
                        profileImageRef.downloadUrl.addOnSuccessListener { uri ->
                            // Lấy URL thành công
                            val imageUrl = uri.toString()
                            val user = HomeChat(userName, imageUrl.toInt(), "", userId) // truyền vào model
                            userList.add(user)
                            adapter.notifyDataSetChanged()
                        }.addOnFailureListener { exception ->
                            // Xử lý lỗi nếu không lấy được URL
                            Log.e(
                                "HomeChatFragment",
                                "Failed to get download URL for user $userId: ${exception.message}"
                            )
                            val defaultImageUrl = R.drawable.img_user // Thay bằng URL mặc định của bạn
                            val user = HomeChat(userName, defaultImageUrl, "", userId)
                            userList.add(user)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeChatFragment", "Database error: ${error.message}")
                Toast.makeText(
                    requireContext(),
                    "Failed to load users: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
