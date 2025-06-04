package com.example.andiezstore.user.model

data class News(
    var id:String?=null,
    val title:String?=null,
    val decription:String?=null,
    val image:String?=null,
    val time:String?=null,
    val date:String?=null,
    val author:String?=null,
    var firebaseKey: String? = null // Thêm trường này để lưu khóa Firebase
)
