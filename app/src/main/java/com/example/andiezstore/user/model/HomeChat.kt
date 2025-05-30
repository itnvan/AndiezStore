package com.example.andiezstore.user.model

data class HomeChat(
    val name: String? = null,
    val presentText: String? = null,
    val userId: String? = null, // Đảm bảo kiểu dữ liệu là String?
    val profileImageUrl: String? = null,
    val otherUserId: String? = null
)



