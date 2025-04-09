package com.example.andiezstore.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.andiezstore.R
import com.example.andiezstore.databinding.ActivityMainBinding
import com.example.andiezstore.user.fragments.HomeFragment
import com.example.andiezstore.user.fragments.MessengerFragment
import com.example.andiezstore.user.fragments.ProfileFragment
import com.example.andiezstore.user.fragments.SocialFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(HomeFragment())
        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.item_home -> replaceFragment(HomeFragment())
                R.id.item_mess_user->replaceFragment(MessengerFragment())
                R.id.item_social_user-> replaceFragment(SocialFragment())
                R.id.item_profile_user-> replaceFragment(ProfileFragment())
                else ->{

                }
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager =supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
    }
}