package com.example.testing.recyclerview.bookapp

class ModelPdf {
    //variables
    var uid:String = ""
    var id:String = ""
    var title:String = ""
    var description:String = ""
    var categoryId:String = ""
    var url:String =""
    var timestamp:Long = 0
    var viewsCount:Long = 0
    var downloadsCouunt:Long = 0

    //empty constructor required for firebase
    constructor()

    //peramitarized constructor
    constructor(
        uid: String,
        id: String,
        title: String,
        description: String,
        categoryId: String,
        url: String,
        timestamp: Long,
        viewsCount: Long,
        downloadsCouunt: Long
    ) {
        this.uid = uid
        this.id = id
        this.title = title
        this.description = description
        this.categoryId = categoryId
        this.url = url
        this.timestamp = timestamp
        this.viewsCount = viewsCount
        this.downloadsCouunt = downloadsCouunt
    }
}