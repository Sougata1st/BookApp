package com.example.testing.recyclerview.bookapp.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.example.testing.recyclerview.bookapp.filters.FilterCatagory
import com.example.testing.recyclerview.bookapp.MyApplication
import com.example.testing.recyclerview.bookapp.activities.PdfListAdminActivity
import com.example.testing.recyclerview.bookapp.databinding.RowCategoryBinding
import com.example.testing.recyclerview.bookapp.models.ModelCategory
import com.example.testing.recyclerview.bookapp.models.ModelPdf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterCatagory(
    private val context: Context,
    categoryArrayList: ArrayList<ModelCategory>
) : Adapter<AdapterCatagory.viwholder>(), Filterable {
    private lateinit var binding: RowCategoryBinding
    var categoryArrayList:ArrayList<ModelCategory> =categoryArrayList
    private var filterList:ArrayList<ModelCategory> = categoryArrayList
    private var filter: FilterCatagory? = null

    inner class viwholder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var catagoryTv:TextView = binding.categoryTv
        var deleteBtn:ImageButton = binding.deleteBtn
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viwholder {

         binding = RowCategoryBinding.inflate(LayoutInflater.from(context) , parent , false)
        return viwholder(binding.root)
    }

    override fun getItemCount(): Int {
        return categoryArrayList.size
    }

    override fun onBindViewHolder(holder: viwholder, position: Int) {
        val model = categoryArrayList[position]
        val id = model.id
        val catagory = model.catagory
        val uid = model.uid
        val timestamp = model.timestamp
        holder.catagoryTv.text = catagory
        holder.deleteBtn.setOnClickListener{
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
                .setMessage("Are you sure , you want to delete this catagory?")
                .setPositiveButton("Confirm"){a,d->
                    //show the toast message
                  Toast.makeText(context,"Deleting...",Toast.LENGTH_SHORT).show()
                    deleteCategory(model , holder)
                }
                .setNegativeButton("Cancel"){a,d->
                    a.dismiss()
                }
                .show()
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context , PdfListAdminActivity::class.java)
            intent.putExtra("categoryId", id)
            intent.putExtra("category", catagory)
            context.startActivity(intent)
        }
    }

    private fun deleteCategory(model: ModelCategory, holder: viwholder) {
        //get id of category to delete
        val id = model.id
        //firebase db>Catagories>categoryid
        val ref = FirebaseDatabase.getInstance().getReference("Catagories")
            ref.child(id)
                .removeValue()
                .addOnSuccessListener {

                    val bookref = FirebaseDatabase.getInstance().getReference("Books")
                    bookref.addListenerForSingleValueEvent(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (ds in snapshot.children){
                                val bookmodel = ds.getValue(ModelPdf::class.java)
                                if (bookmodel?.categoryId == id){
                                    FirebaseDatabase.getInstance().getReference("Books").child(bookmodel.id)
                                        .removeValue().addOnSuccessListener {
                                            MyApplication.deleteBook(
                                                context,
                                                bookmodel.id,
                                                bookmodel.url,
                                                bookmodel.title
                                            )
                                        }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
                    Toast.makeText(context,"Deleted",Toast.LENGTH_SHORT).show()

                }
                .addOnFailureListener { e->
                    Toast.makeText(context,"Unable to delete due to ${e.message}",Toast.LENGTH_SHORT).show()
                }
    }

    override fun getFilter(): Filter {
        if (filter == null){

            filter = FilterCatagory(filterList , this@AdapterCatagory)
        }
        return filter as FilterCatagory
    }
}