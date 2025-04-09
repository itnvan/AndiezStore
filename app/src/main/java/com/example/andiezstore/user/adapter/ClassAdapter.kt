package com.example.andiezstore.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.andiezstore.R
import com.example.andiezstore.user.model.Classroom

class ClassAdapter(private val listClass: MutableList<Classroom>) :
    RecyclerView.Adapter<ClassAdapter.ClassViewHolder>() {
    class ClassViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun onBind(classz: Classroom) {
            subjects.text = classz.subject
            subjectDecrips.text = classz.subjectDecrip
            starCounts.text = classz.starCount
            times.text = classz.time
            quantitys.text = classz.quantity
            imgClasss.setImageResource(classz.imgClass)
        }

        private val subjects = view.findViewById<TextView>(R.id.tvSubjects)
        private val subjectDecrips = view.findViewById<TextView>(R.id.tvDecription)
        private val starCounts = view.findViewById<TextView>(R.id.tvStar)
        private val times = view.findViewById<TextView>(R.id.tvTime)
        private val quantitys = view.findViewById<TextView>(R.id.tvQuantity)
        private val imgClasss = view.findViewById<ImageView>(R.id.imgClass)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ClassViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_classroom, parent, false)
        return ClassViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        val classz = listClass[position]
        holder.onBind(classz)
    }

    override fun getItemCount(): Int {
        return listClass.size
    }
}