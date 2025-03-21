package com.example.andiezstore.user.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andiezstore.user.account.AccountResponsitory
import com.example.andiezstore.user.model.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher

class AccountViewModel(private val accountResponsitory: AccountResponsitory) : ViewModel() {
    private val _fetchData = MutableLiveData<List<Account>?>()
    val fetchData:LiveData<List<Account>?> get()=_fetchData
    fun insertAccount(account: Account){
        viewModelScope.launch(Dispatchers.IO) {
            fetchAccount()
            accountResponsitory.insertAccount(account)
        }
    }
    fun getAllAccount() {
        viewModelScope.launch(Dispatchers.IO) {
            fetchAccount()
            accountResponsitory.getAllAccount()
        }
    }
    fun fetchAccount(){
        viewModelScope.launch {
            val data=accountResponsitory.getAllAccount()
            delay(1000)
            withContext(Dispatchers.Main){
                _fetchData.value=data
            }
        }
    }
}