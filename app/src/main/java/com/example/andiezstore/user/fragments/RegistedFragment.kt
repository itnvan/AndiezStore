package com.example.andiezstore.user.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.example.andiezstore.databinding.FragmentRegistedBinding
import com.example.andiezstore.ui.DatabaseRoom
import com.example.andiezstore.user.account.AccountResponsitory
import com.example.andiezstore.user.adapter.AccountAdapter
import com.example.andiezstore.user.model.Account
import com.example.andiezstore.user.viewmodel.AccountViewModel
import com.example.andiezstore.utils.Util
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class RegistedFragment : Fragment() {
    private lateinit var binding: FragmentRegistedBinding
    private lateinit var accountAdapter: AccountAdapter
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegistedBinding.inflate(layoutInflater)
        addAccount()
        checkAccount()
        data()
        isValidPassword(password = "")
        return binding.root
    }

    private val listAccount = mutableListOf(
        Account(name = "Andiez", phone = "0962913209", pass = "An270502")
    )

    @SuppressLint("NotifyDataSetChanged")
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
        checkAccount()
        addAccount()
    }
    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = "^[A-Z].*(?=.*[0-9])(?=.*[^a-zA-Z0-9]).*$"
        val pattern = Regex(passwordPattern)
        return pattern.matches(password)
    }
    private fun checkAccount() {
        val addName = binding.edtName.text?.trim()
        val addPhone = binding.edtPhone.text?.trim()
        val addPass = binding.edtPass.text?.trim()
        val _fetchData = MutableLiveData<List<Account>>()
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                if (addName.isNullOrEmpty() && !addPhone.isNullOrEmpty() && !addPass.isNullOrEmpty()) {
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
                        requireContext(),
                        "Account created successfully",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    Log.d("addAccount", "$_fetchData")
                    accountAdapter.notifyItemChanged(listAccount.size - 1)
                } else {
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
        isValidPassword(password = "")
        binding.btnRegister.setOnClickListener {
            checkAccount()
            val addName = binding.edtName.text?.trim()
            val addPhone = binding.edtPhone.text?.trim()
            val addPass = binding.edtPass.text?.trim()
            Util.showDialog(requireContext(),"Wait for add Account")
            database= FirebaseDatabase.getInstance().getReference("Users")
            val user=Account(name = "$addName", phone = "$addPhone", pass = "$addPass")
            database.child("$addName").setValue(user).addOnSuccessListener {
                addName?.length?.let { it1 ->
                    if(it1 < 10) {
                        binding.edtName.error
                        binding.edtName.requestFocus()
                    } else{
                        binding.edtName.text
                    }
                }
                addPhone?.length.let {it2->
                    if (it2 != null) {
                        if(it2 < 10){
                            binding.edtPhone.error
                            binding.edtPhone.requestFocus()
                        }
                        else{
                        }
                    }
                }
                addPass?.length.let { it3->
                    if (it3 != null) {
                        if(it3<8) {
                            binding.edtPass.error
                            binding.edtPass.requestFocus()
                        }
                    }
                }
                binding.edtName.text?.clear()
                binding.edtPhone.text?.clear()
                binding.edtPass.text?.clear()
                Util.hideDialog()
                Toast.makeText(requireContext(),"Success",Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Util.hideDialog()
                Toast.makeText(requireContext(),"Error",Toast.LENGTH_SHORT).show()
            }
        }
    }
}

