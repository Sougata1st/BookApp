package com.example.testing.recyclerview.bookapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.testing.recyclerview.bookapp.databinding.ActivityPdfListAdminBinding

class PdfListAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfListAdminBinding
    //category Id and Title
    private var categoryId = ""
    private var category = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfListAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Get from Intent that we passed from adapter
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!
        Log.d("sougata",categoryId)
    }
}