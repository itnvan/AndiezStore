package com.example.andiezstore.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.andiezstore.R
import com.example.andiezstore.model.AccountModel
import java.util.zip.Inflater

open class AccountAdapter(private val  listAccount:MutableList<AccountModel>,private val context:Context):RecyclerView.Adapter<AccountAdapter.AccountViewModel>() {
    inner class AccountViewModel(accountView:View):RecyclerView.ViewHolder(accountView) {
        val edtPhone: EditText=accountView.findViewById(R.id.edtPhone)
        val edtPass: EditText=accountView.findViewById(R.id.edtPass)
        fun onBind(account: AccountModel){
            edtPhone.text.toString()=account.phone
            edtPass.text.toString()=account.pass
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AccountAdapter.AccountViewModel {
        val inflater= LayoutInflater.from(context).inflate(R.layout.fragment_phone,context,false)
        return AccountViewModel(inflater)
    }

    override fun onBindViewHolder(holder: AccountAdapter.AccountViewModel, position: Int) {

    }

    override fun getItemCount(): Int {
        return listAccount.size
    }
}