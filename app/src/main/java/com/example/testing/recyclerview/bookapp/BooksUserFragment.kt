package com.example.testing.recyclerview.bookapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.testing.recyclerview.bookapp.adapter.AdapterPdfUser
import com.example.testing.recyclerview.bookapp.databinding.FragmentBooksUserBinding
import com.example.testing.recyclerview.bookapp.models.ModelPdf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class BooksUserFragment : Fragment {

    private lateinit var binding: FragmentBooksUserBinding

    public companion object {

        public fun newInstance(
            categoryId: String,
            category: String,
            uid: String
        ): BooksUserFragment {
            val fragment = BooksUserFragment()
            //put data to Bundle intent
            val args = Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid = ""

    //arraylist to hold pdf
    private lateinit var pdfArrayList: ArrayList<ModelPdf>
    private lateinit var adapterPdfUser: AdapterPdfUser

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //get arguments that we passed in newInstance method
        val args = arguments
        if (args != null) {
            categoryId = args.getString("categoryId")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBooksUserBinding.inflate(LayoutInflater.from(context), container, false)

        //load pdf according to category, this fragment will have new instance to load each category pdfs
        Log.d("FragMentActivity", "onCreateView: Category: $category")
        if (category == "All") {
            loadAllBooks()
        } else if (category == "Most Viewed") {
            loadMostViewedDownloadedBooks("viewsCount")
        } else if (category == "Most Downloaded") {
            loadMostViewedDownloadedBooks("downloadsCouunt")
        } else {
            loadCategorizedBooks()
        }

        binding.SearchEt.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterPdfUser.filter.filter(s)
                }catch (e : Exception){
                    Log.d("FragMentActivity", "onTextChanged: SEARCH EXCEPTION ${e.message}")
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        return binding.root
    }

    private fun loadAllBooks() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children){
                    val model = ds.getValue(ModelPdf::class.java)
                    pdfArrayList.add(model!!)
                }
                //setup Adapter
                try {
                    adapterPdfUser = AdapterPdfUser(context!!,pdfArrayList)
                }catch (e: Exception){
                    Log.d("sougataexception", e.message.toString())
                }


                //set adapter to recyclerview
                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun loadMostViewedDownloadedBooks(orderBy: String) {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(orderBy).limitToLast(10)//load 10most viewed or most downloaded
            .addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children){
                    val model = ds.getValue(ModelPdf::class.java)
                    pdfArrayList.add(model!!)
                }
                //setup Adapter
                try {
                    adapterPdfUser = AdapterPdfUser(context!!,pdfArrayList)
                }catch (e: Exception){
                    Log.d("sougataexception", e.message.toString())
                }
                //adapterPdfUser = AdapterPdfUser(context!!,pdfArrayList)

                //set adapter to recyclerview
                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun loadCategorizedBooks() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId).addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children){
                        val model = ds.getValue(ModelPdf::class.java)
                        pdfArrayList.add(model!!)
                    }
                    //setup Adapter
                    try {
                        adapterPdfUser = AdapterPdfUser(context!!,pdfArrayList)
                        adapterPdfUser = AdapterPdfUser(context!!,pdfArrayList)
                        adapterPdfUser = AdapterPdfUser(context!!,pdfArrayList)

                    }catch (e: Exception){
                        Log.d("sougataexception", e.message.toString())
                    }


                    //set adapter to recyclerview
                    binding.booksRv.adapter = adapterPdfUser
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

}