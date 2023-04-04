package com.example.testing.recyclerview.bookapp

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.testing.recyclerview.bookapp.Constants.Constants
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.HashMap

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

            Log.d("barbarreload","load hocche bar bar")

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
                        val model = snapshot.getValue(ModelCategory::class.java)
                        val category = model?.catagory
                        Log.d("seetheerror",category.toString())
                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }

        fun deleteBook(context: Context, bookId: String, bookUrl: String, bookTitle: String) {
            Log.d("sougata",bookUrl.toString())
            val progressBarDialog = ProgressDialog(context)
            progressBarDialog.setTitle("Please wait")
            progressBarDialog.setMessage("Deleting $bookTitle...")
            progressBarDialog.setCanceledOnTouchOutside(false)
            progressBarDialog.show()

            val storegeRef = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storegeRef.delete()
                .addOnSuccessListener {

                    val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                    dbRef.child(bookId).removeValue()
                        .addOnSuccessListener {

                            progressBarDialog.dismiss()

                            Toast.makeText(
                                context,
                                "Successfully deleted",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                        .addOnFailureListener { e->
                            progressBarDialog.dismiss()
                            Toast.makeText(
                                context,
                                "Failed to delete from Storege due to ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .addOnFailureListener { e ->
                    progressBarDialog.dismiss()
                    Toast.makeText(
                        context,
                        "Failed to delete from Storege due to ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        fun incrementBookViewCount(bookId: String){
            //get Curr book views Count
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var viewsCount = snapshot.child("viewsCount").value.toString()
                        if (viewsCount == ""||viewsCount==null){
                            viewsCount ="0"
                        }

                        val newViewsCount = viewsCount.toLong() + 1

                        //setup to data in DB
                        val hashMap:HashMap<String , Any> = HashMap()
                        hashMap["viewsCount"] = newViewsCount

                        val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId).updateChildren(hashMap)

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

        }

        fun incrementBookDownloadCount(bookId: String){
            //get Curr book views Count
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var downloadsCouunt = snapshot.child("downloadsCouunt").value.toString()
                        if (downloadsCouunt == ""||downloadsCouunt==null){
                            downloadsCouunt ="0"
                        }

                        val newDownloadsCount = downloadsCouunt.toLong() + 1

                        //setup to data in DB
                        val hashMap:HashMap<String , Any> = HashMap()
                        hashMap["downloadsCouunt"] = newDownloadsCount

                        val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId).updateChildren(hashMap)

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

        }

    }


}