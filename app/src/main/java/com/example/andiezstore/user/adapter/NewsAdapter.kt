package com.example.andiezstore.user.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Import Glide
import com.example.andiezstore.R
import com.example.andiezstore.user.model.News

class NewsAdapter(
    private val newsList: MutableList<News>,
    private val onItemClick: (News) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {


    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvNewsTitle)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvNewsAuthor)
        val imgNewsThumbnail: ImageView = itemView.findViewById(R.id.imgNewsThumbnail)
        val tvDate: TextView = itemView.findViewById(R.id.tvNewsDate)


        fun bind(news: News, context: android.content.Context) {
            tvTitle.text = news.title
            tvAuthor.text = news.author
            tvDate.text = news.date

            Glide.with(context)
                .load(news.image)
                .placeholder(R.drawable.view3)
                .error(R.drawable.view3)
                .into(imgNewsThumbnail)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news_summary_home, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val news = newsList[position]
        holder.bind(news, holder.itemView.context) // Pass context to bind function for Glide
        holder.itemView.setOnClickListener {
            onItemClick(news)
        }
    }

    override fun getItemCount(): Int {
        return newsList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateNewsList(newNewsList: List<News>) {
        newsList.clear() // Clear existing data
        newsList.addAll(newNewsList) // Add all new data
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }
}