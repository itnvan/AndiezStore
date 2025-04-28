package com.example.andiezstore.user.model

data class User(
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String ?= null,
    val dateOfBirth: String = "",
    val hometown: String = "",
)
