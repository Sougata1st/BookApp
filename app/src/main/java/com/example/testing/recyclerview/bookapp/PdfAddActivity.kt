package com.example.testing.recyclerview.bookapp

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.testing.recyclerview.bookapp.databinding.ActivityPdfAddBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class PdfAddActivity : AppCompatActivity() {
    lateinit var binding: ActivityPdfAddBinding

    //firebase Auth
    lateinit var firebaseAuth: FirebaseAuth

    //progress dialog(show while uploading pdf)
    private lateinit var progressDialog: ProgressDialog

    //Arraylist to hold pdf categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    //uri of picked pdf
    private var pdfuri: Uri? = null

    //TAG
    private val TAG = "PDF_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Init Firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        loadPdfCategories()

        //setup progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)
    }

    private fun loadPdfCategories() {
        categoryArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Catagories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                for (ds in snapshot.children) {

                    val model = ds.getValue(ModelCategory::class.java)
                    categoryArrayList.add(model!!)
                    Log.d(TAG, "onDAtaChange: ${model.catagory}")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""
    private fun categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: Showing pdf category pick dialog")

        //get string array of categories from arraylist
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in 0 until categoryArrayList.size) {
            categoriesArray[i] = categoryArrayList[i].catagory
        }
        Log.d("sougata", categoryArrayList.size.toString())

        Log.d("sougata", Arrays.toString(categoriesArray))
        //alertdialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            .setItems(categoriesArray) { dialog, which ->
                //handle item clicked
                //get item click
                selectedCategoryTitle = categoryArrayList[which].catagory
                selectedCategoryId = categoryArrayList[which].id
                binding.categoryTv.text = selectedCategoryTitle
                Log.d(TAG, "CategoryPickDialog: Selected category id: $selectedCategoryId")
                Log.d(TAG, "CategoryPickDialog: Selected category Title: $selectedCategoryTitle")
            }.show()
    }

    private fun pdfPickIntent() {
        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }

    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "Pdf picked ")
                pdfuri = result.data!!.data
            } else {
                Log.d(TAG, "PDF pick cancelled ")
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )

    fun backbtnClicked(view: View) {
        onBackPressed()
    }

    fun uploadCLicked(view: View) {
        //1) validate data
        validateData()
        //2) upload pdf to firebase storage
        //3)get url of uploaded pdf
        //4)upload pdf into firebase db

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
        } else if (pdfuri == null) {
            Toast.makeText(this, "Pick PDF...", Toast.LENGTH_SHORT).show()
        } else {
            //data validated , begin upload
            uploadPdfToStorage()
        }
    }

    private fun uploadPdfToStorage() {
        // upload pdf to firebase storage
        Log.d(TAG, "Uploading to storage...")

        //show progress dialog
        progressDialog.setMessage("Uploading PDF...")
        progressDialog.setCanceledOnTouchOutside(false)                         //
        progressDialog.show()

        //timestamp
        val timestamp = System.currentTimeMillis()

        //path of pdf in firebase storage
        val filePathAndName = "Books/$timestamp"
        //storage reference
        val storageRefernce = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageRefernce.putFile(pdfuri!!)
            .addOnSuccessListener { tasksnapshot ->
                Log.d(TAG, "Uploaded pdf now getting url")

                //get url of uploaded pdf
                val uriTask: Task<Uri> = tasksnapshot.storage.downloadUrl

                while (!uriTask.isSuccessful);
                val uploadPdfUrl = "${uriTask.result}"
                Log.d("pdfurl",uploadPdfUrl)
                uploadPdfInfoToDb(uploadPdfUrl, timestamp)
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "Failed to upload sue to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload PDF due to ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun uploadPdfInfoToDb(uploadPdfUrl: String, timestamp: Long) {
        //upload pdf in firebase db
        Log.d(TAG,"uploading pdf to db")
        progressDialog.setMessage("Uploading PDF info...")

        //uid of current user
        val uid = firebaseAuth.uid
        val hashmap: HashMap<String, Any> = HashMap()
        hashmap["uid"] = "$uid"
        hashmap["id"] = "$timestamp"
        hashmap["title"] = "$title"
        hashmap["description"] = "$description"
        hashmap["categoryId"] = "$selectedCategoryId"
        hashmap["url"] = "$uploadPdfUrl"
        hashmap["timestamp"] = timestamp
        hashmap["viewsCount"] = 0
        hashmap["downloadsCouunt"] = 0

        //db ref > Books >BookId > (Book Info)
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(hashmap)
            .addOnSuccessListener {
                Log.d(TAG, "uploadPdfInfoToDb: Uploaded pdf to db")
                progressDialog.dismiss()
                Toast.makeText(this, "Uploaded...", Toast.LENGTH_SHORT)
                    .show()
                pdfuri = null
            }
            .addOnFailureListener {e->
                Log.d(TAG, "Failed to upload sue to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "uploadPdfInfoToDb: Failed to upload PDF due to ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }

    }


    fun BookCategoryClicked(view: View) {
        categoryPickDialog()
    }

    fun attachPdfBtnClicked(view: View) {
        pdfPickIntent()
    }
}