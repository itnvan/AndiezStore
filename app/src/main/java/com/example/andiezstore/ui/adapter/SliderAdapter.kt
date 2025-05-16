package com.example.andiezstore.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.andiezstore.R

class SliderAdapter(private val imageList: List<Int>, private val viewPager2: ViewPager2) :
    RecyclerView.Adapter<SliderAdapter.SliderViewHolder>() {
    class SliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imgPhoto)
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SliderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return SliderViewHolder(view)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        holder.imageView.setImageResource(imageList[position])
        holder.imageView.scaleType = ImageView.ScaleType.FIT_CENTER // Thêm dòng này
    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}
