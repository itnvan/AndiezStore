package com.example.andiezstore.user.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.andiezstore.R
import com.example.andiezstore.databinding.FragmentClassroomBinding
import com.example.andiezstore.user.adapter.ClassAdapter
import com.example.andiezstore.user.model.Classroom

class ClassroomFragment : Fragment() {
    private lateinit var binding: FragmentClassroomBinding
    private lateinit var classAdapter: ClassAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentClassroomBinding.inflate(layoutInflater)
        val listClass = mutableListOf(
            Classroom(
                subject = "Kotlin Course",
                subjectDecrip = "Kotlin with Firebase Course",
                starCount = "4.9",
                time = "26 April 2  025",
                quantity = "91/125",
                imgClass = R.drawable.sflashlogo
            ),
            Classroom(
                subject = "Java Course",
                subjectDecrip = "Java with Firebase Course",
                starCount = "4.3",
                time = "13 September 2024",
                quantity = "82/125",
                imgClass = R.drawable.sflashlogo
            ),
            Classroom(
                subject = "PHP Course",
                subjectDecrip = "Made a website with PHP and HTML",
                starCount = "4.0",
                time = "13 June 2025",
                quantity = "41/65",
                imgClass = R.drawable.sflashlogo
            ),
            Classroom(
                subject = "Spring Boot",
                subjectDecrip = "BackEnd with Spring Boot",
                starCount = "4.4",
                time = "28 October 2025",
                quantity = "49/50",
                imgClass = R.drawable.sflashlogo
            )
        )
        classAdapter = ClassAdapter(listClass)
        binding.rcvClass.adapter = classAdapter
        return binding.root
    }
}