package com.example.andiezstore.utils

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import com.example.andiezstore.R
import com.example.andiezstore.databinding.DialogCrudBinding
import com.example.andiezstore.databinding.ProcessDialogBinding
import com.example.andiezstore.user.model.News

object Crud {
    private var dialog: AlertDialog? = null
    private lateinit var binding: DialogCrudBinding
    fun showCrudDialog(
        context: Context,
        onAdd: (News) -> Unit,
        onUpdate: (News) -> Unit,
        onDelete: (News) -> Unit,
        onCancel: () -> Unit,
        currentNews: News? = null

    ) {
        val builder = AlertDialog.Builder(context)
        val binding = DialogCrudBinding.inflate(LayoutInflater.from(context))
        builder.setView(binding.root)
        dialog = builder.create()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.setCancelable(false)
        dialog?.show()
        //Anh xa cac view trong dialog
        val edtName: EditText = binding.edtName
        val edtDecription: EditText = binding.edtDecription
        val edtTime: EditText = binding.edtTime
        val edtAuthor:EditText = binding.edtAuthor
        val btnUpdate: Button = binding.btnUpdate
        val btnCancel: AppCompatImageView = binding.btnCancel
        val btnAdd: Button = binding.btnAdd
        val btnDelete: Button = binding.btnDelete

        if (currentNews != null) {
            edtName.setText(currentNews.title)
            edtDecription.setText(currentNews.decription)
            edtTime.setText(currentNews.date)
            edtAuthor.setText(currentNews.author)
            // Trong chế độ Update/Delete, thường không cho phép sửa tên môn học
            // hoặc tên môn học là khóa chính để tìm kiếm
            edtName.isEnabled = false
            btnAdd.visibility = View.GONE // Ẩn nút Add
        } else {
            // Trong chế độ Add, ẩn nút Update và Delete
            btnUpdate.visibility = View.GONE
            btnDelete.visibility = View.GONE
        }
        btnAdd.setOnClickListener {
            val title = edtName.text.toString()
            val decription = edtDecription.text.toString()
            val date = edtTime.text.toString()
            val author = edtAuthor.text.toString()
            if (title.isEmpty() || decription.isEmpty() || date.isEmpty()|| author.isEmpty()) {
                Toast.makeText(context, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val news = News(title = title, decription = decription, date = date, author = author, image = R.drawable.view3.toString())
            onAdd(news)
            dialog?.dismiss()
        }
        btnUpdate.setOnClickListener {
            val title = edtName.text.toString()
            val decription = edtDecription.text.toString()
            val date = edtTime.text.toString()
            val author = edtAuthor.text.toString()
            if (title.isEmpty() || decription.isEmpty() || date.isEmpty()|| author.isEmpty()) {
                Toast.makeText(context, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val news = News(title = title, decription = decription, date = date, author = author, image = R.drawable.view3.toString())
            onUpdate(news)
            dialog?.dismiss()
        }
        btnDelete.setOnClickListener {
            if (currentNews != null) {
                onDelete(currentNews)
                dialog?.dismiss()
            }
        }
        btnCancel.setOnClickListener {
            onCancel()
            dialog?.dismiss()
        }

    }
}