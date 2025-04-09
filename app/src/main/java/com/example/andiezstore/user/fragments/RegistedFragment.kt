package com.example.andiezstore.user.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.andiezstore.R
import com.example.andiezstore.databinding.FragmentRegistedBinding
import com.example.andiezstore.ui.DatabaseRoom
import com.example.andiezstore.ui.MainActivity
import com.example.andiezstore.user.account.AccountResponsitory
import com.example.andiezstore.user.adapter.AccountAdapter
import com.example.andiezstore.user.model.Account
import com.example.andiezstore.user.viewmodel.AccountViewModel
import com.example.andiezstore.utils.Util
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class RegistedFragment : Fragment() {
    private lateinit var recyclerview: RecyclerView
    private lateinit var binding: FragmentRegistedBinding
    private lateinit var accountAdapter: AccountAdapter
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegistedBinding.inflate(layoutInflater)
        addAccount()
        checkAccount()
        data()
        setColorButton()
//        isValidPassword(password = "")
        binding.tvLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registedFragment_to_loginFragment)
        }
        return binding.root
    }

    private val listAccount = mutableListOf(
        Account(name = "Andiez", email = "ksit.nvan@gmail.com", pass = "An270502")
    )


    @SuppressLint("NotifyDataSetChanged")
    private fun data() {
        val accountDao = DatabaseRoom.getDatabase(this@RegistedFragment).accountDao()
        val accountResponsitory = AccountResponsitory(accountDao)
        accountViewModel = AccountViewModel(accountResponsitory)
        accountViewModel.fetchData.observe(viewLifecycleOwner) {
            accountAdapter.notifyDataSetChanged()
            accountViewModel.fetchAccount()
            val list = accountViewModel.getAllAccount()
            Log.d("listAccount", "$list")
            accountAdapter = AccountAdapter( listAccount)
            recyclerview.adapter = accountAdapter

        }
        checkAccount()
        addAccount()
    }

    private fun addAccount() {
        binding.btnRegisted.setOnClickListener {
            Util.showDialog(requireContext(), "Wait A Second")
            val name = binding.edtName.text.toString()
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPass.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || !isValidEmail(email) || !isValidPassword(password)) {
                // Hiển thị lỗi và focus vào EditText rỗng
                if (name.isEmpty()) {
                    binding.edtName.error = "Please fill this"
                    binding.edtName.requestFocus()
                } else {
                    binding.edtName.error = null
                }
                if (email.isEmpty() || !isValidEmail(email)) {
                    binding.edtEmail.error = if (email.isEmpty()) "Please fill with email" else "Invalid email"
                    binding.edtEmail.requestFocus()
                } else {
                    binding.edtEmail.error = null
                }
                if (password.isEmpty() || !isValidPassword(password)) {
                    binding.edtPass.error = if (password.isEmpty()) "Please fill with password ex:Andiez123@" else "Invalid password"
                    binding.edtPass.requestFocus()
                } else {
                    binding.edtPass.error = null
                }
                Util.hideDialog() // Hide loading dialog when errors are present.
            } else {
                // Tất cả điều kiện đều đúng, gọi creatUser() và chuyển sang màn hình đăng nhập
                creatUser()
            }
        }
    }

    fun creatUser() {
        val email = binding.edtEmail.text.toString()
        val password = binding.edtPass.text.toString()
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .await()
                    withContext(Dispatchers.Main) {
                        Util.hideDialog()
                        Toast.makeText(requireContext(), "Registed Success", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_registedFragment_to_loginFragment)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Util.hideDialog()
                        Toast.makeText(
                            requireContext(),
                            "Registed Failed: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
//    private fun isValidPassword(password: String): Boolean {
//        val passwordPattern = "^[A-Z].*(?=.*[0-9])(?=.*[^a-zA-Z0-9]).*$"
//        val pattern = Regex(passwordPattern)
//        return pattern.matches(password)
//    }

    private fun checkAccount() {
        val addName = binding.edtName.text?.trim()
        val addEmail = binding.edtEmail.text?.trim()
        val addPass = binding.edtPass.text?.trim()
        val _fetchData = MutableLiveData<List<Account>>()
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                if (addName.isNullOrEmpty() && !addEmail.isNullOrEmpty() && !addPass.isNullOrEmpty()) {
                    val newAccount =
                        Account(
                            name = addName.toString(),
                            email = addEmail.toString(),
                            pass = addPass.toString()
                        )
                    accountViewModel.insertAccount(newAccount)
                    listAccount.add(newAccount)
                    binding.edtName.text?.clear()
                    binding.edtEmail.text?.clear()
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
                    binding.edtEmail.requestFocus()
                    binding.edtPass.requestFocus()
                    binding.edtName.requestFocus()
                    binding.edtName.error = null
                    binding.edtPass.error = null
                    binding.edtEmail.error = null

                }
            }
        }
    }

    private fun setColorButton() {
        binding.edtEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                updateButtonColor() // Gọi hàm updateButtonColor để kiểm tra tất cả điều kiện
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.edtPass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                updateButtonColor() // Gọi hàm updateButtonColor để kiểm tra tất cả điều kiện
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.edtName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                updateButtonColor() // Gọi hàm updateButtonColor để kiểm tra tất cả điều kiện
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

    }

    fun isValidEmail(email: String?): Boolean {
        // Logic kiểm tra email (ví dụ: sử dụng Regex)
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String?): Boolean {
        // Logic kiểm tra mật khẩu (ví dụ: sử dụng Regex)
        val passwordPattern = "^[A-Z].*(?=.*[0-9])(?=.*[^a-zA-Z0-9]).*$"
        val pattern = Regex(passwordPattern)
        return password?.let { pattern.matches(it) } ?: false
    }

    fun updateButtonColor() {
        val email = binding.edtEmail.text?.toString()
        val password = binding.edtPass.text?.toString()
        val name = binding.edtName.text?.toString()

        if (isValidEmail(email) && isValidPassword(password) && !name.isNullOrEmpty()) {
            binding.btnRegisted.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.my_blue)
            )
        } else {
            binding.btnRegisted.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.gray)
            )
        }
    }


}

