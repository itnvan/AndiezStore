package com.example.andiezstore.user

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.andiezstore.R
import com.example.andiezstore.databinding.ActivityUserMainBinding
import com.example.andiezstore.ui.fragments.ChoiceFragment
import com.example.andiezstore.user.fragments.HomeFragment
import com.example.andiezstore.user.fragments.MessengerFragment
import com.example.andiezstore.user.fragments.ProfileFragment
import com.example.andiezstore.user.fragments.SocialFragment

class UserMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserMainBinding
//    private lateinit var navController: androidx.navigation.NavController
//    private var isLoggedIn: Boolean = false // Biến để theo dõi trạng thái đăng nhập

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Kiểm tra trạng thái đăng nhập.
//        isLoggedIn = intent.getBooleanExtra("isLoggedIn", false)

//        if (isLoggedIn) {
        // Nếu đã đăng nhập, hiển thị BottomNavigationView và thiết lập điều hướng.
//            binding.bottomNavigationView.visibility = android.view.View.VISIBLE
//            navController = findNavController(R.id.nav_host_fragment_user)
//            binding.bottomNavigationView.setupWithNavController(navController)
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_home -> replaceFragment(HomeFragment())
                R.id.item_mess_user -> replaceFragment(MessengerFragment())
                R.id.item_social_user -> replaceFragment(SocialFragment())
                R.id.item_profile_user -> replaceFragment(ProfileFragment())
                else -> {
                    // Handle unrecognised item
                }
            }
            true
        }
//        } else {
//            // Nếu chưa đăng nhập, ẩn BottomNavigationView và hiển thị LoginFragment.
//            binding.bottomNavigationView.visibility = android.view.View.GONE
//            replaceFragment(ChoiceFragment()) // Sử dụng replaceFragment để hiển thị LoginFragment
//        }
    }

//    private fun navigateTo(destinationId: Int) {
//        navController.navigate(destinationId)
//    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.nav_host_fragment_user, fragment) // Đảm bảo bạn sử dụng đúng ID của FrameLayout
        fragmentTransaction.commit()
    }
}
