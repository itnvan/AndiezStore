package com.example.andiezstore.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.andiezstore.R
import com.example.andiezstore.user.UserMainActivity
import com.example.andiezstore.utils.Util
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SflashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sflash)
        lifecycleScope.launch {
            delay(2000L)
            Util.hideDialog()
            startActivity(Intent(this@SflashActivity, UserMainActivity::class.java))
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}