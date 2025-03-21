package com.example.andiezstore.user.account

import com.example.andiezstore.user.model.Account

class AccountResponsitory(private val accountDao: AccountDao) {
    fun getAllAccount(): List<Account>? {
        return accountDao.getAllAccount()
    }
    fun insertAccount(account: Account){
        return accountDao.insertAccount(account)
    }

}