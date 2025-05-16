package com.example.andiezstore.user.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {
    private lateinit var viewPager2: ViewPager2
    private lateinit var handler: Handler
    private lateinit var imageList: ArrayList<Int>
    private lateinit var silderAdapter: SliderAdapter
    private lateinit var cagetoryAdapter: CagetoryAdapter
    private lateinit var binding: FragmentHomeBinding
    private var database: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager2 = binding.viewPager2
        auth = FirebaseAuth.getInstance()
        init()
        setUpTransformer()
        getCurrentUserName()
        setupCategoryRecyclerView()
        binding.imgMess.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_homeChatFragment)
        }
//        binding.imgSearch.setOnClickListener {
//            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
//        }
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 2000)
            }
        })
    }

    private fun setupCategoryRecyclerView() {
        val categoryList = mutableListOf(
            CagetoryModel(name = "Subject", img = R.drawable.view2),
            CagetoryModel(name = "Classroom", img = R.drawable.view3),
            CagetoryModel(name = "Information", img = R.drawable.view)
        )
        cagetoryAdapter = CagetoryAdapter(categoryList)
        binding.rcvCagetories.adapter = cagetoryAdapter
        cagetoryAdapter.setOnItemClickListener(object : CagetoryAdapter.OnItemClickListener {
            override fun onItemClick(category: CagetoryModel, itemView: View) {
                findNavController().let { navController ->
                    when (category.name) {
                        "Subject" -> navController.navigate(R.id.action_classroomFragment_to_subjectFragment)
                        "Classroom" -> navController.navigate(R.id.action_homeFragment_to_classroomFragment)
                        "Information" -> navController.navigate(R.id.action_classroomFragment_to_informationFragment)
                    }
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun getCurrentUserName() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            getUserName(uid)
        } else {
            binding.tvUser.text = "Not logged in"
        }
    }

    private fun getUserName(uid: String) {
        database.child(uid).child("name")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userName = snapshot.getValue(String::class.java)
                        binding.tvUser.text = userName
                    } else {
                        binding.tvUser.text = "User name not found"
                    }
                }

                @SuppressLint("SetTextI18n")
                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Failed to read user name.", error.toException())
                    binding.tvUser.text = "Error loading user name"
                }
            })
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
        binding.viewPager2.setPageTransformer(transfomer)
    }

    private fun init() {
        handler = Handler(Looper.getMainLooper()) // Use getMainLooper for Fragment
        imageList = ArrayList()
        imageList.add(R.drawable.view3)
        imageList.add(R.drawable.view)
        imageList.add(R.drawable.view2)
        imageList.add(R.drawable.rule)
        silderAdapter = SliderAdapter(imageList, viewPager2)
        binding.viewPager2.adapter = silderAdapter
        binding.viewPager2.offscreenPageLimit = 4
        binding.viewPager2.clipToPadding = false
        binding.viewPager2.clipChildren = true
        (binding.viewPager2.getChildAt(0) as? RecyclerView)?.overScrollMode =
            RecyclerView.OVER_SCROLL_NEVER
        binding.dotsIndicator.attachTo(viewPager2)
        runnable = Runnable {
            var currentPosition = viewPager2.currentItem
            if (currentPosition == imageList.size - 1) {
                currentPosition = 0
            } else {
                currentPosition++
            }
            binding.viewPager2.setCurrentItem(currentPosition, true)
        }
    }
}