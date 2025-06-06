package com.example.andiezstore.user.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat // Import GravityCompat
import androidx.drawerlayout.widget.DrawerLayout // Import DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide // Import Glide
import com.example.andiezstore.R
import com.example.andiezstore.databinding.FragmentHomeBinding
import com.example.andiezstore.ui.adapter.SliderAdapter
import com.example.andiezstore.ui.model.CagetoryModel
import com.example.andiezstore.user.adapter.CagetoryAdapter
import com.example.andiezstore.user.adapter.NewsAdapter
import com.example.andiezstore.user.model.News
import com.example.andiezstore.user.viewmodel.NewsViewModel
import com.google.android.material.navigation.NavigationView // Import NavigationView
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
    private lateinit var newsViewModel: NewsViewModel
    private lateinit var homeNewsAdapter: NewsAdapter
    private val newsDataListForHome = mutableListOf<News>()
    private var database: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        swipeRefreshLayout = binding.swipeRefreshLayoutHome
        newsViewModel = ViewModelProvider(this)[NewsViewModel::class.java]
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager2 = binding.viewPager2

        init()
        setUpTransformer()
        getCurrentUserName()
        setupCategoryRecyclerView()
        setupHomeNewsRecyclerView()
        observeNewsViewModelData()
        setupSwipeToRefresh()

        // --- NEW: Handle opening Navigation Drawer ---
        binding.imgMenu.setOnClickListener {
            val drawerLayout = (activity as? AppCompatActivity)?.findViewById<DrawerLayout>(R.id.drawerLayout)
            drawerLayout?.openDrawer(GravityCompat.START) // Mở Drawer từ bên trái
        }
        // --- END NEW ---

        if (newsDataListForHome.isEmpty()) {
            Log.d("HomeFragment", "Initial news fetch triggered.")
            newsViewModel.fetchAllNews()
        }

        binding.imgMess.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_homeChatFragment)
        }

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 2000)
            }
        })
    }

    private fun setupSwipeToRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            Log.d("HomeFragment", "Swipe to refresh triggered, fetching news...")
            newsViewModel.fetchAllNews()
            getCurrentUserName() // Refresh user data on swipe
        }
        swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light
        )
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
                val navController = findNavController()
                when (category.name) {
                    "Subject" -> navController.navigate(R.id.action_homeFragment_to_subjectFragment)
                    "Classroom" -> navController.navigate(R.id.action_homeFragment_to_classroomFragment)
                    "Information" -> navController.navigate(R.id.action_homeFragment_to_informationFragment)
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun getCurrentUserName() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            getUserInformation(uid) // Changed to get more user info
        } else {
            binding.tvUser.text = "Not logged in"
            updateNavHeader(null, null, null) // Clear nav header if not logged in
        }
    }

    // Changed from getUserName to getUserInformation to fetch name, email, and profile image
    private fun getUserInformation(uid: String) {
        database.child(uid).child("information")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userName = snapshot.child("name").getValue(String::class.java)
                        val userEmail = snapshot.child("email").getValue(String::class.java)
                        val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

                        binding.tvUser.text = userName ?: "User"
                        Log.d("HomeFragment", "Fetched user name: $userName")

                        // Update Navigation Drawer header
                        updateNavHeader(userName, userEmail, profileImageUrl)

                    } else {
                        binding.tvUser.text = "User name not found"
                        updateNavHeader(null, null, null) // Clear nav header if info not found
                    }
                }

                @SuppressLint("SetTextI18n")
                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Failed to read user information.", error.toException())
                    binding.tvUser.text = "Error loading user name"
                    updateNavHeader(null, null, null) // Clear nav header on error
                    Toast.makeText(context, "Error loading user profile: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // NEW: Function to update Navigation Drawer header
    private fun updateNavHeader(userName: String?, userEmail: String?, profileImageUrl: String?) {
        val navView = (activity as? AppCompatActivity)?.findViewById<NavigationView>(R.id.navView)
        val navHeaderView = navView?.getHeaderView(0) // Get the header view

        navHeaderView?.let {
            val headerUserName = it.findViewById<TextView>(R.id.textViewUserName)
            val headerUserEmail = it.findViewById<TextView>(R.id.textViewUserEmail)
            val headerUserAvatar = it.findViewById<ImageView>(R.id.imageViewUserAvatar)

            headerUserName?.text = userName ?: getString(R.string.nav_header_title)
            headerUserEmail?.text = userEmail ?: getString(R.string.nav_header_subtitle)

            if (!profileImageUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(profileImageUrl)
                    .placeholder(R.drawable.ic_user) // Default icon for user profile
                    .error(R.drawable.ic_user) // Error icon
                    .into(headerUserAvatar!!)
            } else {
                headerUserAvatar?.setImageResource(R.drawable.ic_user) // Fallback to default if no URL
            }
        }
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
        if (viewPager2.currentItem == imageList.size - 1) {
            viewPager2.currentItem = 0
        } else {
            viewPager2.currentItem = viewPager2.currentItem + 1
        }
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

    private fun setupHomeNewsRecyclerView() {
        homeNewsAdapter = NewsAdapter(newsDataListForHome) { clickedNews ->
            clickedNews.id?.let { newsId ->
                Log.d("HomeFragment", "News clicked: ID - $newsId, Title - ${clickedNews.title}")
                try {
                    val action =
                        HomeFragmentDirections.actionHomeFragmentToNewsDetailFragment(newsId)
                    findNavController().navigate(action)
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Navigation action not found or error: ", e)
                    Toast.makeText(context, "Error navigating", Toast.LENGTH_SHORT)
                        .show()
                }
            } ?: run {
                Toast.makeText(context, "News ID is null", Toast.LENGTH_SHORT).show()
                Log.e("HomeFragment", "News ID is null for title: ${clickedNews.title}")
            }
        }

        binding.rcvProduct.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = homeNewsAdapter
        }
    }

    private fun observeNewsViewModelData() {
        newsViewModel.newsList.observe(viewLifecycleOwner) { newsItems ->
            Log.d("HomeFragment", "News list updated with ${newsItems.size} items.")
            homeNewsAdapter.updateNewsList(newsItems)
        }

        newsViewModel.isLoadingList.observe(viewLifecycleOwner) { isLoading ->
            if (::swipeRefreshLayout.isInitialized) {
                swipeRefreshLayout.isRefreshing = isLoading
            }
            binding.progressBar.visibility = if (isLoading && !swipeRefreshLayout.isRefreshing) View.VISIBLE else View.GONE
            Log.d("HomeFragment", "News list loading state: $isLoading")
        }

        newsViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                Log.e("HomeFragment", "Error observed while fetching news list: $it")
                if (::swipeRefreshLayout.isInitialized && swipeRefreshLayout.isRefreshing) {
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun init() {
        handler = Handler(Looper.getMainLooper())
        imageList = ArrayList()
        imageList.add(R.drawable.view3)
        imageList.add(R.drawable.view)
        imageList.add(R.drawable.view2)
        imageList.add(R.drawable.rule)
        silderAdapter = SliderAdapter(imageList, viewPager2)

        binding.viewPager2.adapter = silderAdapter
        binding.viewPager2.offscreenPageLimit = 1
        binding.viewPager2.setCurrentItem(imageList.size * 1000, false)

        binding.viewPager2.clipToPadding = false
        binding.viewPager2.clipChildren = true
        (binding.viewPager2.getChildAt(0) as? RecyclerView)?.overScrollMode =
            RecyclerView.OVER_SCROLL_NEVER
        binding.dotsIndicator.attachTo(viewPager2)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(runnable)
    }
}