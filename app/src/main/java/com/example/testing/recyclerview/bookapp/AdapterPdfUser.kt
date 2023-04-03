package com.example.testing.recyclerview.bookapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.RecyclerView
import com.example.testing.recyclerview.bookapp.databinding.RowPdfUserBinding

class AdapterPdfUser(
    private var context: Context,
    var pdfArrayList: ArrayList<ModelPdf>
): RecyclerView.Adapter<AdapterPdfUser.viewHolder>(), Filterable{

    private lateinit var binding:RowPdfUserBinding
    private var filterList:ArrayList<ModelPdf> = pdfArrayList
    private var filter: FilterPdfUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context) , parent ,false)
        return viewHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
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
       // MyApplication.loadPdfFromUrlSinglePage(pdfUrl,title , holder.pdfView, holder.progressBar , null)

        MyApplication.loadPdfSize(pdfUrl,title,holder.sizeTv)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId",pdfId)
            context.startActivity(intent)
        }
    }

    inner class viewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
//        val pdfView = binding.pdfView
//        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val catagoryTv = binding.categoryTv
        val sizeTv = binding.sizeTV
        val dateTv = binding.dateTv
    }

    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterPdfUser(filterList,this@AdapterPdfUser)
        }
        return filter as FilterPdfUser
    }
}