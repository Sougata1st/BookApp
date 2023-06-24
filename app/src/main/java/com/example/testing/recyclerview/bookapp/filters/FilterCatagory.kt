package com.example.testing.recyclerview.bookapp.filters

import android.widget.Filter
import com.example.testing.recyclerview.bookapp.adapter.AdapterCatagory
import com.example.testing.recyclerview.bookapp.models.ModelCategory

class FilterCatagory(//Arraylist in which we want to search
    private var filterList: ArrayList<ModelCategory>,

    //adapter in which filter needs to impleted
    private var adapterCatagory: AdapterCatagory
) : Filter() {

    override fun performFiltering(constraints: CharSequence?): FilterResults {


        var constraints = constraints
        val results = FilterResults()

        // value should not be null and empty
        if (constraints != null && constraints.isNotEmpty()) {
            //searched value isn't null or empty
            constraints = constraints.toString().uppercase()
            val filteredModel:ArrayList<ModelCategory> = ArrayList()
            for (i in 0 until filterList.size) {
                //validate
                if (filterList[i].catagory.uppercase().contains(constraints)) {
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
        //apply filter changes
        adapterCatagory.categoryArrayList =
            results.values as ArrayList<ModelCategory>
        //notify changes to adapter
        adapterCatagory.notifyDataSetChanged()
    }

}