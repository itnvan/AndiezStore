package com.example.andiezstore.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.andiezstore.R
import com.example.andiezstore.ui.model.CagetoryModel

class CagetoryAdminAdapter(val listCagetory: MutableList<CagetoryModel>) :
    RecyclerView.Adapter<CagetoryAdminAdapter.CagetoryAdminViewHolder>() {

    private var itemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(category: CagetoryModel, view: View) // Thêm tham số view
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    inner class CagetoryAdminViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val cagetoryName: TextView = view.findViewById(R.id.tvCagetoryez)
        val cagetoryImg: ImageView = view.findViewById(R.id.imgCagetoryez)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val clickedCategory = listCagetory[position]
                    itemClickListener?.onItemClick(clickedCategory, itemView) // Truyền itemView
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CagetoryAdminViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cagetories, parent, false)
        return CagetoryAdminViewHolder(view)
    }

    override fun onBindViewHolder(holder: CagetoryAdminAdapter.CagetoryAdminViewHolder, position: Int) {
        val category = listCagetory[position]
        holder.cagetoryName.text = category.name
        holder.cagetoryImg.setImageResource(category.img)
    }

    override fun getItemCount(): Int {
        return listCagetory.size
    }
}