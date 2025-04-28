package com.example.andiezstore.user.fragments

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.andiezstore.databinding.FragmentInformationBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

class InformationFragment : Fragment() {
    private lateinit var binding: FragmentInformationBinding
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ActivityResultLaunchers
        cameraPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    launchCamera()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Camera permission is required",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK && result.data != null) {
                    val imageBitmap = result.data?.extras?.get("data") as Bitmap
                    displayImage(imageBitmap)
                } else {
                    Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK && result.data != null) {
                    val imageUri = result.data?.data
                    displayImage(imageUri)
                } else {
                    Toast.makeText(requireContext(), "No Image Selected", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInformationBinding.inflate(layoutInflater)
        binding.imgUser.setOnClickListener {
            showImageSelectionDialog()
        }
        return binding.root
    }

    private fun showImageSelectionDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Image")
        builder.setMessage("Choose your option?")
        builder.setPositiveButton("Gallery") { dialog, which ->
            dialog.dismiss()
            openGallery()
        }
        builder.setNegativeButton("Camera") { dialog, which ->
            dialog.dismiss()
            checkCameraPermission()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is granted, launch the camera
                launchCamera()
            }

            shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA) -> {
                // Show an explanation to the user
                AlertDialog.Builder(requireContext())
                    .setTitle("Camera Permission Required")
                    .setMessage("App needs access to your camera to take pictures.")
                    .setPositiveButton("OK") { _, _ ->
                        // Request the permission again
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            else -> {
                // Request the permission
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    private fun launchCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(takePictureIntent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    private fun displayImage(imageBitmap: Bitmap) {
        val requestOptions = RequestOptions()
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache for better performance

        Glide.with(this)
            .load(imageBitmap)
            .apply(requestOptions)
            .into(binding.imgGetUser) // Load into imgGetUser
    }

    private fun displayImage(imageUri: Uri?) {
        val requestOptions = RequestOptions()
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)

        Glide.with(this)
            .load(imageUri)
            .apply(requestOptions)
            .into(binding.imgGetUser)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Unregister ActivityResultLaunchers
        cameraPermissionLauncher.unregister()
        cameraLauncher.unregister()
        galleryLauncher.unregister()
    }
}
