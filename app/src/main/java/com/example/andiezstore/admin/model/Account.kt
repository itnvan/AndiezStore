package com.example.andiezstore.admin.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties // Optional: Helps if Firebase sends extra fields

@IgnoreExtraProperties
data class Account(
    val account: String? = null,
    val password: String? = null,
    @Exclude @JvmField // Exclude from Firebase database serialization
    var firebaseKey: String? = null // For when reading accounts from a list (not directly used for login here)
)