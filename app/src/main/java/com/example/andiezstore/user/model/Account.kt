package com.example.andiezstore.user.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "AccountTable")
data class Account(
    @PrimaryKey(autoGenerate = true) val id:Int=0,
    var name:String,
    var email:String,
    var pass:String
)
