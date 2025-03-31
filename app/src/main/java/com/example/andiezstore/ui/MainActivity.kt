package com.example.andiezstore.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.andiezstore.R
import com.example.andiezstore.ui.adapter.CagetoryAdapter
import com.example.andiezstore.ui.adapter.SliderAdapter
import com.example.andiezstore.ui.model.CagetoryModel

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager2: ViewPager2
    private lateinit var handler: Handler
    private lateinit var imageList: ArrayList<Int>
    private lateinit var silderAdapter: SliderAdapter
    private lateinit var cagetoryAdapter: CagetoryAdapter
    private lateinit var recyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        recyclerView=findViewById(R.id.rcvCagetories)
        init()
        setUpTransformer()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
       val categoryList = mutableListOf(
            CagetoryModel(name = "Subject", img = R.drawable.shoes1),
            CagetoryModel(name = "Classroom", img = R.drawable.shoes2),
            CagetoryModel(name = "Score", img = R.drawable.shoes1)
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        cagetoryAdapter = CagetoryAdapter(categoryList)
        recyclerView.adapter = cagetoryAdapter


        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 2000)
            }
        })
    }
    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable,2500)
    }
    private val runnable= Runnable {
        viewPager2.currentItem=viewPager2.currentItem+1
    }

    private fun setUpTransformer() {
        val transfomer = CompositePageTransformer()
        transfomer.addTransformer(MarginPageTransformer(40))
        transfomer.addTransformer { page, position ->
            val r = 1 - Math.abs(position)
            page.scaleY = 0.85f + r + 0.14f
        }
        viewPager2.setPageTransformer(transfomer)
    }

    private fun init() {
        viewPager2 = findViewById(R.id.viewPager2)
        handler = Handler(Looper.myLooper()!!)
        imageList = ArrayList()
        imageList.add(R.drawable.background)
        imageList.add(R.drawable.shoes1)
        imageList.add(R.drawable.shoes2)
        imageList.add(R.drawable.adidas_campus)
        silderAdapter = SliderAdapter(imageList, viewPager2)
        viewPager2.adapter = silderAdapter
        viewPager2.offscreenPageLimit = 4
        viewPager2.clipToPadding = false
        viewPager2.clipChildren = false
        viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
    }
}