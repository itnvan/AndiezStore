package com.example.andiezstore.user.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.andiezstore.R
import com.example.andiezstore.user.adapter.MessageAdapter
import com.example.andiezstore.user.model.Message
import java.util.ArrayList

class ChatFragment : Fragment() {

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: MutableList<Message>
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var currentUserId: String? = null
    private var receiverId: String? = null  // ID của người nhận
    private var valueEventListener: ValueEventListener? = null // Giữ tham chiếu đến listener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.chat_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khởi tạo các view
        chatRecyclerView = view.findViewById(R.id.chat_recycler_view)
        messageEditText = view.findViewById(R.id.message_edit_text)
        sendButton = view.findViewById(R.id.send_button)

        // Khởi tạo Firebase
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("messages")

        // Khởi tạo danh sách tin nhắn và adapter
        messageList = ArrayList() // Use ArrayList for better compatibility
        messageAdapter = MessageAdapter(messageList)
        chatRecyclerView.layoutManager = LinearLayoutManager(context)
        chatRecyclerView.adapter = messageAdapter

        // Lấy receiverId từ Bundle (hoặc từ nơi khác nếu cần)
        receiverId = arguments?.getString("receiverId")
            ?: "defaultReceiverId" // Thay thế bằng logic lấy ID người nhận thực tế

        // Lấy currentUserId một cách an toàn
        currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Log.e("ChatFragment", "User not logged in")
            // Xử lý trường hợp người dùng chưa đăng nhập (ví dụ: hiển thị thông báo lỗi, chuyển hướng)
            return  // Dừng khởi tạo nếu không có người dùng
        }

        // Thiết lập listener cho nút gửi tin nhắn
        sendButton.setOnClickListener {
            sendMessage()
        }

        // Lắng nghe tin nhắn từ Firebase
        listenForMessages()
    }

    private fun sendMessage() {
        val text = messageEditText.text.toString().trim()
        if (text.isNotEmpty() && currentUserId != null && receiverId != null) {
            val message = Message(
                sender = currentUserId,
                text = text,
                timestamp = System.currentTimeMillis()
            )

            // Sử dụng combined path để lưu trữ tin nhắn
            val messageId = databaseReference.push().key // Lấy key duy nhất
            if (messageId != null) {
                // Lưu tin nhắn vào cả hai nhánh: người gửi và người nhận
                databaseReference
                    .child(currentUserId!!)
                    .child(receiverId!!)
                    .child(messageId)
                    .setValue(message)
                    .addOnSuccessListener {
                        messageEditText.text.clear() // Xóa nội dung tin nhắn sau khi gửi thành công
                        Log.d("ChatFragment", "Message sent successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ChatFragment", "Failed to send message: ${e.message}")
                        // Xử lý lỗi nếu cần
                    }
                databaseReference
                    .child(receiverId!!)
                    .child(currentUserId!!)
                    .child(messageId)
                    .setValue(message)
                    .addOnSuccessListener {
                        Log.d("ChatFragment", "Message sent successfully to receiver's inbox")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ChatFragment", "Failed to send message to receiver's inbox: ${e.message}")
                    }
            }
        }
    }

    private fun listenForMessages() {
        if (currentUserId == null || receiverId == null) return

        // Remove old listener if exists
        valueEventListener?.let {
            databaseReference.child(currentUserId!!).child(receiverId!!).removeEventListener(it)
        }

        // Chỉ lắng nghe tin nhắn giữa người dùng hiện tại và người nhận
        valueEventListener = object : ValueEventListener { // Store the listener in the variable
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempMessageList = ArrayList<Message>() // Use a temporary list
                for (childSnapshot in snapshot.children) {
                    val message = childSnapshot.getValue(Message::class.java)
                    message?.let { tempMessageList.add(it) }
                }

                // Sort messages by timestamp
                tempMessageList.sortBy { it.timestamp }

                messageList.clear()         // Clear the existing list
                messageList.addAll(tempMessageList) // Add the sorted messages
                messageAdapter.notifyDataSetChanged()
                chatRecyclerView.scrollToPosition(messageList.size - 1) // Cuộn xuống cuối để hiển thị tin nhắn mới nhất
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatFragment", "Error listening for messages: ${error.message}")
                // Xử lý lỗi nếu cần
            }
        }

        databaseReference.child(currentUserId!!).child(receiverId!!)
            .orderByChild("timestamp") // Sắp xếp theo thời gian
            .addValueEventListener(valueEventListener!!) // Use the stored listener here
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Hủy bỏ listener nếu cần thiết.
        valueEventListener?.let {
            if (currentUserId != null && receiverId != null) { // Check for nulls
                databaseReference.child(currentUserId!!).child(receiverId!!).removeEventListener(it)
            }
        }
        valueEventListener = null // Set it to null to avoid double removal.
    }
}
