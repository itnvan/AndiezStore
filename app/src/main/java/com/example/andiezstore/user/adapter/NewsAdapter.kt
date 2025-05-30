package com.example.andiezstore.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.andiezstore.R
import com.example.andiezstore.user.model.News

class NewsAdapter(
    private val newsList: MutableList<News>,
    private val onItemClick: (News) -> Unit // Callback for item clicks
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDecription)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        fun bind(news: News) {
            tvTitle.text = news.title
            tvDescription.text = news.decription
            tvDate.text = news.date
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.dialog_crud, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val news = newsList[position]
        holder.bind(news)
        holder.itemView.setOnClickListener {
            onItemClick(news) // Kích hoạt callback khi item được click
        }
    }

    override fun getItemCount(): Int {
        return newsList.size
    }
}