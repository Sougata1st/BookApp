package com.example.testing.recyclerview.bookapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.example.testing.recyclerview.bookapp.MyApplication
import com.example.testing.recyclerview.bookapp.R
import com.example.testing.recyclerview.bookapp.adapter.AdapterPdfFav
import com.example.testing.recyclerview.bookapp.databinding.ActivityProfileBinding
import com.example.testing.recyclerview.bookapp.models.ModelPdf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    lateinit var binding:ActivityProfileBinding

    lateinit var firebaseAuth: FirebaseAuth

    private lateinit var bookArrayList: ArrayList<ModelPdf>

    private lateinit var AdapterPdfFav:AdapterPdfFav

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()

        loadUserInfo()

    }



    private fun loadUserInfo() {
        //db ref to load user info
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    val email:String = snapshot.child("email").value.toString()
                    val name:String = snapshot.child("name").value.toString()
                    val profileImageUrl:String = snapshot.child("profileImage").value.toString()
                    val timestamp = snapshot.child("timestamp").value
                    val uid:String = snapshot.child("uid").value.toString()
                    val userType:String = snapshot.child("userType").value.toString()

                    val formattedDate = MyApplication.formatTimestamp(timestamp as Long)
                    Log.d("Sougata",formattedDate.toString())

                    //set data
                    binding.nameTv.text = name
                    binding.emailTv.text = email
                    binding.accountTypeTv.text = userType
                    binding.memberDateTv.text = formattedDate

                    try {
                        Glide.with(this@ProfileActivity)
                            .load(profileImageUrl)
                            .centerCrop()
                            .placeholder(R.drawable.ic_person)
                            .into(binding.profileIv)
                    }
                    catch (e:Exception){

                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    fun editClicked(view: View) {
        startActivity(Intent(this,ProfileEditActivity::class.java))
    }
    fun backBtnClicked(view: View) {
        onBackPressed()
    }
}