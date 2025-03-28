package com.example.andiezstore.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.andiezstore.R
import com.example.andiezstore.user.fragments.RegistedFragment
import com.example.andiezstore.user.model.Account

open class AccountAdapter(
    listAccount1: RegistedFragment,
    private val listAccount: MutableList<Account>
) :
    RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {
    class AccountViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val edtName: EditText = view.findViewById<EditText>(R.id.edtName)
        val edtEmail: EditText = view.findViewById<EditText>(R.id.edtEmail)
        val edtPass: EditText = view.findViewById<EditText>(R.id.edtPhone)

        fun onBind(account: Account) {
            edtName.setText(account.name)
            edtEmail.setText(account.email)
            edtPass.setText(account.pass)

        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AccountViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.fragment_registed, parent, false)
        return AccountViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val account= listAccount[position]
        holder.onBind(account)
    }

    override fun getItemCount(): Int {
        return listAccount.size
    }
}