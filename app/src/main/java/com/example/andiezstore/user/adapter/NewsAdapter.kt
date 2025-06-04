package com.example.andiezstore.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.andiezstore.R
import com.example.andiezstore.user.model.News

class NewsAdapter(
    private val newsList: MutableList<News>,
    private val onItemClick: (News) -> Unit // Callback for item clicks
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {
    private var _currentDisplayedNews: News? = null // Biến để lưu tin tức đang hiển thị
    fun getCurrentDisplayedNews(): News? {
        return _currentDisplayedNews
    }
    fun setCurrentDisplayedNews(news: News?) {
        _currentDisplayedNews = news
        notifyDataSetChanged()

    }

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        val imgNews: ImageView = itemView.findViewById(R.id.imgNews)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        fun bind(news: News) {
            tvTitle.text = news.title
            tvDate.text = news.date
            tvAuthor.text = news.author
            imgNews.setBackgroundResource(R.drawable.view3)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news_summary_home, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val news = newsList[position]
        holder.bind(news)
        holder.itemView.setOnClickListener {
            onItemClick(news)
        }
    }

    override fun getItemCount(): Int {
        return newsList.size
    }

}