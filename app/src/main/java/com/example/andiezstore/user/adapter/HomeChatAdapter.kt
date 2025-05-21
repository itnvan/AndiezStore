package com.example.andiezstore.user.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.andiezstore.R
import com.example.andiezstore.user.model.HomeChat

class HomeChatAdapter(private val userList: ArrayList<HomeChat>) :
    RecyclerView.Adapter<HomeChatAdapter.HomeChatViewHolder>() {
    class HomeChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Khai báo các thành phần giao diện của item
        val imgUser = itemView.findViewById<ImageView>(R.id.imgUser)
        val tvUser = itemView.findViewById<TextView>(R.id.tvUser)
        val tvPresentText = itemView.findViewById<TextView>(R.id.tvPresentText)
        init { // Add an init block to log
            Log.d("HomeChatViewHolder", "tvUser: $tvUser")
            Log.d("HomeChatViewHolder", "imgUser: $imgUser")
            Log.d("HomeChatViewHolder", "tvPresentText: $tvPresentText")
            if (tvUser == null) {
                Log.e("HomeChatViewHolder", "tvUser is null! Check your item_list_user.xml")
            }}
        fun onBind(homeChat: HomeChat) {
            imgUser.setImageResource(homeChat.imgUser!!)
            tvUser.text = homeChat.name
            tvPresentText.text = homeChat.presentText
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HomeChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_user, parent, false)
        return HomeChatViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: HomeChatViewHolder,
        position: Int
    ) {
        holder.onBind(userList[position])
        // Gắn dữ liệu vào các thành phần giao diện của item

    }

    override fun getItemCount(): Int {
        return userList.size
    }
}
