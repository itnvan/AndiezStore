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
import kotlinx.coroutines.flow.combine

class ClassroomFragment : Fragment() {
    private var _binding: FragmentClassroomBinding? = null
    private val binding get() = _binding!!
    private lateinit var classAdapter: ClassAdapter
    private lateinit var firebaseRefClassrooms: DatabaseReference
    private lateinit var firebaseRefSubjects: DatabaseReference
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
            firebaseRefClassrooms =
                FirebaseDatabase.getInstance().getReference("Users").child(uid).child("classrooms")
            firebaseRefSubjects = FirebaseDatabase.getInstance().getReference("Subjects")
                .child("quantityS") // Changed to the parent of quantityS
            observeClassrooms()
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeClassrooms() {
        lifecycleScope.launch {
            // Use combine to merge data from both sources
            fetchSubjectsFlow().combine (fetchClassroomsFlow()) { Subjects, classrooms ->
                classrooms.map { classroom ->
                    val quantityS = Subjects[classroom.subject] ?: 0 // Default to 0 if not found.
                    classroom.copy(quantityS = quantityS)
                }
                classrooms
            }.collectLatest { combinedClassrooms ->
                classroomList.clear()
                classroomList.addAll(combinedClassrooms)
                classAdapter.notifyDataSetChanged()
            }
            fetchClassroomsFlow().combine(fetchSubjectsFlow()) { classrooms, Subjects ->
                // Combine the data here.  For example, merge quantityS into the Classroom objects.
                //  Important:  Assume that the 'classrooms' list contains Classroom objects
                //  retrieved from the "Users/{uid}/classrooms" path.  The 'subjects'
                //  data is a map of subject IDs to quantityS values from "Subjects/{uid}".

                val combinedList = classrooms.map { classroom ->
                    //  Here, you need a way to match the classroom with the subject.
                    //  Let's assume your Classroom object has a 'subjectId' field.
                    val quantityS = Subjects[classroom.subject] ?: 0 // Default to 0 if not found.
                    classroom.copy(quantityS = quantityS) //  <--  Ensure Classroom has 'quantityS'
                }
                combinedList
            }.collectLatest { combinedClassrooms ->
                classroomList.clear()
                classroomList.addAll(combinedClassrooms)
                classAdapter.notifyDataSetChanged()
                if (combinedClassrooms.isEmpty() && isAdded) {
                    Toast.makeText(context, "No classrooms found for this user", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    // Flow to fetch classroom data
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

    // Flow to fetch subjects data (quantityS)
    private fun fetchSubjectsFlow() = callbackFlow<Map<String, Int>> {  // Changed to return Map
        val listener = firebaseRefSubjects.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //  Build a map of subjectId to quantityS.  Assume the key of each child
                //  of snapshot is the subjectId.  If quantityS is a direct child, this works.
                val subjectsMap = snapshot.children.associate { child ->
                    val quantityS = child.child("quantityS").getValue(Int::class.java) ?: 0
                    child.key!! to quantityS  // Use non-null assertion on child.key!!
                }
                trySend(subjectsMap).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching subjects: $error", error.toException())
                close(error.toException())
            }
        })
        awaitClose { firebaseRefSubjects.removeEventListener(listener) }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}