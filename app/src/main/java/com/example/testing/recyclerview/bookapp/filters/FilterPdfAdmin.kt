package com.example.testing.recyclerview.bookapp.filters

import android.widget.Filter
import com.example.testing.recyclerview.bookapp.adapter.AdapterPdfAdmin
import com.example.testing.recyclerview.bookapp.models.ModelPdf

//filter pdf from recycleview | search pdf
class FilterPdfAdmin(
    val filterList: ArrayList<ModelPdf>,
    val adapterPdfAdmin: AdapterPdfAdmin
) :Filter(){
    override fun performFiltering(constraints: CharSequence?): FilterResults {
        var constraints = constraints
        val results = FilterResults()

        // value should not be null and empty
        if (constraints != null && constraints.isNotEmpty()) {
            //searched value isn't null or empty

            constraints = constraints.toString().uppercase()
            val filteredModel:ArrayList<ModelPdf> = ArrayList()
            for (i in 0 until filterList.size) {
                //validate
                if (filterList[i].title.uppercase().contains(constraints)) {
                    //add to filteredmodel
                    filteredModel.add(filterList[i])
                }
            }

            results.count = filteredModel.size
            results.values = filteredModel


        } else {
            //searched value is null or empty
            results.count = filterList.size
            results.values = filterList
        }

        return results
    }

    override fun publishResults(constraints: CharSequence?, results: FilterResults) {
        adapterPdfAdmin.pdfArrayList = results.values as ArrayList<ModelPdf>

        adapterPdfAdmin.notifyDataSetChanged()
    }
}