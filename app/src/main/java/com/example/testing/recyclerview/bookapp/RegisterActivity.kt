package com.example.testing.recyclerview.bookapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
    }

    fun backbtnClicked(view: View) {
        super.onBackPressed()
    }
}