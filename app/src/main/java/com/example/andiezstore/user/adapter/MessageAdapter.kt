package com.example.andiezstore.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.andiezstore.R
import com.example.andiezstore.user.model.Message
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(private val messageList: MutableList<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val MESSAGE_SENT = 1
    private val MESSAGE_RECEIVED = 2
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        if (viewType == MESSAGE_SENT) {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_sent, parent, false)
            return SentMessageHolder(view)
        } else {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_received, parent, false)
            return ReceivedMessageHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]
        if (holder.itemViewType == MESSAGE_SENT) {
            (holder as SentMessageHolder).bind(message)
        } else {
            (holder as ReceivedMessageHolder).bind(message)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        return if (message.sender == auth.currentUser?.uid) {
            MESSAGE_SENT
        } else {
            MESSAGE_RECEIVED
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    // Các lớp ViewHolder giữ các tham chiếu đến các view trong mỗi item tin nhắn
    inner class SentMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.text_message_body)
        private val timeText: TextView = itemView.findViewById(R.id.text_message_time)

        fun bind(message: Message) {
            messageText.text = message.text
            // Định dạng thời gian hiển thị (ví dụ: "HH:mm")
            timeText.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp ?: 0))
        }
    }

    inner class ReceivedMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.text_message_body)
        private val timeText: TextView = itemView.findViewById(R.id.text_message_time)

        fun bind(message: Message) {
            messageText.text = message.text
            // Định dạng thời gian hiển thị (ví dụ: "HH:mm")
            timeText.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp ?: 0))
        }
    }
}
