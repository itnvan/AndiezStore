package com.example.andiezstore.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.andiezstore.R
import com.example.andiezstore.user.model.Classroom

class ClassroomAdminAdapter(private val listClass: List<Classroom>) :
    RecyclerView.Adapter<ClassroomAdminAdapter.ClassroomAdminViewHolder>() {

    class ClassroomAdminViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun onBind(classroom: Classroom) {
            subjects.text = classroom.subject
            subjectDecrips.text = classroom.description
            starCounts.text = classroom.tvStar
            times.text = classroom.timeStart
            quantityS.text = classroom.quantityS.toString()
            quantityE.text = classroom.quantityE.toString()
            classroom.imgClasroom?.let { imgClasss.setImageResource(it) }

        }

        private val subjects = view.findViewById<TextView>(R.id.tvSubjects)
        private val subjectDecrips = view.findViewById<TextView>(R.id.tvDecription)
        private val starCounts = view.findViewById<TextView>(R.id.tvStar)
        private val times = view.findViewById<TextView>(R.id.tvTime)
        private val quantityS = view.findViewById<TextView>(R.id.tvQuantity1)
        private val imgClasss = view.findViewById<ImageView>(R.id.imgClass)
        private val quantityE = view.findViewById<TextView>(R.id.tvQuantity2)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ClassroomAdminViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_classroom, parent, false)
        return ClassroomAdminViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClassroomAdminViewHolder, position: Int) {
        val classroom = listClass[position]
        holder.onBind(classroom)

    }

    override fun getItemCount(): Int {
        return listClass.size
    }
}