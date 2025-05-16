package com.example.andiezstore.user.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.andiezstore.databinding.FragmentSubjectBinding
import com.example.andiezstore.user.adapter.SubjectAdapter
import com.example.andiezstore.user.model.Subject
import com.google.firebase.database.*

class SubjectFragment : Fragment() {
    private lateinit var binding: FragmentSubjectBinding
    private lateinit var subjectAdapter: SubjectAdapter
    private lateinit var adminDatabase: DatabaseReference
    private val listSubject = mutableListOf<Subject>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSubjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Database
        adminDatabase = FirebaseDatabase.getInstance().getReference("Admin").child("Subjects")

        // Initialize RecyclerView and Adapter, pass the update callback
        subjectAdapter = SubjectAdapter(listSubject, requireContext()) { subjectKey, updatedSubject ->
            updateSubject(subjectKey, updatedSubject)
        }
        binding.rcvSubject.adapter = subjectAdapter
        binding.rcvSubject.layoutManager = LinearLayoutManager(requireContext())

        // Fetch subject data from Firebase
        getSubjectData()
    }

    private fun getSubjectData() {
        adminDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listSubject.clear()
                if (snapshot.exists()) {
                    for (childSnapshot in snapshot.children) {
                        val subject = Subject(
                            subject = childSnapshot.child("subjects").getValue(String::class.java),
                            description = childSnapshot.child("description").getValue(String::class.java),
                            timeStart = childSnapshot.child("timeStart").getValue(String::class.java),
                            tvStar = childSnapshot.child("starCount").getValue(String::class.java),
                            quantityS = childSnapshot.child("quantityS").getValue(String::class.java)?.toIntOrNull(),
                            quantityE = childSnapshot.child("quantityE").getValue(String::class.java)?.toIntOrNull()
                        )
                        listSubject.add(subject)
                    }
                    subjectAdapter.notifyDataSetChanged()
                } else {
                    Log.d("SubjectFragment", "No subjects found")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SubjectFragment", "Error getting data: ${error.message}")
                Toast.makeText(requireContext(), "Failed to load subjects: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun updateSubject(subjectKey: String, updatedSubject: Subject) {
        // Update the subject data in Firebase
        adminDatabase.child(subjectKey).setValue(updatedSubject) // Use setValue for the whole object
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Subject updated successfully", Toast.LENGTH_SHORT).show()
                // No need to call getSubjectData() here.  The ValueEventListener will automatically update the list.
            }
            .addOnFailureListener { e ->
                Log.e("SubjectFragment", "Error updating subject: ${e.message}", e)
                Toast.makeText(requireContext(), "Failed to update subject: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
