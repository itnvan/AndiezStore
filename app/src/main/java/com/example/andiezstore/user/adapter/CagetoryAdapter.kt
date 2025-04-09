package com.example.andiezstore.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.andiezstore.R
import com.example.andiezstore.ui.model.CagetoryModel

class CagetoryAdapter(val listCagetory: MutableList<CagetoryModel>) :
    RecyclerView.Adapter<CagetoryAdapter.CagetoryViewHolder>() {

    private var itemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(category: CagetoryModel)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    inner class CagetoryViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val cagetoryName: TextView = view.findViewById(R.id.tvCagetoryez)
        val cagetoryImg: ImageView = view.findViewById(R.id.imgCagetoryez)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val clickedCategory = listCagetory[position]
                    itemClickListener?.onItemClick(clickedCategory)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CagetoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cagetories, parent, false)
        return CagetoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CagetoryViewHolder, position: Int) {
        val category = listCagetory[position]
        holder.cagetoryName.text = category.name
        holder.cagetoryImg.setImageResource(category.img)
    }

    override fun getItemCount(): Int {
        return listCagetory.size
    }
}

