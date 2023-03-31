package com.example.testing.recyclerview.bookapp

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.testing.recyclerview.bookapp.databinding.RowPdfAdminBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AdapterPdfAdmin(private val context: Context,  var pdfArrayList: ArrayList<ModelPdf>) :
    RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin>(),Filterable {

    private lateinit var binding: RowPdfAdminBinding
    private var filterList:ArrayList<ModelPdf> = pdfArrayList
    private var filter:FilterPdfAdmin? = null


    //oncreate
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfAdmin(binding.root)
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {
        val model = pdfArrayList[position]
        val pdfId = model.id
        val title = model.title
        val description = model.description
        val catagoryId = model.categoryId
        val pdfUrl = model.url
        val timestamp = model.timestamp
        //convert timestamo to dd/MM/yyyy format
        val formattedDate = MyApplication.formatTimestamp(timestamp)

        //set date
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = formattedDate

        //Load further details i.e category, pdf from url , pdf size

        //load category
        MyApplication.loadCategory(catagoryId , holder.catagoryTv)

        //we dont need page no here , pass null for page no || load pdf thumbnail
        MyApplication.loadPdfFromUrlSinglePage(pdfUrl,title , holder.pdfView, holder.progressBar , null)

        MyApplication.loadPdfSize(pdfUrl,title,holder.sizeTv)

        holder.moreBtn.setOnClickListener {
            moreOptionsDialog( model , holder)
        }

    }

    private fun moreOptionsDialog(model: ModelPdf, holder: AdapterPdfAdmin.HolderPdfAdmin) {

        val bookId = model.id
        val bookUrl = model.url
        val bookTitle = model.title

        //Options to show in dialogue
        val items = arrayOf("Edit", "Delete")
        MaterialAlertDialogBuilder(context)
            .setTitle("Choose Options")
            .setItems(items){dialog, which ->
                if (which == 0){
                    //Edit Clicked
                    val intent = Intent(context, PdfEditActivity::class.java)
                    intent.putExtra("bookId", bookId) // This book id will be uded to delete the book
                    context.startActivity(intent)
                }else if (which == 1){
                    //Delete Clicked
                    MyApplication.deleteBook(context , bookId , bookUrl ,bookTitle)
                }
            }.show()
    }

    //viewholder class
    inner class HolderPdfAdmin(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val catagoryTv = binding.categoryTv
        val sizeTv = binding.sizeTV
        val dateTv = binding.dateTv
        val moreBtn = binding.moreBtn
    }

    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterPdfAdmin(filterList , this@AdapterPdfAdmin)
        }
        return filter as FilterPdfAdmin
    }

}