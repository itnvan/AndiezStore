package com.example.andiezstore.user

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.andiezstore.R
import com.example.andiezstore.databinding.ActivityUserMainBinding
import com.example.andiezstore.user.fragments.HomeFragment
import com.example.andiezstore.user.fragments.MessengerFragment
import com.example.andiezstore.user.fragments.ProfileFragment
import com.example.andiezstore.user.fragments.SocialFragment

class UserMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserMainBinding.inflate(layoutInflater)
        setContentView(binding.root)}
}