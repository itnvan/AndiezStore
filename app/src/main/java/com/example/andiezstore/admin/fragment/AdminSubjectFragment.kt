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
    private lateinit var adminSubjectRef: DatabaseReference
    private lateinit var userSubjectRef: DatabaseReference
    private var currentSubjectKey: String? = null
    private var currentSubjectNameOnFirebase: String? = null

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

        adminSubjectRef = FirebaseDatabase.getInstance().getReference("Admin").child("Subjects")
        userSubjectRef = FirebaseDatabase.getInstance()
            .getReference("Subjects")

        binding.btnMain.setOnClickListener { onMainButtonClicked() }
        binding.btnAdd.setOnClickListener { onAddButtonClicked() }
        binding.btnDelete.setOnClickListener { onDeleteButtonClicked() }
        binding.btnUpdate.setOnClickListener { onUpdateButtonClicked() }

        binding.edtName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                loadSubjectDetails()
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
        val description = binding.edtBirthday.text.toString().trim()
        val subjectDay = binding.edtHometown.text.toString().trim()
        val star = binding.edtStar.text.toString().trim()
        val quantity = binding.edtQuantity.text.toString().trim()

        if (subjectName.isEmpty() || description.isEmpty() || subjectDay.isEmpty() || star.isEmpty() || quantity.isEmpty()) {
            showError("Please fill in all fields.")
            return
        }

        val starFloat = star.toFloatOrNull()
        val quantityInt = quantity.toIntOrNull() // Vẫn cần parse để validate cho adminSubjectRef

        if (starFloat == null || starFloat > 5f) {
            showError("Number of stars must be a number and less than or equal to 5.")
            return
        }
        if (quantityInt == null || quantityInt > 150) {
            showError("Quantity must be a number and less than or equal to 150.")
            return
        }

        adminSubjectRef.orderByChild("subjects").equalTo(subjectName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        showError("Subject '$subjectName' existed.")
                    } else {
                        val newAdminSubject: HashMap<String, Any> = hashMapOf(
                            "subjects" to subjectName,
                            "description" to description,
                            "timeStart" to subjectDay,
                            "starCount" to star,
                            "quantityE" to quantity // Lưu quantityE vào Admin/Subjects
                        )

                        adminSubjectRef.push().setValue(newAdminSubject)
                            .addOnSuccessListener {
                                Log.d("Firebase", "Subject added to Admin/Subjects successfully.")

                                // Thay đổi ở đây: SET CỨNG quantityS LÀ 0
                                val userSubjectData = hashMapOf("quantityS" to 0) // <--- Thay đổi ở đây
                                userSubjectRef.child(subjectName).setValue(userSubjectData)
                                    .addOnSuccessListener {
                                        Log.d("Firebase", "Subject added to Subjects with quantityS=0 successfully.")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Firebase", "Error adding subject to Subjects: ${e.message}", e)
                                    }
                                showSuccess("New subject added successfully!")
                                clearInputFields()
                            }
                            .addOnFailureListener { e ->
                                Log.e(
                                    "Firebase",
                                    "Error adding subject to Admin/Subjects: ${e.message}",
                                    e
                                )
                                showError("Failed to add new subject: ${e.message}")
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Database Error checking existence: ${error.message}")
                    showError("Error checking existence: ${error.message}")
                }
            })
    }

    private fun onUpdateButtonClicked() {
        val newSubjectName = binding.edtName.text.toString().trim()
        val description = binding.edtBirthday.text.toString().trim()
        val subjectDay = binding.edtHometown.text.toString().trim()
        val star = binding.edtStar.text.toString().trim()
        val quantity = binding.edtQuantity.text.toString().trim() // Vẫn cần lấy để validate cho adminSubjectRef

        if (newSubjectName.isEmpty()) {
            showError("Please enter a subject name to update.")
            return
        }
        if (description.isEmpty() || subjectDay.isEmpty() || star.isEmpty() || quantity.isEmpty()) {
            showError("Please fill in all fields.")
            return
        }

        val starFloat = star.toFloatOrNull()
        val quantityInt = quantity.toIntOrNull() // Vẫn cần parse để validate cho adminSubjectRef

        if (starFloat == null || starFloat > 5f) {
            showError("Number of stars must be a number and less than or equal to 5.")
            return
        }
        if (quantityInt == null || quantityInt > 150) {
            showError("Quantity must be a number and less than or equal to 150.")
            return
        }

        adminSubjectRef.orderByChild("subjects").equalTo(newSubjectName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var foundExistingSubjectWithNewName = false

                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            if (child.key != currentSubjectKey) {
                                foundExistingSubjectWithNewName = true
                                break
                            }
                        }
                    }

                    if (foundExistingSubjectWithNewName) {
                        showError("Subject name '$newSubjectName' already exists for another subject.")
                        return
                    }

                    if (currentSubjectKey == null) {
                        showError("Please load a subject's details first by entering its name and losing focus from the input field.")
                        return
                    }

                    val updatedAdminSubject: HashMap<String, Any> = hashMapOf(
                        "subjects" to newSubjectName,
                        "description" to description,
                        "timeStart" to subjectDay,
                        "starCount" to star,
                        "quantityE" to quantity // Cập nhật quantityE vào Admin/Subjects
                    )

                    adminSubjectRef.child(currentSubjectKey!!).updateChildren(updatedAdminSubject)
                        .addOnSuccessListener {
                            Log.d("Firebase", "Subject updated in Admin/Subjects successfully.")

                            val updates = HashMap<String, Any?>()
                            // Thay đổi ở đây: SET CỨNG quantityS LÀ 0
                            val userSubjectData = hashMapOf("quantityS" to 0) // <--- Thay đổi ở đây

                            if (currentSubjectNameOnFirebase != null && newSubjectName != currentSubjectNameOnFirebase) {
                                updates["Subjects/${currentSubjectNameOnFirebase}"] = null
                                updates["Subjects/$newSubjectName"] = userSubjectData
                            } else {
                                updates["Subjects/$newSubjectName"] = userSubjectData
                            }

                            FirebaseDatabase.getInstance().reference.updateChildren(updates)
                                .addOnSuccessListener {
                                    Log.d("Firebase", "Subject name and quantityS=0 synced in Subjects successfully.")
                                    showSuccess("Môn học đã được cập nhật!")
                                    clearInputFields()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firebase", "Error syncing subject name and quantityS=0 in Subjects: ${e.message}", e)
                                    showError("Môn học đã cập nhật (admin), nhưng lỗi đồng bộ cho người dùng: ${e.message}")
                                    clearInputFields()
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e(
                                "Firebase",
                                "Error updating subject in Admin/Subjects: ${e.message}",
                                e
                            )
                            showError("Failed to update subject: ${e.message}")
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Database Error: ${error.message}")
                    showError("Failed to update subject: ${error.message}")
                }
            })
    }

    private fun onDeleteButtonClicked() {
        val subjectName = binding.edtName.text.toString().trim()
        if (subjectName.isEmpty()) {
            showError("Please enter a subject name to delete.")
            return
        }

        adminSubjectRef.orderByChild("subjects").equalTo(subjectName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val subjectKey = snapshot.children.first().key

                        adminSubjectRef.child(subjectKey!!).removeValue()
                            .addOnSuccessListener {
                                Log.d(
                                    "Firebase",
                                    "Subject deleted from Admin/Subjects successfully."
                                )

                                userSubjectRef.child(subjectName).removeValue()
                                    .addOnSuccessListener {
                                        Log.d(
                                            "Firebase",
                                            "Subject deleted from Subjects successfully."
                                        )
                                        showSuccess("Môn học đã được xóa!")
                                        clearInputFields()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(
                                            "Firebase",
                                            "Error deleting subject from Subjects: ${e.message}",
                                            e
                                        )
                                        showError("Failed to delete subject (user): ${e.message}")
                                    }
                            }
                            .addOnFailureListener { e ->
                                Log.e(
                                    "Firebase",
                                    "Error deleting subject from Admin/Subjects: ${e.message}",
                                    e
                                )
                                showError("Failed to delete subject (admin): ${e.message}")
                            }
                    } else {
                        showError("Subject '$subjectName' is not found.")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Database Error: ${error.message}")
                    showError("Error deleting subject: ${error.message}")
                }
            })
    }

    private fun clearInputFields() {
        binding.edtName.text?.clear()
        binding.edtBirthday.text?.clear()
        binding.edtHometown.text?.clear()
        binding.edtStar.text?.clear()
        binding.edtQuantity.text?.clear()
        currentSubjectKey = null
        currentSubjectNameOnFirebase = null
        binding.edtName.requestFocus()
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun loadSubjectDetails() {
        val subjectName = binding.edtName.text.toString().trim()
        if (subjectName.isEmpty()) {
            clearInputFields()
            return
        }

        adminSubjectRef.orderByChild("subjects").equalTo(subjectName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val subjectData = snapshot.children.first()
                        val existingData = subjectData.value as Map<*, *>
                        currentSubjectKey = subjectData.key
                        currentSubjectNameOnFirebase = existingData["subjects"]?.toString()

                        binding.edtBirthday.setText(existingData["description"]?.toString() ?: "")
                        binding.edtHometown.setText(existingData["timeStart"]?.toString() ?: "")
                        binding.edtStar.setText(existingData["starCount"]?.toString() ?: "")
                        binding.edtQuantity.setText(
                            existingData["quantityE"]?.toString() ?: ""
                        )
                    } else {
                        binding.edtBirthday.text?.clear()
                        binding.edtHometown.text?.clear()
                        binding.edtStar.text?.clear()
                        binding.edtQuantity.text?.clear()
                        currentSubjectKey = null
                        currentSubjectNameOnFirebase = null
                        showError("Subject '$subjectName' is not found.")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Database Error: ${error.message}")
                    showError("Error loading subject details: ${error.message}")
                }
            })
    }
}