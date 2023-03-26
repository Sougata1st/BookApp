package com.example.testing.recyclerview.bookapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.example.testing.recyclerview.bookapp.databinding.ActivityDashboardAdminBinding
import com.example.testing.recyclerview.bookapp.databinding.ActivityDashboardUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardAdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardAdminBinding
    private lateinit var firebaseAuth:FirebaseAuth

    //arraylist to hold categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    //adapter
    private lateinit var adapterCatagory: AdapterCatagory
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()

        checkuser()
        loadCategories()

        //search
        binding.searchbox.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //called when user types anything
                try {
                    Log.d("sougata",s.toString())
                    adapterCatagory.filter.filter(s)
                }catch (e:Exception){
                 // Log.d("sougata",s.toString())
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
    }

    private fun loadCategories() {
        //init categoryArrayList
        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Catagories")

        ref.addValueEventListener( object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before adding data to it
                categoryArrayList.clear()
                for (ds in snapshot.children){
                    //get data as model
                    val model = ds.getValue(ModelCategory::class.java)

                    //add to categoryArrayList
                    categoryArrayList.add(model!!)
                }
                //setup adapter
                adapterCatagory = AdapterCatagory(this@DashboardAdminActivity, categoryArrayList)
                //set adapter to RecyclerView
                binding.catagoriesRv.adapter = adapterCatagory
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
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

    fun LogoutClickedAd(view: View) {
        firebaseAuth.signOut()
        checkuser()
    }

    fun AddCatagoryClicked(view: View) {
        startActivity(Intent(this, CatagoryAddActivity::class.java))
    }

}