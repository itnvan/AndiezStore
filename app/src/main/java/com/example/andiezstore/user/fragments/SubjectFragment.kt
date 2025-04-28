package com.example.andiezstore.user.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.andiezstore.R
import com.example.andiezstore.databinding.FragmentSubjectBinding
import com.example.andiezstore.user.adapter.SubjectAdapter
import com.example.andiezstore.user.model.Subject


class SubjectFragment : Fragment() {
    private lateinit var binding: FragmentSubjectBinding
    private lateinit var subjectAdapter: SubjectAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSubjectBinding.inflate(layoutInflater)
        val listSubject = mutableListOf(
            Subject(
                subject = "Kotlin Course",
                description = "Kotlin with Firebase Course",
                timeStart = "26 January 2025",
                tvStar = "4.5",
                quantityS =null,
                quantityE = 100,
                imgSubject = R.drawable.sflashlogo
            ),
            Subject(
                subject = "Java Course",
                description = "Java with Firebase Course",
                timeStart = "26 January 2025",
                tvStar = "4.9",
                quantityS =null,
                quantityE = 100,
                imgSubject = R.drawable.sflashlogo
            ),
            Subject(
                subject = "PHP Course",
                description = "Made a website with PHP and HTML",
                timeStart = "10 May 2025",
                tvStar = "5.0",
                quantityS =null,
                quantityE = 100,
                imgSubject = R.drawable.sflashlogo
            ),
            Subject(
                subject = "Spring Boot",
                description = "BackEnd with Spring Boot",
                timeStart = "26 January 2025",
                tvStar = "4.1",
                quantityS =null,
                quantityE = 100,
                imgSubject = R.drawable.sflashlogo
            ),
            Subject(
                subject = "Lavavel Course",
                description = "Start with Lavavel BackEnd",
                timeStart = "26 January 2025",
                tvStar = "4.7",
                quantityS =null,
                quantityE = 100,
                imgSubject = R.drawable.sflashlogo
            ),
            Subject(
                subject = "Css Course",
                description = "Start Css for beginner",
                timeStart = "26 January 2025",
                tvStar = "26 April 2025",
                quantityS =null,
                quantityE = 100,
                imgSubject = R.drawable.sflashlogo
            ),
        )
        subjectAdapter = SubjectAdapter(
            listSubject,
            context = requireContext()
        )
        binding.rcvSubject.adapter = subjectAdapter
        return binding.root
    }
}
