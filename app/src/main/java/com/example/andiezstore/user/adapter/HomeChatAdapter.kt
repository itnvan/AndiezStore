package com.example.andiezstore.user.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy.ALL
import com.bumptech.glide.request.RequestOptions
import com.example.andiezstore.R
import com.example.andiezstore.user.model.HomeChat
import com.example.andiezstore.user.model.Message // Import Message model
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@Suppress("SENSELESS_COMPARISON")
open class HomeChatAdapter(
    private val userList: ArrayList<HomeChat>,
    private val context: Context,
    private val onItemClick: (HomeChat) -> Unit // Callback for item clicks
) : RecyclerView.Adapter<HomeChatAdapter.HomeChatViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    class HomeChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgUser: ImageView = itemView.findViewById(R.id.imgUser)
        val tvUser: TextView = itemView.findViewById(R.id.tvUser)
        val tvPresentText: TextView = itemView.findViewById(R.id.tvPresentText)

        init {
            if (imgUser == null) {
                Log.e("HomeChatViewHolder", "imgUser is null! Check item_user_layout.xml for ID @+id/imgUser")
            }
            if (tvUser == null) {
                Log.e("HomeChatViewHolder", "tvUser is null! Check item_user_layout.xml for ID @+id/tvUser")
            }
            if (tvPresentText == null) {
                Log.e("HomeChatViewHolder", "tvPresentText is null! Check item_user_layout.xml for ID @+id/tvPresentText")
            }
        }

        fun bind(homeChat: HomeChat, context: Context, currentUserId: String?) {
            tvUser.text = homeChat.name

            val requestOptions = RequestOptions()
                .placeholder(R.drawable.img_user)
                .diskCacheStrategy(ALL)
                .circleCrop()
                .error(R.drawable.img_user)

            Glide.with(context)
                .setDefaultRequestOptions(requestOptions)
                .load(homeChat.profileImageUrl)
                .apply(requestOptions)
                .into(imgUser)

            // Lấy tin nhắn gần nhất và hiển thị vào tvPresentText
            if (currentUserId != null && homeChat.userId != null) {
                val otherUserId = homeChat.userId

                val databaseRef = FirebaseDatabase.getInstance().getReference("messages")

                // Tin nhắn từ current user gửi đến other user
                val path1 = databaseRef.child(currentUserId).child(otherUserId)
                // Tin nhắn từ other user gửi đến current user
                val path2 = databaseRef.child(otherUserId).child(currentUserId)

                var latestMessage: Message? = null

                // Listener cho Path 1
                path1.orderByChild("timestamp").limitToLast(1)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            var messageFromPath1: Message? = null
                            if (snapshot.exists()) {
                                for (childSnapshot in snapshot.children) {
                                    messageFromPath1 = childSnapshot.getValue(Message::class.java)
                                }
                            }

                            // Listener cho Path 2 (đặt trong callback của Path 1 để đảm bảo tuần tự)
                            path2.orderByChild("timestamp").limitToLast(1)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot2: DataSnapshot) {
                                        var messageFromPath2: Message? = null
                                        if (snapshot2.exists()) {
                                            for (childSnapshot2 in snapshot2.children) {
                                                messageFromPath2 = childSnapshot2.getValue(Message::class.java)
                                            }
                                        }

                                        // So sánh timestamp để tìm tin nhắn mới nhất
                                        if (messageFromPath1 != null && messageFromPath2 != null) {
                                            latestMessage = if (messageFromPath1.timestamp!! > messageFromPath2.timestamp!!) {
                                                messageFromPath1
                                            } else {
                                                messageFromPath2
                                            }
                                        } else if (messageFromPath1 != null) {
                                            latestMessage = messageFromPath1
                                        } else if (messageFromPath2 != null) {
                                            latestMessage = messageFromPath2
                                        }

                                        // Cập nhật tvPresentText
                                        if (latestMessage != null) {
                                            tvPresentText.text = latestMessage!!.text
                                        } else {
                                            tvPresentText.text = "No messages yet."
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e("HomeChatAdapter", "Failed to load messages from path2: ${error.message}")
                                        tvPresentText.text = "Error loading message."
                                    }
                                })
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("HomeChatAdapter", "Failed to load messages from path1: ${error.message}")
                            tvPresentText.text = "Error loading message."
                        }
                    })
            } else {
                tvPresentText.text = "Chat info missing."
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_layout, parent, false)
        return HomeChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeChatViewHolder, position: Int) {
        val currentUser = userList[position]
        holder.bind(currentUser, context, currentUserId)

        holder.itemView.setOnClickListener {
            onItemClick(currentUser)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}