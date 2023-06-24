package com.example.testing.recyclerview.bookapp.activities

import android.R
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.testing.recyclerview.bookapp.Constants.Constants
import com.example.testing.recyclerview.bookapp.MyApplication
import com.example.testing.recyclerview.bookapp.MyApplication.Companion.incrementBookViewCount
import com.example.testing.recyclerview.bookapp.databinding.ActivityPdfDetailBinding
import com.example.testing.recyclerview.bookapp.models.ModelPdf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.FileOutputStream

class PdfDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfDetailBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth

    private var isInMyFav = false
    private var bookId = ""
    private var bookTitle = ""
    private var bookUrl = ""
    lateinit var permissions:Array<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bookId = intent.getStringExtra("bookId")!!
        firebaseAuth = FirebaseAuth.getInstance()

        incrementBookViewCount(bookId)

        loadBookCategories()

        progressDialog = ProgressDialog(this)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setTitle("Please Wait...")

        if (firebaseAuth.currentUser != null){
            checkIsFavourite()
        }

        if (Build.VERSION.SDK_INT < 33)
        permissions = arrayOf(
            "android.permission.WRITE_EXTERNAL_STORAGE"
        )
        else
            permissions = arrayOf("android.permission.READ_MEDIA_IMAGES",
                "android.permission.READ_MEDIA_VIDEO")
    }

    private fun loadBookCategories() {
        //Books > BookId > Details
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val model = snapshot.getValue(ModelPdf::class.java)
                    val categoryId = model?.categoryId
                    val description = model?.description
                    val downloadsCouunt = model?.downloadsCouunt
                    val timestamp = model?.timestamp
                    bookTitle = model?.title!!
                    val uid = model.uid
                    bookUrl = model.url
                    val viewsCount = model.viewsCount

                    //Format Date
                    val date = MyApplication.formatTimestamp(timestamp!!)

                    //Load Category
                    MyApplication.loadCategory(categoryId!!, binding.categoryTv)

                    //LoadPdf
                    MyApplication.loadPdfFromUrlSinglePage(
                        bookUrl,
                        bookTitle, binding.pdfView, binding.progressBar, binding.PagesTv
                    )

                    //Load Pdf Size
                    MyApplication.loadPdfSize(bookUrl, bookTitle, binding.sizeTv)

                    binding.TitleTv.text = bookTitle
                    binding.descriptionTv.text = description
                    binding.viewsTv.text = viewsCount.toString()
                    binding.DownloadsTv.text = downloadsCouunt.toString()
                    binding.dateTv.text = date
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    fun BackPressed(view: View) {
        onBackPressed()
    }

    fun readBookClicked(view: View) {
        val intent = Intent(this, PdfViewActivity::class.java)
        intent.putExtra("bookId", bookId)
        startActivity(intent)
    }

    fun downloadClicked(view: View) {
        requestPermissions(permissions, 55)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 55) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("bookappPermissions", "permissions granted")
                downloadBooks()
            } else {
                Log.d("bookappPermissions", "permissions denied")
                Toast.makeText(
                    this,
                    "Please allow the permissions to continue...",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun downloadBooks() {

        progressDialog.setMessage("Downloading Book...")
        progressDialog.show()

        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        storageRef.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                progressDialog.dismiss()
                saveToDownloadsFolder(bytes)
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Log.d("downloadbooks", " Books downloading Failed due to ${e.message}")
            }

    }

    private fun saveToDownloadsFolder(bytes: ByteArray) {
        val nameWithExtension = "$bookTitle${System.currentTimeMillis()}.pdf"
        try {
            val downloadsFolder =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsFolder.mkdirs() //create folder if doesnot exists

            val filePath = downloadsFolder.path + "/" + nameWithExtension

            val out = FileOutputStream(filePath)

            out.write(bytes)
            out.close()
            MyApplication.showSnackBar(
                findViewById(R.id.content),
                "Saved to Downloads Folder",
                this,
                ContextCompat.getColor(
                    this,
                    com.example.testing.recyclerview.bookapp.R.color.green
                ),
                ContextCompat.getColor(this, com.example.testing.recyclerview.bookapp.R.color.black)
            )
            progressDialog.dismiss()

            MyApplication.incrementBookDownloadCount(bookId)
        } catch (e: Exception) {
            progressDialog.dismiss()
            Toast.makeText(this, "Failed to Download due to ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun favouriteClicked(view: View) {
        if (firebaseAuth.currentUser != null) {

            if (isInMyFav){
                MyApplication.removeFromFavourite(this,bookId)
            }else{
                addToFavourite()
            }

        } else {
            MyApplication.showSnackBar(
                findViewById(R.id.content),
                "You're not Logged in",
                this,
                ContextCompat.getColor(this, com.example.testing.recyclerview.bookapp.R.color.red),
                ContextCompat.getColor(this, com.example.testing.recyclerview.bookapp.R.color.black)
            )
        }

    }

    private fun addToFavourite() {
        val timestamp = System.currentTimeMillis()
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["timestamp"] = timestamp
        hashMap["bookId"] = bookId
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .child("Favourites").child(bookId)
            .setValue(hashMap).addOnSuccessListener {
                MyApplication.showSnackBar(
                    findViewById(R.id.content),
                    "Added to Favourites",
                    this,
                    ContextCompat.getColor(
                        this,
                        com.example.testing.recyclerview.bookapp.R.color.green
                    ),
                    ContextCompat.getColor(
                        this,
                        com.example.testing.recyclerview.bookapp.R.color.black
                    )
                )
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to add to favourites due to ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

//    private fun removeFromFavourite() {
//        val ref = FirebaseDatabase.getInstance().getReference("Users")
//        ref.child(firebaseAuth.uid!!)
//            .child("Favourites").child(bookId).removeValue()
//            .addOnSuccessListener {
//                MyApplication.showSnackBar(
//                    findViewById(R.id.content),
//                    "Removed From Favourites",
//                    this,
//                    ContextCompat.getColor(
//                        this,
//                        com.example.testing.recyclerview.bookapp.R.color.green
//                    ),
//                    ContextCompat.getColor(
//                        this,
//                        com.example.testing.recyclerview.bookapp.R.color.black
//                    )
//                )
//            }
//            .addOnFailureListener{ e ->
//                Toast.makeText(
//                    this,
//                    "Failed to remove from favourites due to ${e.message}",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//    }

    private fun checkIsFavourite() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .child("Favourites").child(bookId)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyFav = snapshot.exists()
                    if (isInMyFav){
                        binding.favouriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
                            com.example.testing.recyclerview.bookapp.R.drawable.ic_fav_filled,0,0)
                        binding.favouriteBtn.text = "Remove Favourite"
                    }
                    else{
                        binding.favouriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
                            com.example.testing.recyclerview.bookapp.R.drawable.ic_favourite_white,0,0)
                        binding.favouriteBtn.text = "Add Favourite"
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

    }

}