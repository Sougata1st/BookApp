package com.example.testing.recyclerview.bookapp.activities

import android.app.ProgressDialog
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.testing.recyclerview.bookapp.MyApplication
import com.example.testing.recyclerview.bookapp.R
import com.example.testing.recyclerview.bookapp.databinding.ActivityProfileEditBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProfileEditActivity : AppCompatActivity() {

    lateinit var binding: ActivityProfileEditBinding

    lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>

    private var imageUri: Uri? = null


    //progress dialog(show while uploading pdf)
    private lateinit var progressDialog: ProgressDialog

    lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        loadData()

        //initialize progress bar
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //imagepickker
        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                imageUri = uri
                Glide.with(this)
                    .load(uri)
                    .centerCrop().placeholder(R.drawable.ic_person)
                    .into(binding.profileIv)
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    }

    private fun loadData() {
        val dbRef = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.child(firebaseAuth.uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name: String = snapshot.child("name").value.toString()
                    val profileImageUrl: String = snapshot.child("profileImage").value.toString()
                    binding.nameEditEt.setText(name)

                    try {
                        Glide.with(this@ProfileEditActivity)
                            .load(profileImageUrl)
                            .centerCrop()
                            .placeholder(R.drawable.ic_person)
                            .into(binding.profileIv)
                    } catch (e: Exception) {

                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    fun updateClicked(view: View) {
        validateData()
    }

    private fun validateData() {
        if (binding.nameEditEt.text.isEmpty()){
            MyApplication.showSnackBar(
                findViewById(android.R.id.content),
                "Name can't be empty...",
                this,
                ContextCompat.getColor(
                    this,
                    com.example.testing.recyclerview.bookapp.R.color.red
                ),
                ContextCompat.getColor(
                    this,
                    com.example.testing.recyclerview.bookapp.R.color.white
                )
            )
        }else{
            uploadToStorage()
        }
    }

    private fun uploadToStorage() {
        progressDialog.setMessage("Updating data...")
        progressDialog.setCanceledOnTouchOutside(false)                         //
        progressDialog.show()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .child("name")
            .setValue(binding.nameEditEt.text.toString())
            .addOnSuccessListener {
                progressDialog.dismiss()
                MyApplication.showSnackBar(
                    findViewById(android.R.id.content),
                    "Updated...",
                    this,
                    ContextCompat.getColor(
                        this,
                        R.color.green
                    ),
                    ContextCompat.getColor(
                        this,
                        R.color.black
                    )
                )
            }
            .addOnFailureListener{ e->
                Toast.makeText(this,"cannot update name due to ${e.message}",Toast.LENGTH_LONG).show()
                progressDialog.dismiss()
            }

        // upload pdf to firebase storage

        //show progress dialog


        if (imageUri!=null) {

            progressDialog.setMessage("Uploading image...")
            progressDialog.setCanceledOnTouchOutside(false)                         //
            progressDialog.show()

            //path of pdf in firebase storage
            val filePathAndName = "ProfilePic/${firebaseAuth.uid}"
            //storage reference
            val storageRefernce = FirebaseStorage.getInstance().getReference(filePathAndName)
            storageRefernce.putFile(imageUri!!)
                .addOnSuccessListener { tasksnapshot ->


                    //get url of uploaded pdf
                    val uriTask = tasksnapshot.storage.downloadUrl

                    while (!uriTask.isSuccessful);
                    val uploadimageUrl = "${uriTask.result}"
                    Log.d("pdfurl", uploadimageUrl)

                    uploadPdfInfoToDb(uploadimageUrl)
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(
                        this,
                        "Failed to upload PDF due to ${e.message}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
        }
    }

    private fun uploadPdfInfoToDb(uploadimageUrl: String) {

        progressDialog.setMessage("Uploading image info...")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("profileImage")
            .setValue(uploadimageUrl).addOnSuccessListener {

                progressDialog.dismiss()

                MyApplication.showSnackBar(
                    findViewById(android.R.id.content),
                    "Uploaded...",
                    this,
                    ContextCompat.getColor(
                        this,
                        R.color.green
                    ),
                    ContextCompat.getColor(
                        this,
                        R.color.black
                    )
                )

                imageUri = null
            }
            .addOnFailureListener {e->

                progressDialog.dismiss()
                Toast.makeText(this, "uploadPdfInfoToDb: Failed to upload PDF due to ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    fun backbtnClicked(view: View) {
        onBackPressed()
    }

    fun imageViewCLicked(view: View) {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}