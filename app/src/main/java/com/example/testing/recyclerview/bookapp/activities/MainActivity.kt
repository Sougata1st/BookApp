package com.example.testing.recyclerview.bookapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.testing.recyclerview.bookapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    // Login Clicked
    fun loginclicked(view: View) {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    // skip btn clicked
    fun Skipclicked(view: View) {
        startActivity(Intent(this, DashboardUserActivity::class.java))
    }
}