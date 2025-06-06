package com.example.andiezstore.user.fragments

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.andiezstore.R
import com.example.andiezstore.databinding.FragmentInformationBinding
import com.example.andiezstore.user.UserMainActivity
import com.example.andiezstore.user.model.User
import com.example.andiezstore.utils.Util
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.core.net.toUri

@Suppress("DEPRECATION")
class InformationFragment : Fragment() {
    private var _binding: FragmentInformationBinding? = null
    private val binding get() = _binding!!
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String
    private lateinit var storageRef: StorageReference
    private var selectedImageUri: Uri? = null

    private var originalName: String? = null
    private var originalEmail: String? = null
    private var originalDateOfBirth: String? = null
    private var originalHometown: String? = null
    private var originalProfileImageUrl: String? = null // Store URL string instead of Uri for original
    private var isImageChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeActivityResultLaunchers()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInformationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")
        storageRef = FirebaseStorage.getInstance().reference
        userId = auth.currentUser?.uid ?: run {
            showToast("User not logged in")
            // Consider navigating back or to login if user is not logged in
            findNavController().popBackStack()
            return
        }
        fetchAndBindData()
        setupTextWatchers()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.imgBack.setOnClickListener {
            // Check for unsaved changes before navigating back
            if (hasUnsavedChanges()) {
                showUnsavedChangesDialog {
                    findNavController().navigate(R.id.action_informationFragment_to_homeFragment)
                }
            } else {
                findNavController().navigate(R.id.action_informationFragment_to_homeFragment)
            }
        }
        binding.btnUpdate.setOnClickListener { updateInformation() }
        binding.fabEditPhoto.setOnClickListener { showImageSelectionDialog() }
        binding.imgExit.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Date Picker Dialog for Birthday
        binding.inputLayoutBirthday.setEndIconOnClickListener {
            showDatePickerDialog()
        }
        binding.edtBirthday.setOnClickListener {
            showDatePickerDialog()
        }


        binding.btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        originalDateOfBirth?.let {
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                calendar.time = sdf.parse(it)!!
            } catch (e: Exception) {
            }
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                val selectedDate = "${String.format("%02d",selectedDayOfMonth)}/${String.format("%02d",selectedMonth + 1)}/$selectedYear"
                binding.edtBirthday.setText(selectedDate)
                updateUpdateButtonState() // Check if this change enables update
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }


    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Information")
            .setMessage("Are you sure you want to delete all your personal information? This action cannot be undone.")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteUserInformation()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteUserInformation() {
        Util.showDialog(requireContext(), "Deleting information...")

        // 1. Delete profile image from Firebase Storage
        val imageRef = storageRef.child("users/$userId/profile_image.jpg")
        imageRef.delete().addOnCompleteListener { imageDeleteTask ->
            if (imageDeleteTask.isSuccessful) {
                Log.d("InformationFragment", "Profile image deleted from Storage.")
            } else {
                Log.e("InformationFragment", "Failed to delete profile image from Storage: ${imageDeleteTask.exception?.message}")
                // Continue to delete RTDB data even if image deletion fails, but log it.
            }

            // 2. Delete information from Firebase Realtime Database
            val updates = mapOf<String, Any?>(
                "name" to null,
                "email" to null,
                "dateOfBirth" to null,
                "hometown" to null,
                "profileImageUrl" to null
            )

            database.child(userId).child("information").updateChildren(updates)
                .addOnCompleteListener { rtdbTask ->
                    Util.hideDialog()
                    if (rtdbTask.isSuccessful) {
                        showToast("Information deleted successfully")
                        // Clear fields and UI
                        binding.edtName.setText("")
                        binding.edtEmail.setText("")
                        binding.edtBirthday.setText("")
                        binding.edtHometown.setText("")
                        loadProfileImage(null) // Load default image

                        // Reset original values and image changed flag
                        originalName = null
                        originalEmail = null
                        originalDateOfBirth = null
                        originalHometown = null
                        originalProfileImageUrl = null
                        isImageChanged = false
                        selectedImageUri = null

                        updateUpdateButtonState()
                    } else {
                        showToast("Failed to delete information from database.")
                        Log.e("InformationFragment", "RTDB deletion error: ${rtdbTask.exception?.message}")
                    }
                }
        }
    }


    private fun initializeActivityResultLaunchers() {
        cameraPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) launchCamera()
                else showToast("Camera permission is required")
            }

        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK && result.data != null) {
                    val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                    if (imageBitmap != null) {
                        selectedImageUri = bitmapToUri(imageBitmap)
                        displayImage(selectedImageUri)
                        isImageChanged = true
                        updateUpdateButtonState()
                    } else {
                        showToast("Failed to get image from camera")
                    }
                } else showToast("Failed to capture image")
            }

        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK && result.data != null) {
                    selectedImageUri = result.data?.data
                    displayImage(selectedImageUri)
                    isImageChanged = true
                    updateUpdateButtonState()
                } else showToast("No Image Selected")
            }
    }

    private fun updateInformation() {
        val name = binding.edtName.text.toString().trim()
        val email = binding.edtEmail.text.toString().trim()
        val dateOfBirth = binding.edtBirthday.text.toString().trim()
        val hometown = binding.edtHometown.text.toString().trim()

        // Basic validation (can be more extensive)
        if (name.isEmpty() || email.isEmpty() || dateOfBirth.isEmpty() || hometown.isEmpty()) {
            showToast("All fields are required.")
            return
        }
        Util.showDialog(requireContext(), "Updating profile...")

        if (isImageChanged && selectedImageUri != null) {
            uploadImageAndGetUrl(userId, selectedImageUri) { imageUrl, success ->
                if (success && imageUrl != null) {
                    updateUserInformationInDatabase(name, email, imageUrl, dateOfBirth, hometown)
                } else {
                    Util.hideDialog()
                    showToast("Failed to update profile image. Information not updated.")
                }
            }
        } else {
            // If image is not changed, use the originalProfileImageUrl
            updateUserInformationInDatabase(name, email, originalProfileImageUrl, dateOfBirth, hometown)
        }
    }

    private fun updateUserInformationInDatabase(
        name: String,
        email: String,
        imageUrl: String?, // Can be the new URL or the original one
        dateOfBirth: String,
        hometown: String
    ) {
        val userUpdates = mutableMapOf<String, Any?>(
            "name" to name,
            "email" to email,
            "dateOfBirth" to dateOfBirth,
            "hometown" to hometown
        )
        // Only include profileImageUrl if it's not null (it could be null if user had no image or it was deleted)
        // If imageUrl is the same as originalProfileImageUrl, Firebase won't do an unnecessary write for this field.
        userUpdates["profileImageUrl"] = imageUrl


        database.child(userId).child("information").updateChildren(userUpdates)
            .addOnCompleteListener { task ->
                Util.hideDialog()
                if (task.isSuccessful) {
                    showToast("Profile updated successfully")
                    // After successful update, re-fetch data to update original values and UI
                    fetchAndBindData()
                } else {
                    showToast("Failed to update profile: ${task.exception?.message}")
                    Log.e("InformationFragment", "Update error: ${task.exception?.message}")
                }
            }
    }


    private fun fetchAndBindData() {
        Util.showDialog(requireContext(), "Loading data...")
        database.child(userId).child("information").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Util.hideDialog()
                if (!snapshot.exists()) {
                    showToast("No user data found. Please complete your profile.")
                    // Initialize original values to empty or null to allow saving new data
                    originalName = ""
                    originalEmail = ""
                    originalDateOfBirth = ""
                    originalHometown = ""
                    originalProfileImageUrl = null
                    isImageChanged = false // No existing image to compare against initially
                    updateUpdateButtonState()
                    return
                }

                val user = snapshot.getValue(User::class.java) ?: run {
                    Log.e("Firebase", "Failed to parse user data")
                    showToast("Failed to retrieve user data")
                    return
                }

                originalName = user.name
                originalEmail = user.email
                originalDateOfBirth = user.dateOfBirth
                originalHometown = user.hometown
                originalProfileImageUrl = user.profileImageUrl
                isImageChanged = false
                selectedImageUri = null // Reset selected image URI on fetch

                binding.edtName.setText(user.name)
                binding.edtEmail.setText(user.email)
                binding.edtBirthday.setText(user.dateOfBirth)
                binding.edtHometown.setText(user.hometown)
                loadProfileImage(user.profileImageUrl)
                updateUpdateButtonState()
            }

            override fun onCancelled(error: DatabaseError) {
                Util.hideDialog()
                Log.e("Firebase", "Error fetching user data: ${error.message}")
                showToast("Failed to fetch data: ${error.message}")
            }
        })
    }

    private fun uploadImageAndGetUrl(
        userId: String,
        imageUri: Uri?, // This should not be nullable here if isImageChanged is true
        callback: (String?, Boolean) -> Unit
    ) {
        if (imageUri == null) { // Should ideally not happen if isImageChanged is true
            callback(originalProfileImageUrl, true) // Fallback to original if somehow null
            return
        }
        val imageRef = storageRef.child("users/$userId/profile_image.jpg")

        imageRef.putFile(imageUri)
            .addOnSuccessListener { _ ->
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    callback(downloadUri.toString(), true)
                }.addOnFailureListener { exception ->
                    Log.e("Firebase", "Failed to get download URL: ${exception.message}")
                    callback(null, false)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Failed to upload image: ${exception.message}")
                callback(null, false)
            }
    }


    private fun loadProfileImage(imageUrl: String?) {
        val requestOptions = RequestOptions()
            .circleCrop() // Make it circular if not using ShapeableImageView's own shaping
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.img_user) // Placeholder while loading
            .error(R.drawable.img_user) // Image if loading fails

        Glide.with(requireContext())
            .load(imageUrl)
            .apply(requestOptions)
            .into(binding.imgUserProfile)
    }

    private fun showImageSelectionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Select Profile Picture")
            .setItems(arrayOf("Open Gallery", "Use Camera")) { dialog, which ->
                when (which) {
                    0 -> openGallery()
                    1 -> checkCameraPermission()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> launchCamera()

            shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA) -> {
                showCameraPermissionRationaleDialog()
            }

            else -> cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun showCameraPermissionRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Camera Permission Required")
            .setMessage("This app needs access to your camera to take pictures for your profile.")
            .setPositiveButton("Grant Permission") { _, _ ->
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun launchCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(takePictureIntent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent) // No need to set type, ACTION_PICK with URI implies images
    }

    private fun displayImage(imageUri: Uri?) {
        // This function is called when a new image is selected from gallery/camera
        // originalProfileImageUrl is not updated here, only selectedImageUri
        loadProfileImage(imageUri?.toString()) // Use loadProfileImage for consistency
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("UseKtx")
    private fun bitmapToUri(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            requireContext().contentResolver,
            bitmap,
            "ProfileImage_${System.currentTimeMillis()}", // Unique title
            null
        )
        return path.toUri()
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateUpdateButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        binding.edtName.addTextChangedListener(textWatcher)
        binding.edtEmail.addTextChangedListener(textWatcher)
        binding.edtBirthday.addTextChangedListener(textWatcher) // Date picker will also trigger this
        binding.edtHometown.addTextChangedListener(textWatcher)
    }

    private fun hasUnsavedChanges(): Boolean {
        val nameChanged = binding.edtName.text.toString() != originalName
        val emailChanged = binding.edtEmail.text.toString() != originalEmail
        val birthdayChanged = binding.edtBirthday.text.toString() != originalDateOfBirth
        val hometownChanged = binding.edtHometown.text.toString() != originalHometown
        // isImageChanged is already tracked
        return nameChanged || emailChanged || birthdayChanged || hometownChanged || isImageChanged
    }


    private fun updateUpdateButtonState() {
        binding.btnUpdate.isEnabled = hasUnsavedChanges()
        if (binding.btnUpdate.isEnabled) {
            binding.btnUpdate.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.purple_500)) // Example color
        } else {
            // Apply inactive style
            binding.btnUpdate.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.gray)) // Example disabled color
        }
    }

    private fun showUnsavedChangesDialog(onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Unsaved Changes")
            .setMessage("You have unsaved changes. Are you sure you want to leave without saving?")
            .setPositiveButton("Leave") { dialog, _ ->
                onConfirm()
                dialog.dismiss()
            }
            .setNegativeButton("Stay", null)
            .show()
    }
    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { dialog, _ ->
                performLogout()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        auth.signOut()
        // Navigate to login screen or initial screen of the app
        // For example, if UserMainActivity is your entry point after login:
        val intent = Intent(requireContext(), UserMainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        Util.showDialog(requireContext(), "Logging out...") // Consider hiding this dialog appropriately
        startActivity(intent)
        requireActivity().finish() // Finish current activity
    }
}
