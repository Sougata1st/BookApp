package com.example.testing.recyclerview.bookapp

import android.widget.Filter

class FilterPdfUser(
    val filterList:ArrayList<ModelPdf>,
    val adapterPdfUser: AdapterPdfUser
): Filter() {
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

    override fun publishResults(constraints: CharSequence?, results: FilterResults?) {
       adapterPdfUser.pdfArrayList = results?.values as ArrayList<ModelPdf>
        adapterPdfUser.notifyDataSetChanged()
    }
}