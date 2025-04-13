package com.example.andiezstore.user.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import com.example.andiezstore.R
import com.example.andiezstore.databinding.FragmentSubjectBinding
import com.example.andiezstore.user.adapter.SubjectAdapter
import com.example.andiezstore.user.model.Subject

class SubjectFragment : Fragment() {
    private lateinit var binding: FragmentSubjectBinding
    private lateinit var subjectAdapter: SubjectAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentSubjectBinding.inflate(layoutInflater)
        val listSubject=mutableListOf(
            Subject(subject = "Kotlin Course",
                decription = "Kotlin with Firebase Course",
                timeStart = "26 January 2025",
                timeEnd = "26 April 2025",
                imgSubject = R.drawable.sflashlogo),
            Subject(subject = "Java Course",
                decription = "Java with Firebase Course",
                timeStart = "26 January 2025",
                timeEnd = "26 April 2025",
                imgSubject = R.drawable.sflashlogo),
            Subject(subject = "PHP Course",
                decription = "Made a website with PHP and HTML",
                timeStart = "10 May 2025",
                timeEnd = "26 July 2025",
                imgSubject = R.drawable.sflashlogo),
            Subject(subject = "Spring Boot",
                decription = "BackEnd with Spring Boot",
                timeStart = "26 January 2025",
                timeEnd = "26 April 2025",
                imgSubject = R.drawable.sflashlogo),
            Subject(subject = "Kotlin Course",
                decription = "Kotlin with Firebase Course",
                timeStart = "26 January 2025",
                timeEnd = "26 April 2025",
                imgSubject = R.drawable.sflashlogo),
            Subject(subject = "Kotlin Course",
                decription = "Kotlin with Firebase Course",
                timeStart = "26 January 2025",
                timeEnd = "26 April 2025",
                imgSubject = R.drawable.sflashlogo),
        )
        subjectAdapter=SubjectAdapter(listSubject)
        binding.rcvSubject.adapter=subjectAdapter
        return binding.root
    }

}