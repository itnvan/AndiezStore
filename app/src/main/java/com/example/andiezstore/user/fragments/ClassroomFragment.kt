package com.example.andiezstore.user.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
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

class ClassroomFragment : Fragment() {
    private var _binding: FragmentClassroomBinding? = null
    private val binding get() = _binding!!
    private lateinit var classAdapter: ClassAdapter
    private var firebaseRefClassrooms: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("Users")
    private var firebaseRefSubjects: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("Subjects")
    private val classroomList = mutableListOf<Classroom>()
    private var subjectQuantityMap =
        mutableMapOf<String, Int>()  // Map to store subject quantities, key is subjectName
    private val subjectKeyMap = mutableMapOf<String, String>() // Map to store subject keys, key is subjectName

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClassroomBinding.inflate(inflater, container, false)
        binding.rcvClass.layoutManager = LinearLayoutManager(context)
        classAdapter = ClassAdapter(classroomList)
        binding.rcvClass.adapter = classAdapter //set adapter here

        val auth = FirebaseAuth.getInstance().currentUser?.uid
        if (auth != null) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            getCurrentUserName(uid.toString())
            fetchSubjectQuantity(uid.toString()) // Fetch subject quantities first.
            fetchClassrooms(uid.toString())    //Then fetch classrooms
        } else {
            binding.tvName.text = "Not logged in"
        }

        return binding.root
    }

    private fun fetchSubjectQuantity(uid: String) {
        firebaseRefSubjects
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    subjectQuantityMap.clear()
                    for (childSnapshot in snapshot.children) {
                        val subjectName = childSnapshot.key // Use the key as subjectName
                        val quantityS = childSnapshot.child("quantityS").getValue(Int::class.java)
                        if (subjectName != null && quantityS != null) {
                            subjectQuantityMap[subjectName] =
                                quantityS // Store with subjectName
                        }
                    }
                    // Notify adapter only once after ALL data is fetched
                    updateClassroomList()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(
                        "ClassroomFragment",
                        "Error fetching subject quantity: ${error.message}"
                    )
                    Toast.makeText(
                        requireContext(),
                        "Failed to load subject quantity: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun getCurrentUserName(uid: String) {
        firebaseRefClassrooms.child(uid).child("information").child("name")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userName = snapshot.getValue(String::class.java)
                        binding.tvName.text = userName
                    } else {
                        binding.tvName.text = "User name not found"
                    }
                }

                @SuppressLint("SetTextI18n")
                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Failed to read user name.", error.toException())
                    binding.tvName.text = "Error loading user name"
                }
            })
    }

    private fun fetchClassrooms(uid: String) {
        firebaseRefClassrooms.child(uid).child("classrooms")
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    classroomList.clear() // Clear the list before adding new data
                    for (childSnapshot in snapshot.children) {
                        val subjectName = childSnapshot.child("subject").getValue(String::class.java)
                        val description =
                            childSnapshot.child("description").getValue(String::class.java)
                        val timeStart = childSnapshot.child("timeStart").getValue(String::class.java)
                        val starCount =
                            childSnapshot.child("starCount").getValue(String::class.java) ?: 0
                        val quantityE =
                            childSnapshot.child("quantityE").getValue(Int::class.java) ?: 0
                        if (subjectName != null) {
                            val classroom = Classroom(
                                subject=subjectName,
                                description,
                                timeStart,
                                tvStar = starCount.toString(),
                                quantityE = quantityE
                            )
                            classroomList.add(classroom)
                        }
                    }
                    updateClassroomList()//update the classroom list and call notifyDataSetChanged
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ClassroomFragment", "Error fetching classrooms: ${error.message}")
                    Toast.makeText(
                        requireContext(),
                        "No classrooom available",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun updateClassroomList() {
        // After fetching both classrooms and subject quantities, update the classroomList
        for (classroom in classroomList) {
            val quantityS = subjectQuantityMap[classroom.subject] ?: 0
            classroom.quantityS = quantityS
        }
        classAdapter.notifyDataSetChanged() // Notify the adapter after updating the list
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
