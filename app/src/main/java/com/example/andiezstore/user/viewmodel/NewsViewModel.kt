package com.example.andiezstore.user.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.andiezstore.user.model.News // Đảm bảo import đúng model News
import com.google.firebase.database.*

class NewsViewModel : ViewModel() {

    // For list of news summaries in HomeFragment
    private val _newsList = MutableLiveData<List<News>>()
    val newsList: LiveData<List<News>> = _newsList

    // For a single selected news item in NewsDetailFragment
    private val _selectedNewsDetail = MutableLiveData<News?>()
    val selectedNewsDetail: LiveData<News?> = _selectedNewsDetail

    private val _isLoadingList = MutableLiveData<Boolean>()
    val isLoadingList: LiveData<Boolean> = _isLoadingList

    private val _isLoadingDetail = MutableLiveData<Boolean>()
    val isLoadingDetail: LiveData<Boolean> = _isLoadingDetail

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val newsDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Admin/News")

    // Fetches all news for HomeFragment
    fun fetchAllNews() {
        _isLoadingList.value = true
        _error.value = null
        newsDatabaseReference.orderByChild("date")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val items = mutableListOf<News>()
                    if (snapshot.exists()) {
                        for (newsSnapshot in snapshot.children) {
                            val newsItem = newsSnapshot.getValue(News::class.java)
                            newsItem?.let {
                                it.id = newsSnapshot.key // Gán key của Firebase node làm id
                                items.add(it)
                            }
                        }
                    }
                    _newsList.value = items.reversed() // Hiển thị tin mới nhất lên đầu (tùy chọn)
                    _isLoadingList.value = false
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("NewsViewModel", "Failed to read news list.", databaseError.toException())
                    _error.value = "Failed to load news list: ${databaseError.message}"
                    _isLoadingList.value = false
                }
            })
    }

    // Fetches a single news item by its ID for NewsDetailFragment
    fun fetchNewsDetailById(newsId: String) {
        _isLoadingDetail.value = true
        _error.value = null
        _selectedNewsDetail.value = null // Xóa lựa chọn cũ
        newsDatabaseReference.child(newsId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val newsItem = snapshot.getValue(News::class.java)
                    newsItem?.id = snapshot.key
                    _selectedNewsDetail.value = newsItem
                } else {
                    _error.value = " News item not found."
                }
                _isLoadingDetail.value = false
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("NewsViewModel", "Failed to read news detail.", databaseError.toException())
                _error.value = "Failed to load news detail: ${databaseError.message}"
                _isLoadingDetail.value = false
            }
        })
    }

    fun clearSelectedNewsDetail() {
        _selectedNewsDetail.value = null
    }
}