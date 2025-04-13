package com.example.andiezstore.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.andiezstore.R
import com.example.andiezstore.user.model.Subject

open class SubjectAdapter(private val listSubject: MutableList<Subject>): RecyclerView.Adapter<SubjectAdapter.SubjectViewModel>() {
    class SubjectViewModel(val view:View): RecyclerView.ViewHolder(view) {
        val tvSubject=view.findViewById<TextView>(R.id.tvSubjects)
        val tvDecription=view.findViewById<TextView>(R.id.tvDecription)
        val timeStart=view.findViewById<TextView>(R.id.tvStart)
        val timeEnd=view.findViewById<TextView>(R.id.tvTime)
        val imgSubject=view.findViewById<ImageView>(R.id.imgSubject)
        fun onBind(subject: Subject) {
            tvSubject.text=subject.subject
            tvDecription.text=subject.decription
            timeStart.text=subject.timeStart
            timeEnd.text=subject.timeEnd
            imgSubject.setImageResource(subject.imgSubject)
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SubjectAdapter.SubjectViewModel {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.item_subject,parent,false)
        return SubjectViewModel(view)
    }

    override fun onBindViewHolder(holder: SubjectAdapter.SubjectViewModel, position: Int) {
        val subject=listSubject[position]
        holder.onBind(subject)

    }

    override fun getItemCount(): Int {
        return listSubject.size
    }
}