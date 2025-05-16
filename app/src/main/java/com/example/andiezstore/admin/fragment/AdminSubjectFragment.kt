package com.example.andiezstore.admin.fragment

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
import com.example.andiezstore.R
import com.example.andiezstore.databinding.FragmentAdminSubjectBinding
import com.google.firebase.database.*

class AdminSubjectFragment : Fragment() {
    private lateinit var binding: FragmentAdminSubjectBinding
    private lateinit var fromBottom: Animation
    private lateinit var toBottom: Animation
    private lateinit var rotateOpen: Animation
    private lateinit var rotateClose: Animation
    private var clicked = false
    private lateinit var subjectRef: DatabaseReference

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
    ): View? {
        binding = FragmentAdminSubjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Database reference
        subjectRef = FirebaseDatabase.getInstance().getReference("Admin").child("Subjects")

        // Set click listeners
        binding.btnMain.setOnClickListener {
            onMainButtonClicked()
        }
        binding.btnAdd.setOnClickListener {
            onAddButtonClicked()
        }
        binding.btnDelete.setOnClickListener {
            onDeleteButtonClicked()
        }
        binding.btnUpdate.setOnClickListener {
            onUpdateButtonClicked()
        }
        binding.edtName.setOnFocusChangeListener { _, hasFocus ->  // lắng nghe thay đổi focus của edtName
            if (!hasFocus) { // nếu không còn focus vào edtName
                loadSubjectDetails() // gọi hàm loadSubjectDetails
            }
        }
        setVisibility(clicked)
        setAnimation(clicked)
        setClickable(clicked)
    }

    private fun onMainButtonClicked() {
        clicked = !clicked
        setVisibility(clicked)
        setAnimation(clicked)
        setClickable(clicked)
    }

    private fun setVisibility(clicked: Boolean) {
        binding.btnAdd.visibility = if (!clicked) View.VISIBLE else View.INVISIBLE
        binding.btnDelete.visibility = if (!clicked) View.VISIBLE else View.INVISIBLE
        binding.btnUpdate.visibility = if (!clicked) View.VISIBLE else View.INVISIBLE
    }

    private fun setAnimation(clicked: Boolean) {
        binding.btnAdd.startAnimation(if (!clicked) fromBottom else toBottom)
        binding.btnDelete.startAnimation(if (!clicked) fromBottom else toBottom)
        binding.btnUpdate.startAnimation(if (!clicked) fromBottom else toBottom)
        binding.btnMain.startAnimation(if (!clicked) rotateOpen else rotateClose)
    }

    private fun setClickable(clicked: Boolean) {
        binding.btnAdd.isClickable = !clicked
        binding.btnDelete.isClickable = !clicked
        binding.btnUpdate.isClickable = !clicked
    }

    private fun onAddButtonClicked() {
        val subjectName = binding.edtName.text.toString().trim()
        val description = binding.edtBirthday.text.toString().trim() // Correct ID
        val subjectDay = binding.edtHometown.text.toString().trim() // Correct ID
        val star = binding.edtStar.text.toString().trim() // Correct ID
        val quantity = binding.edtQuantity.text.toString().trim() // Correct ID

        if (subjectName.isEmpty() || description.isEmpty() || subjectDay.isEmpty() || star.isEmpty()|| quantity.isEmpty()) {
            showError("Please fill in all fields.")
            if (star< 5.toString()){
                showError("Star must be less than 150")
            }
            else if (quantity<100.toString()){
                showError("Quantity must be less than 150")
            }
            return
        }

        val newSubject: HashMap<String, Any> = hashMapOf(
            "subjects" to subjectName,
            "description" to description,
            "timeStart" to subjectDay, // Correct key name
            "starCount" to star,
            "quantityE" to quantity
        )

        // Use push() to generate a unique ID for the new subject
        subjectRef.push().setValue(newSubject)
            .addOnSuccessListener {
                Log.d("Firebase", "Subject added successfully.")
                clearInputFields()
                showSuccess("Subject added!")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error adding subject: ${e.message}", e)
                showError("Failed to add subject: ${e.message}")
            }
    }

    private fun onUpdateButtonClicked() {
        val subjectName = binding.edtName.text.toString().trim()

        if (subjectName.isEmpty()) {
            showError("Please enter Subject Name to update.")
            return
        }

        subjectRef.orderByChild("subjects").equalTo(subjectName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Get the key of the first matching subject.
                        val subjectKey = snapshot.children.first().key

                        //update
                        val description = binding.edtBirthday.text.toString().trim()
                        val subjectDay = binding.edtHometown.text.toString().trim()
                        val star = binding.edtStar.text.toString().trim()
                        val quantity= binding.edtQuantity.text.toString().trim()

                        val updatedSubject: HashMap<String, Any> = hashMapOf(
                            "subjects" to subjectName,
                            "description" to description,
                            "timeStart" to subjectDay,
                            "starCount" to star,
                            "quantityE" to quantity
                        )
                        subjectRef.child(subjectKey!!).updateChildren(updatedSubject as Map<String, Any>)
                            .addOnSuccessListener {
                                Log.d("Firebase", "Subject updated successfully.")
                                clearInputFields()
                                showSuccess("Subject updated")
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firebase", "Error updating subject: ${e.message}", e)
                                showError("Failed to update subject: ${e.message}")
                            }


                    } else {
                        showError("Subject Name does not exist.")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Database Error: ${error.message}")
                }
            })


    }

    private fun onDeleteButtonClicked() {
        // Get the key of the subject to delete
        val subjectName = binding.edtName.text.toString().trim()
        if (subjectName.isEmpty()) {
            showError("Please enter Subject Name to delete.")
            return
        }
        subjectRef.orderByChild("subjects").equalTo(subjectName).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Get the key of the first matching subject.
                    val subjectKey = snapshot.children.first().key

                    subjectRef.child(subjectKey!!).removeValue()
                        .addOnSuccessListener {
                            Log.d("Firebase", "Subject deleted successfully.")
                            clearInputFields()
                            showSuccess("Subject Deleted")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firebase", "Error deleting subject: ${e.message}", e)
                            showError("Failed to delete subject: ${e.message}")
                        }
                } else {
                    showError("Subject Name does not exist.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Database Error: ${error.message}")
            }
        })


    }

    private fun clearInputFields() {
        binding.edtName.text?.clear()
        binding.edtBirthday.text?.clear()
        binding.edtHometown.text?.clear()
        binding.edtStar.text?.clear()
    }

    private fun showError(message: String) {
        // Use a Toast or a SnackBar to display the error message
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun loadSubjectDetails() {
        val subjectName = binding.edtName.text.toString().trim()
        if (subjectName.isEmpty()) return  // Không làm gì nếu không có subject name

        subjectRef.orderByChild("subjects").equalTo(subjectName).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val existingData = snapshot.children.first().value as Map<*, *>
                    // Hiện thị dữ liệu
                    binding.edtBirthday.setText(existingData["description"]?.toString() ?: "")
                    binding.edtHometown.setText(existingData["timeStart"]?.toString() ?: "")
                    binding.edtStar.setText(existingData["starCount"]?.toString() ?: "")
                } else {
                        showError("Subject Name does not exist.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Database Error: ${error.message}")
            }
        })
    }
}

