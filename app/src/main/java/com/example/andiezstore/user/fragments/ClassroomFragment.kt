package com.example.andiezstore.user.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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

class ClassroomFragment : Fragment() {
    private var _binding: FragmentClassroomBinding? = null
    private val binding get() = _binding!!
    private lateinit var classAdapter: ClassAdapter
    private lateinit var firebaseRefClassrooms: DatabaseReference
    private val classroomList = mutableListOf<Classroom>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClassroomBinding.inflate(inflater, container, false)
        binding.rcvClass.layoutManager = LinearLayoutManager(context)
        classAdapter = ClassAdapter(classroomList)
        binding.rcvClass.adapter = classAdapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            firebaseRefClassrooms = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Classrooms")
            observeClassrooms()
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeClassrooms() {
        lifecycleScope.launch {
            fetchClassroomsFlow().collectLatest { classrooms ->
                classroomList.clear()
                classroomList.addAll(classrooms)
                classAdapter.notifyDataSetChanged()
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
        _binding = null
    }
}