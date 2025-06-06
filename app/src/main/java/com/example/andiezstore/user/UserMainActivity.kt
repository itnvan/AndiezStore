package com.example.andiezstore.user

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import com.example.andiezstore.databinding.ActivityUserMainBinding


class UserMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
