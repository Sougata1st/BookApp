package com.example.testing.recyclerview.bookapp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.testing.recyclerview.bookapp.MyApplication
import com.example.testing.recyclerview.bookapp.activities.PdfDetailActivity
import com.example.testing.recyclerview.bookapp.databinding.RowPdfFavBinding
import com.example.testing.recyclerview.bookapp.models.ModelPdf

class AdapterPdfFav(private val context: Context, var pdfArrayList: ArrayList<ModelPdf>) :
    RecyclerView.Adapter<AdapterPdfFav.viewholder>() {

    private lateinit var binding: RowPdfFavBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewholder {
        binding = RowPdfFavBinding.inflate(LayoutInflater.from(context), parent, false)
        return viewholder(binding.root)
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun onBindViewHolder(holder: viewholder, position: Int) {
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
        MyApplication.loadCategory(catagoryId, holder.catagoryTv)

        //we dont need page no here , pass null for page no || load pdf thumbnail
        //MyApplication.loadPdfFromUrlSinglePage(pdfUrl,title , holder.pdfView, holder.progressBar , null)

        //MyApplication.loadPdfSize(pdfUrl, title, holder.sizeTv)

        holder.favBtn.setOnClickListener {
            MyApplication.removeFromFavourite(context, pdfId )
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", pdfId)
            context.startActivity(intent)
        }
    }

    inner class viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val catagoryTv = binding.categoryTv
        val sizeTv = binding.sizeTV
        val dateTv = binding.dateTv
        val favBtn = binding.favBtn
    }



}