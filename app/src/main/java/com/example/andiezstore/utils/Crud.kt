package com.example.andiezstore.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.bumptech.glide.Glide
import com.example.andiezstore.R
import com.example.andiezstore.user.model.News
import java.text.SimpleDateFormat
import java.util.*

object Crud {

    var dialog: AlertDialog? = null

    enum class DialogMode {
        ADD,
        UPDATE,
        DELETE
    }

    fun showCrudDialog(
        context: Context,
        currentNews: News? = null,
        imagePickerLauncher: ActivityResultLauncher<Intent>,
        mode: DialogMode,
        onAdd: (News) -> Unit,
        onUpdate: (News) -> Unit,
        onDelete: (News) -> Unit,
        onCancel: () -> Unit
    ) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_crud, null)
        builder.setView(dialogView)

        dialog = builder.create()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCancelable(false)

        val tvManageSubject: TextView = dialogView.findViewById(R.id.tvManageSubject)
        val edtName: EditText = dialogView.findViewById(R.id.edtName)
        val edtDescription: EditText = dialogView.findViewById(R.id.edtDecription)
        val edtTime: EditText = dialogView.findViewById(R.id.edtTime)
        val edtAuthor: EditText = dialogView.findViewById(R.id.edtAuthor)
        val imgSelectedNews: ImageView = dialogView.findViewById(R.id.imgSelectedNews)
        val btnSelectImage: View = dialogView.findViewById(R.id.btnSelectImage)

        val btnAddDialog: Button = dialogView.findViewById(R.id.btnAdd)
        val btnUpdateDialog: Button = dialogView.findViewById(R.id.btnUpdate)
        val btnDeleteDialog: Button = dialogView.findViewById(R.id.btnDelete)
        val btnCancel: ImageView = dialogView.findViewById(R.id.btnCancel)

        // Date formatter for consistent display and capture
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        when (mode) {
            DialogMode.ADD -> {
                tvManageSubject.text = context.getString(R.string.add_news)
                btnAddDialog.visibility = View.VISIBLE
                btnUpdateDialog.visibility = View.GONE
                btnDeleteDialog.visibility = View.GONE

                // Set current date/time when adding, and make it uneditable
                edtTime.setText(dateFormat.format(Date()))
                edtTime.isEnabled = false // Make time field read-only for ADD
                edtAuthor.setText("Admin") // Default author for new news (can be made read-only too if desired)
            }
            DialogMode.UPDATE -> {
                tvManageSubject.text = context.getString(R.string.update_news)
                btnAddDialog.visibility = View.GONE
                btnUpdateDialog.visibility = View.VISIBLE
                btnDeleteDialog.visibility = View.GONE

                edtTime.isEnabled = false // Make time field read-only for UPDATE (displays original time)
            }
            DialogMode.DELETE -> {
                tvManageSubject.text = context.getString(R.string.delete_news)
                btnAddDialog.visibility = View.GONE
                btnUpdateDialog.visibility = View.GONE
                btnDeleteDialog.visibility = View.VISIBLE

                // Make all fields uneditable in delete mode
                edtName.isEnabled = false
                edtDescription.isEnabled = false
                edtTime.isEnabled = false
                edtAuthor.isEnabled = false
                btnSelectImage.isClickable = false
            }
        }

        currentNews?.let { news ->
            edtName.setText(news.title)
            edtDescription.setText(news.decription)
            edtTime.setText(news.date) // This will load the original date
            edtAuthor.setText(news.author)
            news.image?.let { imageUrl ->
                if (imageUrl.isNotEmpty()) {
                    Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.view3)
                        .error(R.drawable.view3)
                        .into(imgSelectedNews)
                    imgSelectedNews.visibility = View.VISIBLE
                    dialogView.findViewById<View>(R.id.iconAddImage).visibility = View.GONE
                    dialogView.findViewById<View>(R.id.tvAddImage).visibility = View.GONE
                }
            }
        }

        btnSelectImage.setOnClickListener {
            if (mode != DialogMode.DELETE) { // Only allow image selection if not in delete mode
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                imagePickerLauncher.launch(intent)
            }
        }

        btnAddDialog.setOnClickListener {
            val title = edtName.text.toString().trim()
            val description = edtDescription.text.toString().trim()
            val author = edtAuthor.text.toString().trim()
            val imageUriString = imgSelectedNews.tag as? String

            if (title.isBlank() || description.isBlank() || author.isBlank()) {
                Toast.makeText(context, "Please fill in all required fields (Title, Description, Author).", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Capture the current time right before adding
            val currentTime = dateFormat.format(Date()) // Use the already defined dateFormat

            val newNews = News(
                id = "",
                title = title,
                decription = description,
                date = currentTime, // This will be the auto-set current time
                author = author,
                image = imageUriString
            )
            onAdd(newNews)
            dialog?.dismiss()
        }

        btnUpdateDialog.setOnClickListener {
            currentNews?.let { news ->
                val title = edtName.text.toString().trim()
                val description = edtDescription.text.toString().trim()
                val author = edtAuthor.text.toString().trim()
                val imageUriString = imgSelectedNews.tag as? String ?: news.image

                if (title.isBlank() || description.isBlank() || author.isBlank()) {
                    Toast.makeText(context, "Please fill in all required fields (Title, Description, Author).", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // For update, the date is already populated from currentNews and is read-only
                val updatedNews = news.copy(
                    title = title,
                    decription = description,
                    date = edtTime.text.toString().trim(), // Keep the original date loaded from currentNews
                    author = author,
                    image = imageUriString
                )
                onUpdate(updatedNews)
                dialog?.dismiss()
            } ?: Toast.makeText(context, "No news selected to update.", Toast.LENGTH_SHORT).show()
        }

        btnDeleteDialog.setOnClickListener {
            currentNews?.let { news ->
                onDelete(news)
                dialog?.dismiss()
            } ?: Toast.makeText(context, "No news selected to delete.", Toast.LENGTH_SHORT).show()
        }

        btnCancel.setOnClickListener {
            dialog?.dismiss()
            onCancel()
        }

        dialog?.show()
    }

    fun updateSelectedImage(imageUri: Uri, imageView: ImageView, context: Context) {
        Glide.with(context)
            .load(imageUri)
            .placeholder(R.drawable.view3)
            .error(R.drawable.view3)
            .into(imageView)
        imageView.visibility = View.VISIBLE
        imageView.tag = imageUri.toString()
        (imageView.parent as? View)?.findViewById<View>(R.id.iconAddImage)?.visibility = View.GONE
        (imageView.parent as? View)?.findViewById<View>(R.id.tvAddImage)?.visibility = View.GONE
    }

    fun dismissDialog() {
        dialog?.dismiss()
        dialog = null
    }
}