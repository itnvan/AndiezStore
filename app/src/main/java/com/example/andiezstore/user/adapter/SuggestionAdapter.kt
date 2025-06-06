package com.example.andiezstore.user.fragments // Adjust package as needed

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.andiezstore.R // Make sure R points to your project's R file
import com.example.andiezstore.user.model.News // Assuming your News data class is here

// Define a click listener interface for handling item clicks
class SuggestionAdapter(private val onItemClick: (News) -> Unit) :
    RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder>() {

    private var suggestions: List<News> = emptyList()

    // Update the data in the adapter
    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newSuggestions: List<News>) {
        suggestions = newSuggestions
        notifyDataSetChanged() // Notifies the RecyclerView that the data has changed
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news_summary_home, parent, false) // Inflate your suggestion item layout
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val news = suggestions[position]
        holder.bind(news)
        holder.itemView.setOnClickListener { onItemClick(news) } // Handle item click
    }

    override fun getItemCount(): Int = suggestions.size

    inner class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgCover: ImageView = itemView.findViewById(R.id.imgNewsThumbnail)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvNewsTitle)
        private val tvAuthor: TextView = itemView.findViewById(R.id.tvNewsAuthor)

        @SuppressLint("SetTextI18n")
        fun bind(news: News) {
            tvTitle.text = news.title ?: "No title"
            tvAuthor.text = "${news.author ?: "Unknown Author"} · ${news.date ?: "Unknown Date"}"

            news.image?.let { imageUrl ->
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.view3)
                    .error(R.drawable.view3)
                    .into(imgCover)
            } ?: run {
                imgCover.setImageResource(R.drawable.view3)
            }
        }
    }
}