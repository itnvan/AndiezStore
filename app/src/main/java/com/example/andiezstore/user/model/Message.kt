package com.example.andiezstore.user.model

data class Message(
    var sender: String? = null,  // Người gửi
    var text: String? = null,    // Nội dung tin nhắn
    var timestamp: Long? = null  // Thời gian gửi
)
