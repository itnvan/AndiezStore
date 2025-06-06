package com.example.andiezstore.admin.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.andiezstore.R
import com.example.andiezstore.admin.adapter.NewsAdminAdapter
import com.example.andiezstore.databinding.FragmentAdminNewsBinding
import com.example.andiezstore.user.model.News
import com.example.andiezstore.utils.Crud
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminNewsFragment : Fragment() {
    private var _binding: FragmentAdminNewsBinding? = null
    private val binding get() = _binding!!
    private lateinit var newsDatabaseRef: DatabaseReference
    private lateinit var storageRef: StorageReference
    private var _currentSelectedNews: News? = null // Renamed to clearly indicate selected news for CRUD
    private lateinit var newsAdapter: NewsAdminAdapter // Declare your adapter

    // Animation variables
    private lateinit var fromBottom: Animation
    private lateinit var toBottom: Animation
    private lateinit var rotateOpen: Animation
    private lateinit var rotateClose: Animation
    private var clicked = false

    private var activeFab: View? = null // For "lighting up" the FABs

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fromBottom = AnimationUtils.loadAnimation(context, R.anim.from_bottom_anim)
        toBottom = AnimationUtils.loadAnimation(context, R.anim.to_bottom_anim)
        rotateOpen = AnimationUtils.loadAnimation(context, R.anim.rotate_open_anim)
        rotateClose = AnimationUtils.loadAnimation(context, R.anim.rotate_close_anim)

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri: Uri? = result.data?.data
                if (imageUri != null) {
                    Crud.updateSelectedImage(imageUri, Crud.dialog?.findViewById(R.id.imgSelectedNews)!!, requireContext())
                } else {
                    showError("No image selected.")
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAdminNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newsDatabaseRef = FirebaseDatabase.getInstance().getReference("Admin").child("News")
        storageRef = FirebaseStorage.getInstance().reference.child("news_images")

        newsAdapter = NewsAdminAdapter(emptyList()) { clickedNews ->
            _currentSelectedNews = clickedNews
            showCrudDialog(clickedNews, mode = Crud.DialogMode.UPDATE)


        }
        binding.recyclerViewNews.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewNews.adapter = newsAdapter

        binding.btnMain.setOnClickListener {
            onMainButtonClicked()
        }

        binding.btnAdd.setOnClickListener {
            handleFabClick(it)
        }

        binding.btnUpdate.setOnClickListener {
            handleFabClick(it)
        }

        binding.btnDelete.setOnClickListener {
            handleFabClick(it)
        }

        setVisibility(clicked)
        setClickable(clicked)

        fetchAllNews()
    }

    private fun handleFabClick(clickedFab: View) {
        activeFab?.let {
            when (it.id) {
                R.id.btnAdd -> it.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.my_green)
                R.id.btnUpdate -> it.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.my_blue)
                R.id.btnDelete -> it.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.my_red)
            }
        }

        activeFab = clickedFab
        clickedFab.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.my_blue)

        when (clickedFab.id) {
            R.id.btnAdd -> {
                _currentSelectedNews = null // Clear any previously selected news when adding new
                showCrudDialog(mode = Crud.DialogMode.ADD)
            }
            R.id.btnUpdate -> {
                _currentSelectedNews?.let { newsToUpdate ->
                    showCrudDialog(newsToUpdate, mode = Crud.DialogMode.UPDATE)
                } ?: showError("Please select a news item to update by clicking it in the list.")
            }
            R.id.btnDelete -> {
                _currentSelectedNews?.let { newsToDelete ->
                    showCrudDialog(newsToDelete, mode = Crud.DialogMode.DELETE)
                } ?: showError("Please select a news item to delete by clicking it in the list.")
            }
        }
    }

    private fun onMainButtonClicked() {
        clicked = !clicked
        setVisibility(clicked)
        setAnimation(clicked)
        setClickable(clicked)

        if (!clicked) {
            activeFab = null
            binding.btnAdd.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.my_green)
            binding.btnUpdate.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.my_blue)
            binding.btnDelete.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.my_red)
        }
    }

    private fun setVisibility(clicked: Boolean) {
        binding.btnAdd.visibility = if (!clicked) View.VISIBLE else View.INVISIBLE
        binding.btnUpdate.visibility = if (!clicked) View.VISIBLE else View.INVISIBLE
        binding.btnDelete.visibility = if (!clicked) View.VISIBLE else View.INVISIBLE
    }

    private fun setAnimation(clicked: Boolean) {
        binding.btnAdd.startAnimation(if (!clicked) fromBottom else toBottom)
        binding.btnUpdate.startAnimation(if (!clicked) fromBottom else toBottom)
        binding.btnDelete.startAnimation(if (!clicked) fromBottom else toBottom)
        binding.btnMain.startAnimation(if (!clicked) rotateOpen else rotateClose)
    }

    private fun setClickable(clicked: Boolean) {
        binding.btnAdd.isClickable = !clicked
        binding.btnUpdate.isClickable = !clicked
        binding.btnDelete.isClickable = !clicked
    }

    private fun showCrudDialog(news: News? = null, mode: Crud.DialogMode) {
        Crud.showCrudDialog(
            context = requireContext(),
            currentNews = news,
            imagePickerLauncher = imagePickerLauncher,
            mode = mode,
            onAdd = { newNews ->
                newNews.image?.let { imageUriString ->
                    val imageUri = Uri.parse(imageUriString)
                    uploadImageToFirebaseStorage(imageUri) { imageUrl ->
                        val newsWithImageUrl = newNews.copy(image = imageUrl)
                        onAddNews(newsWithImageUrl)
                    }
                } ?: run {
                    onAddNews(newNews) // No image, just add news
                }
            },
            onUpdate = { updatedNews ->
                updatedNews.image?.let { imageUriString ->
                    // Check if the image string is a local URI (e.g., content:// or file://)
                    if (imageUriString.startsWith("content://") || imageUriString.startsWith("file://")) {
                        val imageUri = Uri.parse(imageUriString)
                        uploadImageToFirebaseStorage(imageUri) { imageUrl ->
                            val newsWithImageUrl = updatedNews.copy(image = imageUrl)
                            onUpdateNews(newsWithImageUrl)
                        }
                    } else {
                        // If it's already a web URL (Firebase Storage URL), no need to re-upload.
                        onUpdateNews(updatedNews)
                    }
                } ?: run {
                    // If no image is selected, and currentNews also had no image, proceed without image
                    onUpdateNews(updatedNews)
                }
            },
            onDelete = { newsToDelete ->
                newsToDelete.image?.let { imageUrl ->
                    if (imageUrl.startsWith("http")) { // Only attempt to delete from Storage if it's a Storage URL
                        deleteImageFromFirebaseStorage(imageUrl) { success ->
                            if (success) {
                                onDeleteNews(newsToDelete)
                            } else {
                                Log.e("AdminNewsFragment", "Failed to delete image for news: ${newsToDelete.title}. Proceeding with news deletion.")
                                onDeleteNews(newsToDelete) // Proceed with news deletion even if image delete fails
                            }
                        }
                    } else {
                        onDeleteNews(newsToDelete) // If not a Storage URL, just delete the news
                    }
                } ?: onDeleteNews(newsToDelete) // If no image, just delete the news
            }
            ,
            onCancel = {
                resetFabTint() // Reset tint of active FAB if dialog cancelled
            }
        )
    }

    // Function to upload image to Firebase Storage
    private fun uploadImageToFirebaseStorage(imageUri: Uri, onComplete: (String) -> Unit) {
        val fileName = "${System.currentTimeMillis()}_${imageUri.lastPathSegment}"
        val imageRef = storageRef.child(fileName)

        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    Log.d("FirebaseStorage", "Image uploaded: $imageUrl")
                    showSuccess("Image uploaded successfully!")
                    onComplete(imageUrl)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseStorage", "Image upload failed: ${e.message}", e)
                showError("Image upload failed: ${e.message}")
                onComplete("") // Indicate failure to callback
            }
    }

    // Function to delete image from Firebase Storage
    private fun deleteImageFromFirebaseStorage(imageUrl: String, onComplete: (Boolean) -> Unit) {
        try {
            // Get the file name from the URL by parsing the encoded path
            val encodedPath = Uri.parse(imageUrl).lastPathSegment
            val fileName = encodedPath?.substringAfterLast("news_images%2F")?.substringBefore("?")

            if (fileName == null) {
                Log.e("FirebaseStorage", "Could not extract file name from URL: $imageUrl")
                onComplete(false)
                return
            }

            val imageRef = storageRef.child(fileName)

            imageRef.delete()
                .addOnSuccessListener {
                    Log.d("FirebaseStorage", "Image deleted successfully: $imageUrl")
                    showSuccess("Image deleted successfully from Storage.")
                    onComplete(true)
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseStorage", "Failed to delete image: ${e.message}", e)
                    showError("Failed to delete image from Storage: ${e.message}")
                    onComplete(false)
                }
        } catch (e: Exception) {
            Log.e("FirebaseStorage", "Error parsing image URL for deletion: ${e.message}", e)
            showError("Error deleting image from Storage: ${e.message}")
            onComplete(false)
        }
    }


    // Function to fetch ALL news from Firebase for the RecyclerView
    private fun fetchAllNews() {
        newsDatabaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newsList = mutableListOf<News>()
                for (newsSnapshot in snapshot.children) {
                    val news = newsSnapshot.getValue(News::class.java)
                    news?.let {
                        it.firebaseKey = newsSnapshot.key // Store the Firebase key
                        newsList.add(it)
                    }
                }
                // Sort news by date if desired (e.g., newest first)
                newsList.sortByDescending {
                    try {
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse(it.date ?: "")
                    } catch (e: Exception) {
                        Date(0) // Return a very old date if parsing fails
                    }
                }
                newsAdapter.updateNewsList(newsList) // Update RecyclerView adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AdminNewsFragment", "Database Error: ${error.message}")
                showError("Error fetching news: ${error.message}")
            }
        })
    }

    // CRUD logic functions (interaction with Firebase)
    private fun onAddNews(news: News) {
        val newNewsData: HashMap<String, Any?> = hashMapOf(
            "title" to news.title,
            "decription" to news.decription,
            "date" to news.date,
            "author" to news.author,
            "image" to news.image
        )

        newsDatabaseRef.push().setValue(newNewsData)
            .addOnSuccessListener {
                Log.d("Firebase", "Admin: News added successfully.")
                showSuccess("News added successfully!")
                resetFabTint()
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Admin: Error adding news: ${e.message}", e)
                showError("Failed to add News: ${e.message}")
            }
    }

    private fun onUpdateNews(news: News) {
        news.firebaseKey?.let { key ->
            val updatedNewsData: HashMap<String, Any?> = hashMapOf(
                "title" to news.title,
                "decription" to news.decription,
                "date" to news.date, // Preserve original date
                "author" to news.author,
                "image" to news.image
            )

            newsDatabaseRef.child(key).updateChildren(updatedNewsData)
                .addOnSuccessListener {
                    Log.d("Firebase", "Admin: News updated successfully.")
                    showSuccess("News updated successfully!")
                    resetFabTint()
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Admin: Error updating news: ${e.message}", e)
                    showError("Failed to update news: ${e.message}")
                }
        } ?: showError("Can't find News to update. Firebase key is missing.")
    }

    private fun onDeleteNews(news: News) {
        news.firebaseKey?.let { key ->
            newsDatabaseRef.child(key).removeValue()
                .addOnSuccessListener {
                    Log.d("Firebase", "Admin: News deleted successfully.")
                    showSuccess("News deleted successfully!")
                    resetFabTint()
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Admin: Error deleting news: ${e.message}", e)
                    showError("Failed to delete news: ${e.message}")
                }
        } ?: showError("No News to delete. Firebase key is missing.")
    }

    private fun resetFabTint() {
        activeFab?.let {
            when (it.id) {
                R.id.btnAdd -> it.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.my_green)
                R.id.btnUpdate -> it.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.my_blue)
                R.id.btnDelete -> it.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.my_red)
            }
            activeFab = null
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Crud.dismissDialog()
    }
}