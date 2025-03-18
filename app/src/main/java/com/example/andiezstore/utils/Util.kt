package com.example.andiezstore.utils

import android.app.ProgressDialog
import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.example.andiezstore.R
import com.example.andiezstore.databinding.ProcessDialogBinding

object Util {
    private var dialog: AlertDialog ? = null
    fun showDialog(context: Context, mess: String) {
        val process = ProcessDialogBinding.inflate(LayoutInflater.from(context))
        process.tvMessage.text=mess
        dialog=AlertDialog.Builder(context).setView(process.root).setCancelable(false).create()
        dialog!!.show()
    }
    fun hideDialog(){
        dialog?.dismiss()
    }
}