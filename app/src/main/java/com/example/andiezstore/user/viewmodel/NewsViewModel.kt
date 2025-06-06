package com.example.andiezstore.user.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.andiezstore.user.model.News // Ensure correct import for News model
import com.google.firebase.database.*

class NewsViewModel : ViewModel() {

    // For list of news summaries in HomeFragment
    private val _newsList = MutableLiveData<List<News>>()
    val newsList: LiveData<List<News>> = _newsList

    // For a single selected news item in NewsDetailFragment
    private val _selectedNewsDetail = MutableLiveData<News?>()
    val selectedNewsDetail: LiveData<News?> = _selectedNewsDetail

    // NEW: LiveData for suggested news list
    private val _suggestedNews = MutableLiveData<List<News>>()
    val suggestedNews: LiveData<List<News>> = _suggestedNews

    private val _isLoadingList = MutableLiveData<Boolean>()
    val isLoadingList: LiveData<Boolean> = _isLoadingList

    private val _isLoadingDetail = MutableLiveData<Boolean>()
    val isLoadingDetail: LiveData<Boolean> = _isLoadingDetail

    // NEW: LiveData for loading state of suggested news
    private val _isLoadingSuggestions = MutableLiveData<Boolean>()
    val isLoadingSuggestions: LiveData<Boolean> = _isLoadingSuggestions

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val newsDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Admin/News")


    fun fetchAllNews() {
        _isLoadingList.value = true
        _error.value = null
        newsDatabaseReference.orderByChild("date") // Order by date, assuming "date" field exists
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val items = mutableListOf<News>()
                    if (snapshot.exists()) {
                        for (newsSnapshot in snapshot.children) {
                            val newsItem = newsSnapshot.getValue(News::class.java)
                            newsItem?.let {
                                it.id = newsSnapshot.key // Assign Firebase node key as ID
                                items.add(it)
                            }
                        }
                    }
                    _newsList.value = items.reversed() // Optional: Display newest news first
                    _isLoadingList.value = false
                    Log.d("NewsViewModel", "Successfully fetched ${items.size} news items.")
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("NewsViewModel", "Failed to read news list.", databaseError.toException())
                    _error.value = "Failed to load news list: ${databaseError.message}"
                    _isLoadingList.value = false
                }
            })
    }

    /**
     * Fetches a single news item by its ID for NewsDetailFragment.
     * Also triggers fetching of suggested news after main news is loaded.
     */
    fun fetchNewsDetailById(newsId: String) {
        _isLoadingDetail.value = true
        _error.value = null
        _selectedNewsDetail.value = null // Clear previous selection

        newsDatabaseReference.child(newsId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val newsItem = snapshot.getValue(News::class.java)
                    newsItem?.id = snapshot.key // Assign Firebase key as ID
                    _selectedNewsDetail.value = newsItem
                    Log.d("NewsViewModel", "Successfully fetched news detail for ID: $newsId")
                    // After fetching main news, fetch suggestions
                    fetchSuggestedNews(newsId)
                } else {
                    _error.value = "News item not found for ID: $newsId."
                    Log.e("NewsViewModel", "News item not found for ID: $newsId.")
                    // If main news not found, clear suggestions too
                    _suggestedNews.value = emptyList()
                    _isLoadingSuggestions.value = false
                }
                _isLoadingDetail.value = false
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("NewsViewModel", "Failed to read news detail.", databaseError.toException())
                _error.value = "Failed to load news detail: ${databaseError.message}"
                _isLoadingDetail.value = false
                // On error, clear suggestions
                _suggestedNews.value = emptyList()
                _isLoadingSuggestions.value = false
            }
        })
    }

    /**
     * NEW: Fetches a list of suggested news items from Firebase.
     * Excludes the current news item from the suggestions.
     * Fetches a limited number of random or latest items.
     */
    fun fetchSuggestedNews(currentNewsId: String) {
        _isLoadingSuggestions.value = true
        _suggestedNews.value = emptyList() // Clear previous suggestions

        newsDatabaseReference.limitToLast(5) // Fetch the last 5 news items as suggestions
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val suggestions = mutableListOf<News>()
                    if (snapshot.exists()) {
                        for (newsSnapshot in snapshot.children) {
                            val newsItem = newsSnapshot.getValue(News::class.java)
                            newsItem?.let {
                                it.id = newsSnapshot.key // Assign Firebase node key as ID
                                // Exclude the current news item from suggestions
                                if (it.id != currentNewsId) {
                                    suggestions.add(it)
                                }
                            }
                        }
                    }
                    // Optional: Shuffle suggestions to show different items each time
                    _suggestedNews.value = suggestions.shuffled()
                    _isLoadingSuggestions.value = false
                    Log.d("NewsViewModel", "Successfully fetched ${suggestions.size} suggested news items (excluding current).")
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("NewsViewModel", "Failed to read suggested news.", databaseError.toException())
                    // Do not set _error.value here to avoid overwriting main detail error
                    // You might want a separate LiveData for suggestion-specific errors
                    _isLoadingSuggestions.value = false
                    _suggestedNews.value = emptyList() // Clear suggestions on error
                }
            })
    }

    /**
     * Clears the selected news detail and suggested news data.
     * Useful when the fragment view is destroyed to prevent memory leaks.
     */
    fun clearSelectedNewsDetail() {
        _selectedNewsDetail.value = null
        _suggestedNews.value = emptyList() // Also clear suggestions
        Log.d("NewsViewModel", "Cleared selected news detail and suggestions.")
    }
}