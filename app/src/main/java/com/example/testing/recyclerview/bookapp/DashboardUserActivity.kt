package com.example.testing.recyclerview.bookapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.testing.recyclerview.bookapp.databinding.ActivityDashboardUserBinding
import com.google.firebase.auth.FirebaseAuth

class DashboardUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardUserBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth=FirebaseAuth.getInstance()
        checkuser()
    }

    private fun checkuser() {
        val FirebaseUser = firebaseAuth.currentUser
        if (FirebaseUser != null){
            binding.subTitleTv.text = FirebaseUser.email
        }else{
            startActivity(Intent(this , MainActivity::class.java))
            finish()
        }
    }

    fun LogoutClicked(view: View) {
        firebaseAuth.signOut()
        checkuser()
    }
}