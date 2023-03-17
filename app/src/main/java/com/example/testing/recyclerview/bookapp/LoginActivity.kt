package com.example.testing.recyclerview.bookapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun not_have_account_clicked(view: View) {
        startActivity(Intent(this, RegisterActivity::class.java))
    }
}