package com.example.andiezstore.user.model

data class News(
    val title:String?=null,
    val decription:String?=null,
    val image:String?=null,
    val date:String?=null,
    var firebaseKey: String? = null // Thêm trường này để lưu khóa Firebase
)
