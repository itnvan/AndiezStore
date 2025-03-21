package com.example.andiezstore.user.account

import androidx.room.Insert
import androidx.room.Query
import com.example.andiezstore.user.model.Account

interface AccountDao {
    @Query("SELECT*FROM AccountTable")
    fun getAllAccount():List<Account>?
    @Insert
    fun insertAccount(account: Account)

}