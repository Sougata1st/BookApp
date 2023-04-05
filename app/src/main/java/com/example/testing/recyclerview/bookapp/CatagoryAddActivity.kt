package com.example.testing.recyclerview.bookapp

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.testing.recyclerview.bookapp.databinding.ActivityCatagoryAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CatagoryAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCatagoryAddBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCatagoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)
    }

    fun Submitclicked(view: View) {
        validateData()
    }

    private var catagory = ""
    private fun validateData() {
        //validate data

        //get data
        catagory = binding.AddPdfEt.text.toString().trim()

        if (catagory.isEmpty()) {
            Toast.makeText(this, "Enter Catagory...", Toast.LENGTH_SHORT).show()
        } else {
            addCatagoryFirebase()
        }

    }

    private fun addCatagoryFirebase() {
        progressDialog.show()
        val timestamp = System.currentTimeMillis()
        //setup data to be added in Firebase
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["catagory"] = catagory
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"

        //add data to firebase realime database : Database Root > Catagories > CatagoryId > Catagory Info

        val ref = FirebaseDatabase.getInstance().getReference("Catagories")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                //added successfully
                progressDialog.dismiss()
                MyApplication.showSnackBar(findViewById(android.R.id.content),"Added Successfully...",this,
                    ContextCompat.getColor(this,R.color.green), ContextCompat.getColor(this,R.color.black))
                binding.AddPdfEt.setText("")
            }
            .addOnFailureListener { e ->

                progressDialog.dismiss()

                Toast.makeText(this, "Failed to add data due to ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    fun AddCatbackbtnClicked(view: View) {
        super.onBackPressed()
    }
}