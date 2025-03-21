package com.example.andiezstore.ui

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.andiezstore.user.account.AccountDao
import com.example.andiezstore.user.fragments.RegistedFragment
import com.example.andiezstore.user.model.Account

@Database(entities = [Account::class], version = 1, exportSchema = true)
abstract class DatabaseRoom : RoomDatabase() {
    abstract fun accountDao(): AccountDao
//    abstract fun subjectsDao(): SubjectsDao

    companion object {
        @Volatile
        private var INSTANCE: DatabaseRoom? = null
        fun getDatabase(fragment: RegistedFragment): DatabaseRoom {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    fragment.requireContext(),
                    DatabaseRoom::class.java,
                    name = "account_database"
                ).allowMainThreadQueries().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}