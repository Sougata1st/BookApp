package com.example.testing.recyclerview.bookapp

import android.app.Application
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.example.testing.recyclerview.bookapp.Constants.Constants
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }

    companion object {

        //return the date on basis of time
        fun formatTimestamp(timestamp: Long): String? {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp
            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        //fun to get pdf size
        fun loadPdfSize(pdfUrl: String, pdfTitle: String, sizeTv: TextView) {

            //firebase storege ref
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)

            ref.metadata
                .addOnSuccessListener { storegeMetaData ->
                    val bytes = storegeMetaData.sizeBytes.toDouble()

                    val kb = bytes / 1024
                    val mb = kb / 1024

                    if (mb > 1) {
                        sizeTv.text = "${String.format("%.2f", mb)} MB"
                    } else if (kb > 1) {
                        sizeTv.text = "${String.format("%.2f", kb)} KB"
                    } else {
                        sizeTv.text = "${String.format("%.2f", bytes)} bytes"
                    }
                }
                .addOnFailureListener { e ->
                    Log.d("sougata", "Failed to get Metadata due to ${e.message}")
                }
        }

        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            pagesTv: TextView?
        ) {

            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)

            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener { bytes ->

                    pdfView.fromBytes(bytes)
                        .pages(0)//show first page only
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError{t->
                            progressBar.visibility = View.INVISIBLE
                        }
                        .onPageError{page , t->
                            progressBar.visibility = View.INVISIBLE
                        }
                        .onLoad{ nbPages->
                            progressBar.visibility = View.INVISIBLE
                            if (pagesTv != null){
                                pagesTv.text = "$nbPages"
                            }
                        }
                        .load()
                }
                .addOnFailureListener { e ->
                    Log.d("sougata", "Failed to get Metadata due to ${e.message}")
                }
        }

        fun loadCategory(categoryId: String , categoryTv: TextView){
            //load pdf from Firebase by CategoryId
            val ref = FirebaseDatabase.getInstance().getReference("Catagories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val category = snapshot.child("catagory").value as String
                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }

    }
}