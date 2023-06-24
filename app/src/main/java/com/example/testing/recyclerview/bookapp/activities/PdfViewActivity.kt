package com.example.testing.recyclerview.bookapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.testing.recyclerview.bookapp.Constants.Constants
import com.example.testing.recyclerview.bookapp.databinding.ActivityPdfViewBinding
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfViewActivity : AppCompatActivity() {
    private lateinit var binding:ActivityPdfViewBinding

    var bookId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bookId = intent.getStringExtra("bookId").toString()
        loadBooKDetails()
    }

    private fun loadBooKDetails() {
        //db ref
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val url = snapshot.child("url").value
                //Load book From Url
                loadBookFromURl(url.toString())
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun loadBookFromURl(url: String) {
        val storageRef =FirebaseStorage.getInstance().getReferenceFromUrl(url)
        storageRef.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes->

                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onPageChange(object :OnPageChangeListener{
                        override fun onPageChanged(page: Int, pageCount: Int) {
                            val currPage = page +1
                            binding.subTitleTv.text = "$currPage/$pageCount"
                        }

                    }).load()
                binding.progressBar.visibility = View.GONE

            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE

            }

    }

    fun BackPressed(view: View) {
        onBackPressed()
    }
}