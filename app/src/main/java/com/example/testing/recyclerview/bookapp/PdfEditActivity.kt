package com.example.testing.recyclerview.bookapp

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.testing.recyclerview.bookapp.databinding.ActivityPdfEditBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class PdfEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPdfEditBinding

    private var bookId = ""
    private var categoryId = ""

    private lateinit var progressDialog: ProgressDialog

    private lateinit var categoryTitleArrayList: ArrayList<String>

    private lateinit var categoryIdArrayList: ArrayList<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId")!!
        categoryId = intent.getStringExtra("categoryId")!!
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadCategories()
        loadBookInfo()
    }

    private fun loadBookInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val selectedCategoryId = snapshot.child("categoryId").value.toString()
                val title = snapshot.child("title").value.toString()
                val description = snapshot.child("description").value.toString()
                binding.titleEt.setText(title)
                binding.descriptionEt.setText(description)
                val refBookCategory = FirebaseDatabase.getInstance().getReference("Catagories")
               refBookCategory.child(selectedCategoryId)
                   .addListenerForSingleValueEvent(object : ValueEventListener{
                       override fun onDataChange(snapshot: DataSnapshot) {
                          binding.categoryTv.text = snapshot.child("catagory").value.toString()
                       }

                       override fun onCancelled(error: DatabaseError) {

                       }

                   })
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun loadCategories() {

        categoryIdArrayList = ArrayList()
        categoryTitleArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Catagories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryIdArrayList.clear()
                categoryTitleArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelCategory::class.java)
                    categoryTitleArrayList.add(model!!.catagory)
                    categoryIdArrayList.add(model.id)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun backbtnClicked(view: View) {
        onBackPressed()
    }

    fun UpdateCLicked(view: View) {
        validateData()
    }


    private var title = ""
    private var description = ""
    private var category = ""
    private fun validateData() {

        //get data
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString()

        //validate data
        if (title.isEmpty()) {
            Toast.makeText(this, "Enter Title...", Toast.LENGTH_SHORT).show()
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Enter Description...", Toast.LENGTH_SHORT).show()
        } else if (category.isEmpty()) {
            Toast.makeText(this, "Pick Category...", Toast.LENGTH_SHORT).show()
        } else {
            //data validated , begin upload
            updateData()
        }
    }

    private fun updateData() {

        progressDialog.setMessage("Updating book Info")
        progressDialog.show()

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["title"] = title
        hashMap["description"] = description
        hashMap["categoryId"] = categoryId

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                MyApplication.showSnackBar(findViewById(android.R.id.content),"Updated data Successfully",this,
                    ContextCompat.getColor(this,R.color.green), ContextCompat.getColor(this,R.color.black))
                progressDialog.dismiss()
            }
            .addOnFailureListener { e ->
                MyApplication.showSnackBar(findViewById(android.R.id.content),"Uploaded...",this,
                    ContextCompat.getColor(this,R.color.red),ContextCompat.getColor(this,R.color.white))
                progressDialog.dismiss()
            }
    }

    fun BookCategoryClicked(view: View) {
        showCategoryDialog()
    }


    private var selectedCategoryTitle = ""

    private fun showCategoryDialog() {
        val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)
        for (i in categoryTitleArrayList.indices) {
            categoriesArray[i] = categoryTitleArrayList[i]
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Select Category")
            .setItems(categoriesArray) { dialog, pos ->
                categoryId = categoryIdArrayList[pos]
                selectedCategoryTitle = categoryTitleArrayList[pos]
                binding.categoryTv.text = selectedCategoryTitle
            }
            .show()
    }



}