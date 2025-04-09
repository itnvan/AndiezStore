package com.example.andiezstore.user.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.andiezstore.R
import com.example.andiezstore.databinding.FragmentHomeBinding
import com.example.andiezstore.ui.adapter.SliderAdapter
import com.example.andiezstore.ui.model.CagetoryModel
import com.example.andiezstore.user.adapter.CagetoryAdapter

class HomeFragment : Fragment() {
    private lateinit var viewPager2: ViewPager2
    private lateinit var handler: Handler
    private lateinit var imageList: ArrayList<Int>
    private lateinit var silderAdapter: SliderAdapter
    private lateinit var cagetoryAdapter: CagetoryAdapter
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewPager2 = binding.viewPager2 // Khởi tạo viewPager2 bằng View Binding
        init()
        setUpTransformer()
        val categoryList = mutableListOf(
            CagetoryModel(name = "Subject", img = R.drawable.view2),
            CagetoryModel(name = "Classroom", img = R.drawable.view3),
            CagetoryModel(name = "Information", img = R.drawable.view)
        )
        cagetoryAdapter = CagetoryAdapter(categoryList)
        binding.rcvCagetories.adapter = cagetoryAdapter // Sử dụng binding để truy cập recyclerView

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 2000)
            }
        })

        cagetoryAdapter.setOnItemClickListener(object : CagetoryAdapter.OnItemClickListener {
            override fun onItemClick(category: CagetoryModel) {
                when (category.name) {
                    "Subject" -> findNavController(binding.root).navigate(R.id.action_homeFragment_to_subjectFragment)
                    "Classroom" -> findNavController(binding.root).navigate(R.id.action_homeFragment_to_classroomFragment)
                    "Information" -> findNavController(binding.root).navigate(R.id.action_homeFragment_to_informationFragment)
                }
            }
        })

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable, 2500)
    }

    private var runnable = Runnable {
        viewPager2.currentItem = viewPager2.currentItem + 1
    }

    private fun setUpTransformer() {
        val transfomer = CompositePageTransformer()
        transfomer.addTransformer(MarginPageTransformer(40))
        transfomer.addTransformer { page, position ->
            val r = 1 - Math.abs(position)
            page.scaleY = 0.85f + r + 0.14f
        }
        binding.viewPager2.setPageTransformer(transfomer) // Sử dụng binding
    }

    private fun init() {
        handler = Handler(Looper.myLooper()!!)
        imageList = ArrayList()
        imageList.add(R.drawable.view3)
        imageList.add(R.drawable.view)
        imageList.add(R.drawable.view2)
        imageList.add(R.drawable.rule)
        silderAdapter = SliderAdapter(imageList, viewPager2) // viewPager2 đã được khởi tạo
        binding.viewPager2.adapter = silderAdapter // Sử dụng binding
        binding.viewPager2.offscreenPageLimit = 4 // Sử dụng binding
        binding.viewPager2.clipToPadding = false // Sử dụng binding
        binding.viewPager2.clipChildren = true // Sử dụng binding
        binding.viewPager2.getChildAt(0).overScrollMode =
            RecyclerView.OVER_SCROLL_NEVER // Sử dụng binding
        binding.dotsIndicator.attachTo(viewPager2) // viewPager2 đã được khởi tạo
        runnable = Runnable {
            var currentPosition = viewPager2.currentItem
            if (currentPosition == imageList.size - 1) {
                currentPosition = 0
            } else {
                currentPosition++
            }
            binding.viewPager2.setCurrentItem(currentPosition, true) // Sử dụng binding
        }
    }
}