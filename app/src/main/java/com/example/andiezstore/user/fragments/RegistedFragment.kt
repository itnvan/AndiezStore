package com.example.andiezstore.user.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.example.andiezstore.R
import com.example.andiezstore.databinding.FragmentRegistedBinding
import com.example.andiezstore.ui.DatabaseRoom
import com.example.andiezstore.user.account.AccountResponsitory
import com.example.andiezstore.user.adapter.AccountAdapter
import com.example.andiezstore.user.model.Account
import com.example.andiezstore.user.viewmodel.AccountViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class RegistedFragment : Fragment() {
    private lateinit var binding: FragmentRegistedBinding
    private lateinit var accountAdapter: AccountAdapter
    private lateinit var accountViewModel: AccountViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegistedBinding.inflate(layoutInflater)
        addAccount()
        checkAccount()
        data()
        return binding.root
    }

    val listAccount = mutableListOf(
        Account(name = "Andiez", phone = "0962913209", pass = "An270502")
    )

    private fun data() {
        val accountDao = DatabaseRoom.getDatabase(this@RegistedFragment).accountDao()
        val accountResponsitory = AccountResponsitory(accountDao)
        accountViewModel = AccountViewModel(accountResponsitory)
        accountViewModel.fetchData.observe(viewLifecycleOwner) {
            accountAdapter.notifyDataSetChanged()
            accountViewModel.fetchAccount()
            val listAccount = accountViewModel.getAllAccount()
            Log.d("listAccount", "$listAccount")
        }
        addAccount()
    }

    private fun checkAccount() {
        val addName = binding.edtName.text?.trim()
        val addPhone = binding.edtPhone.text?.trim()
        val addPass = binding.edtPass.text?.trim()
        val _fetchData = MutableLiveData<List<Account>>()
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                if (addName != null && addPass != null && addPhone != null) {
                    val newAccount =
                        Account(
                            name = addName.toString(),
                            phone = addPhone.toString(),
                            pass = addPass.toString()
                        )
                    accountViewModel.insertAccount(newAccount)
                    listAccount.add(newAccount)
                    binding.edtName.text?.clear()
                    binding.edtPhone.text?.clear()
                    binding.edtPass.text?.clear()
                    Toast.makeText(
                        this@RegistedFragment,
                        "Account created successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("addAccout", "$_fetchData")
                    accountAdapter.notifyItemChanged(listAccount.size - 1)
                } else {
                    Toast.makeText(
                        this@RegistedFragment,
                        "Account created failed",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.edtPhone.requestFocus()
                    binding.edtPass.requestFocus()
                    binding.edtName.requestFocus()
                    binding.edtName.error = null
                    binding.edtPass.error = null
                    binding.edtPhone.error = null

                }
            }
        }
    }

    private fun addAccount() {
        binding.btnRegister.setOnClickListener {

        }
    }
}

