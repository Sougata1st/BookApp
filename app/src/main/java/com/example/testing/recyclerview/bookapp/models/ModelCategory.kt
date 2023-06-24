package com.example.testing.recyclerview.bookapp.models

class ModelCategory {
    //variables , must match as in firebase
    var id:String = ""
    var catagory = ""
    var timestamp:Long = 0
    var uid:String = ""

    //empty constrctor required by firebase
    constructor()

    //peramitarized constructor
    constructor(id: String, catagory: String, timestamp: Long, uid: String) {
        this.id = id
        this.catagory = catagory
        this.timestamp = timestamp
        this.uid = uid
    }
}