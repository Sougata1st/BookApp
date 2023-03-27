package com.example.testing.recyclerview.bookapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.testing.recyclerview.bookapp.databinding.ActivityRegisterBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {
    lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)
    }

    fun backbtnClicked(view: View) {
        super.onBackPressed()
    }

    fun registerclicked(view: View) {
        // 1) Input data
        // 2) Validate Data
        // 3) Create Account
        // 4) Save user info -Firebase Realtime Database
        validatedata()
    }

    private var name = ""
    private var email = ""
    private var pass = ""
    private var cpass = ""


    private fun validatedata() {
        // 1) Input data
        name = binding.nameEt.text.toString().trim()
        email = binding.RegisterEmailEt.text.toString().trim()
        pass = binding.RegisterPassEt.text.toString().trim()
        cpass = binding.RegisterConfirmPassEt.text.toString().trim()
        Log.e("sougata",pass.toString())
        if (name.isEmpty()) {
            Toast.makeText(this, "Enter your name...", Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email...", Toast.LENGTH_SHORT).show()
        } else if (pass.isEmpty()) {
            Toast.makeText(this, "Enter password...", Toast.LENGTH_SHORT).show()
        } else if (cpass.isEmpty()) {
            Toast.makeText(this, "Confirm your password...", Toast.LENGTH_SHORT).show()
        } else if (pass != cpass) {
            Toast.makeText(this, "Password doesn't match...", Toast.LENGTH_SHORT).show()
        } else {
            createUserAccount()
        }
    }

    private fun createUserAccount() {
        // 3) Create Account
        progressDialog.setMessage("Creating Account...")
        progressDialog.show()
        firebaseAuth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                //user created , now add user info in db
                updateUserInfo()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed Creating account due to ${e.message.toString()}...",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun updateUserInfo() {
        // 4) Save user info -Firebase Realtime Database
        progressDialog.setMessage("Saving user Info...")

        //timestamp
        val timestamp = System.currentTimeMillis()

        //get current user uid , since user is registered we can get it
        val uid = firebaseAuth.uid
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"] = "" // will do in profile edit
        hashMap["userType"] =
            "user" // possible values are user and admin , will change value to admin manually in firebase db
        hashMap["timestamp"] = timestamp

        val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                //user info saved , open user dashboard
                progressDialog.dismiss()
                Toast.makeText(this,"account created...",Toast.LENGTH_SHORT).show()
                startActivity(Intent(this,DashboardUserActivity::class.java))
            }
            .addOnFailureListener{e->
                //failed adding data to db
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed saving data to DB due to ${e.message.toString()}...",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}