package com.example.andiezstore.admin.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContentProviderCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.andiezstore.R
import com.example.andiezstore.admin.adapter.ClassroomAdminAdapter
import com.example.andiezstore.databinding.FragmentAdminClassroomBinding
import com.example.andiezstore.databinding.FragmentClassroomBinding
import com.example.andiezstore.user.adapter.ClassAdapter
import com.example.andiezstore.user.model.Classroom
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdminClassroomFragment : Fragment() {
    private lateinit var fromBottom:Animation
    private lateinit var toBottom: Animation
    private lateinit var rotateOpen: Animation
    private lateinit var rotateClose: Animation
    private lateinit var classroomAdminAdapter: ClassroomAdminAdapter
    private lateinit var firebaseRefClassrooms: DatabaseReference
    private var binding: FragmentAdminClassroomBinding ?= null
    private val classroomList = mutableListOf<Classroom>()
    private val _binding get() = binding!!
    private var clicked = false
    override fun onAttach(context: Context) {
        super.onAttach(context)
        fromBottom = AnimationUtils.loadAnimation(context, R.anim.from_bottom_anim)
        toBottom = AnimationUtils.loadAnimation(context, R.anim.to_bottom_anim)
        rotateOpen = AnimationUtils.loadAnimation(context, R.anim.rotate_open_anim)
        rotateClose = AnimationUtils.loadAnimation(context, R.anim.rotate_close_anim)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminClassroomBinding.inflate(inflater, container, false)
        binding?.rcvClass?.layoutManager = LinearLayoutManager(context)
        classroomAdminAdapter = ClassroomAdminAdapter(classroomList)
        binding?.rcvClass?.adapter = classroomAdminAdapter
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            firebaseRefClassrooms = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Classrooms")
            observeClassrooms()
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }
        binding = FragmentAdminClassroomBinding.inflate(layoutInflater)
        binding?.btnMain?.setOnClickListener {
            onAddButtonClicked()
        }
        binding?.btnAdd?.setOnClickListener {
            // Xử lý sự kiện click nút Add
        }
        binding?.btnDelete?.setOnClickListener {
            onDeleteButtonClicked()
        }
        binding?.btnUpdate?.setOnClickListener {
            onUpdateButtonClicked()
        }
        return binding?.root!!
    }

    private fun onUpdateButtonClicked() {
        // Xử lý sự kiện click nút Update
    }

    private fun onDeleteButtonClicked() {
        // Xử lý sự kiện click nút Delete
    }

    private fun onAddButtonClicked() {
        clicked = !clicked
        setVisibility(clicked)
        setAnimation(clicked)
        setClickable(clicked)
    }

    private fun setVisibility(clicked: Boolean) {
        binding?.btnAdd?.visibility = if (!clicked) View.VISIBLE else View.INVISIBLE
        binding?.btnDelete?.visibility = if (!clicked) View.VISIBLE else View.INVISIBLE
        binding?.btnUpdate?.visibility = if (!clicked) View.VISIBLE else View.INVISIBLE
    }

    private fun setAnimation(clicked: Boolean) {
        binding?.btnAdd?.startAnimation(if (!clicked) fromBottom else toBottom)
        binding?.btnDelete?.startAnimation(if (!clicked) fromBottom else toBottom)
        binding?.btnUpdate?.startAnimation(if (!clicked) fromBottom else toBottom)
        binding?.btnMain?.startAnimation(if (!clicked) rotateOpen else rotateClose)
    }

    private fun setClickable(clicked: Boolean) {
        binding?.btnAdd?.isClickable = !clicked
        binding?.btnDelete?.isClickable = !clicked
        binding?.btnUpdate?.isClickable = !clicked
        // Tùy chọn: Nếu bạn muốn btnMain luôn clickable, bỏ qua dòng này
        // binding.btnMain.isClickable = true
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun observeClassrooms() {
        lifecycleScope.launch {
            fetchClassroomsFlow().collectLatest { classrooms ->
                classroomList.clear()
                classroomList.addAll(classrooms)
                classroomAdminAdapter.notifyDataSetChanged()
                if (classrooms.isEmpty() && isAdded) {
                    Toast.makeText(context, "No classrooms found for this user", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchClassroomsFlow() = callbackFlow<List<Classroom>> {
        val listener = firebaseRefClassrooms.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                val classrooms = snapshot.children.mapNotNull { it.getValue(Classroom::class.java) }
                trySend(classrooms).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching classrooms: $error", error.toException())
                close(error.toException())
            }
        })
        awaitClose { firebaseRefClassrooms.removeEventListener(listener) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}