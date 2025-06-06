package com.example.andiezstore.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.andiezstore.R
import com.example.andiezstore.user.model.News // Assuming your News model is here

class NewsAdminAdapter(
    private var newsList: List<News>,
    private val onItemClick: (News) -> Unit // Callback for item clicks
) : RecyclerView.Adapter<NewsAdminAdapter.NewsViewHolder>() {

    // ViewHolder class for each item in the RecyclerView
    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.item_news_image)
        val titleTextView: TextView = itemView.findViewById(R.id.item_news_title)
        val descriptionTextView: TextView = itemView.findViewById(R.id.item_news_description)
        val authorTextView: TextView = itemView.findViewById(R.id.item_news_author)
        val dateTextView: TextView = itemView.findViewById(R.id.item_news_date)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(newsList[position]) // Invoke the click callback
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news_admin, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val news = newsList[position]
        holder.titleTextView.text = news.title
        holder.descriptionTextView.text = news.decription
        holder.authorTextView.text = "By: ${news.author}" // Prepend "By: " for clarity
        holder.dateTextView.text = news.date

        // Load image using Glide
        Glide.with(holder.itemView.context)
            .load(news.image)
            .placeholder(R.drawable.view3) // Your placeholder image
            .error(R.drawable.view3)       // Your error image
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = newsList.size

    // Function to update the data in the adapter
    fun updateNewsList(newNewsList: List<News>) {
        newsList = newNewsList
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }
}