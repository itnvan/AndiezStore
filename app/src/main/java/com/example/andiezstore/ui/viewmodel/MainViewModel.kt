package com.example.andiezstore.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.andiezstore.ui.model.SliderModel
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainViewModel : ViewModel(){
    private val firebaseDatabase=FirebaseDatabase.getInstance()
    private val banner=MutableLiveData<List<SliderModel>>()
    val banners:LiveData<List<SliderModel>> = banner
    fun loadBanners(){
        val ref=firebaseDatabase.getReference("banner")
        ref.addChildEventListener(object:ValueEventListener, ChildEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists= mutableListOf<SliderModel>()
                for(childSnapshot in snapshot.children){
                    val list=childSnapshot.getValue((SliderModel::class.java))
                    if (list!=null){
                        lists.add(list)
                    }
                }
                banner.value=lists
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}