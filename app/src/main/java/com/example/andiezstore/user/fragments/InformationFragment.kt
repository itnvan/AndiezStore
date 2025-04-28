package com.example.andiezstore.user.fragments

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy.ALL
import com.bumptech.glide.request.RequestOptions
import com.example.andiezstore.R
import com.example.andiezstore.databinding.FragmentInformationBinding
import com.example.andiezstore.user.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream


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

    // Biến để lưu trữ trạng thái ban đầu
    private var originalName: String? = null
    private var originalEmail: String? = null
    private var originalDateOfBirth: String? = null
    private var originalHometown: String? = null
    private var originalImageUri: Uri? = null  // Lưu Uri của ảnh ban đầu
    private var isImageChanged = false // Theo dõi thay đổi ảnh

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
            return
        }
        fetchAndBindData()
        setupTextWatchers() // Thiết lập lắng nghe thay đổi
        binding.btnUpdate.setOnClickListener { updateInformation() }
        binding.imgUser.setOnClickListener { showImageSelectionDialog() }
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
                    val imageBitmap = result.data?.extras?.get("data") as Bitmap
                    selectedImageUri = bitmapToUri(imageBitmap)
                    displayImage(selectedImageUri)
                    isImageChanged = true // Đánh dấu ảnh đã thay đổi
                    updateUpdateButtonState()
                } else showToast("Failed to capture image")
            }

        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK && result.data != null) {
                    selectedImageUri = result.data?.data
                    displayImage(selectedImageUri)
                    isImageChanged = true // Đánh dấu ảnh đã thay đổi
                    updateUpdateButtonState()
                } else showToast("No Image Selected")
            }
    }

    private fun updateInformation() {
        val name = binding.edtName.text.toString()
        val email = binding.edtEmail.text.toString()
        val dateOfBirth = binding.edtBirthday.text.toString()
        val hometown = binding.edtHometown.text.toString()

        if (selectedImageUri != null) {
            uploadImageAndGetUrl(userId = userId, selectedImageUri) { imageUrl, success ->
                if (success) {
                    updateUserInformation(name, email, imageUrl, dateOfBirth, hometown)
                } else {
                    showToast("Failed to update profile image. Please try again.")
                }
            }
        } else {
            updateUserInformation(
                name,
                email,
                null,  // Keep the old image URL
                dateOfBirth,
                hometown
            )
        }
    }

    private fun updateUserInformation(
        name: String,
        email: String,
        imageUrl: String?,
        dateOfBirth: String,
        hometown: String
    ) {
        val userUpdates = mutableMapOf<String, Any>(
            "name" to name,
            "email" to email,
            "dateOfBirth" to dateOfBirth,
            "hometown" to hometown
        )

        if (imageUrl != null) {
            userUpdates["profileImageUrl"] = imageUrl
        }

        database.child(userId).updateChildren(userUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Profile updated successfully")
                    fetchAndBindData() // Gọi lại để cập nhật dữ liệu và trạng thái nút
                } else {
                    showToast("Failed to update profile")
                }
            }
    }


    private fun fetchAndBindData() {
        database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("UseKtx")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    showToast("No user data found")
                    return
                }

                val user = snapshot.getValue(User::class.java) ?: run {
                    Log.e("Firebase", "Failed to parse user data")
                    showToast("Failed to retrieve user data")
                    return
                }

                // Lưu trữ giá trị ban đầu
                originalName = user.name
                originalEmail = user.email
                originalDateOfBirth = user.dateOfBirth
                originalHometown = user.hometown
                originalImageUri = if (user.profileImageUrl != null) Uri.parse(user.profileImageUrl) else null
                isImageChanged = false

                binding.edtName.setText(user.name)
                binding.edtEmail.setText(user.email)
                binding.edtBirthday.setText(user.dateOfBirth)
                binding.edtHometown.setText(user.hometown)
                loadProfileImage(user.profileImageUrl)
                updateUpdateButtonState() // Cập nhật trạng thái nút khi tải dữ liệu
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching user data: ${error.message}")
                showToast("Failed to fetch data: ${error.message}")
            }
        })
    }

    private fun uploadImageAndGetUrl(
        userId: String,
        imageUri: Uri?,
        callback: (String?, Boolean) -> Unit
    ) {
        if (imageUri == null) {
            callback(null, true)
            return
        }
        val imageRef = storageRef.child("users/$userId/profile_image.jpg")

        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val downloadUrl = downloadUri.toString()
                    callback(downloadUrl, true)
                }.addOnFailureListener { exception ->
                    Log.e("Firebase", "Failed to get download URL: ${exception.message}")
                    showToast("Failed to get download URL")
                    callback(null, false)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Failed to upload image: ${exception.message}")
                showToast("Failed to upload image")
                callback(null, false)
            }
    }


    private fun loadProfileImage(imageUrl: String?) {
        val requestOptions = RequestOptions()
            .centerCrop()
            .diskCacheStrategy(ALL)

        Glide.with(requireContext())
            .load(imageUrl ?: R.drawable.img_user)
            .apply(requestOptions)
            .into(binding.imgGetUser)
    }

    private fun showImageSelectionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Select Image")
            .setMessage("Choose your option?")
            .setPositiveButton("Gallery") { dialog, _ ->
                dialog.dismiss()
                openGallery()
            }
            .setNegativeButton("Camera") { dialog, _ ->
                dialog.dismiss()
                checkCameraPermission()
            }
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
            .setMessage("App needs access to your camera to take pictures.")
            .setPositiveButton("OK") { _, _ ->
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun launchCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(takePictureIntent)
    }

    @SuppressLint("IntentReset")
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    private fun displayImage(imageUri: Uri?) {
        val requestOptions = RequestOptions()
            .centerCrop()
            .diskCacheStrategy(ALL)

        Glide.with(requireContext())
            .load(imageUri ?: R.drawable.img_user)
            .apply(requestOptions)
            .into(binding.imgGetUser)
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
            "Title",
            null
        )
        return Uri.parse(path)
    }

    private fun setupTextWatchers() {
        binding.edtName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateUpdateButtonState()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.edtEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateUpdateButtonState()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.edtBirthday.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateUpdateButtonState()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.edtHometown.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateUpdateButtonState()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updateUpdateButtonState() {
        val nameChanged = binding.edtName.text.toString() != originalName
        val emailChanged = binding.edtEmail.text.toString() != originalEmail
        val birthdayChanged = binding.edtBirthday.text.toString() != originalDateOfBirth
        val hometownChanged = binding.edtHometown.text.toString() != originalHometown
        val imageChanged = isImageChanged
        binding.btnUpdate.background =(ContextCompat.getDrawable(requireContext(), R.drawable.bg_subject))
        if (nameChanged || emailChanged || birthdayChanged || hometownChanged || imageChanged) {
            binding.btnUpdate.background =(ContextCompat.getDrawable(requireContext(), R.drawable.bg_subject))
            binding.btnUpdate.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white_blue
                )
            )
           // Thay R.color.your_highlight_color
            binding.btnUpdate.isEnabled = true
        } else {

            binding.btnUpdate.isEnabled = false
        }
    }
}
