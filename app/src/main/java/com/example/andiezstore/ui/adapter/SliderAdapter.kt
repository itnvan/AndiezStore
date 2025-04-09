package com.example.andiezstore.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.andiezstore.R
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

class SliderAdapter(private val imageList: ArrayList<Int>, private val viewPager2: ViewPager2) :
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

    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}
//    private var sliderItems: List<SliderModel>,
//    private val viewPager2: ViewPager2
//) : RecyclerView.Adapter<SliderAdapter.SliderViewHolder>() {
//    private lateinit var context: Context
////    private val runnable: Runnable
////    {
////        sliderItems = sliderItems
////        notifyDataSetChanged()
////    }
//
//    class SliderViewHolder(private val binding: SliderItemContainerBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//        fun setImage(sliderModel: SliderModel, context: Context) {
//            Glide.with(context)
////                .load(sliderItems.url)
////                .apply {
////                    RequestOptions().transform(CenterInside)
////                }
////                .into(binding.imageSlide)
//        }
//
//    }
//
//    override fun onCreateViewHolder(
//        parent: ViewGroup,
//        viewType: Int
//    ): SliderAdapter.SliderViewHolder {
//        context = parent.context
//        val binding =
//            SliderItemContainerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return SliderViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: SliderAdapter.SliderViewHolder, position: Int) {
//        holder.setImage(sliderItems[position], context)
////        if (position == sliderItems.lastIndex - 1) {
////            viewPager2.post(runnable)
////        }
//    }
//
//    override fun getItemCount(): Int = sliderItems.size
//}