package com.example.andiezstore.user.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.room.Database
import com.example.andiezstore.R
import com.example.andiezstore.databinding.FragmentLoginBinding
import com.example.andiezstore.utils.Util
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.toString

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var database: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        setColorButton()
        checkToLogin()
        binding.tvRegisted.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registedFragment)
        }
        binding.tvPhone.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_phoneFragment)
        }
        return binding.root
    }

    private fun checkToLogin() {
        binding.btnLogin.setOnClickListener {
            Util.showDialog(requireContext(), "Wait A Second")
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPass.text.toString()

            if (email.isEmpty() || !isValidEmail(email) || password.isEmpty() || !isValidPassword(password)) {
                // Hiển thị lỗi và focus vào EditText rỗng
                Util.hideDialog()
                if (email.isEmpty() || !isValidEmail(email)) {
                    binding.edtEmail.error = "Invalid email"
                    binding.edtEmail.requestFocus()
                } else {
                    binding.edtEmail.error = null
                }
                if (password.isEmpty() || !isValidPassword(password)) {
                    binding.edtPass.error = "Invalid password"
                    binding.edtPass.requestFocus()
                } else {
                    binding.edtPass.error = null
                }
            } else {
                // Email và mật khẩu hợp lệ, gọi loginUser()
                loginUser()
            }
        }
    }

    fun loginUser() {
        val email = binding.edtEmail.text.toString()
        val password = binding.edtPass.text.toString()
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).await()
                    withContext(Dispatchers.Main) {
                        Util.hideDialog()
                        Toast.makeText(requireContext(), "Login Success", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Util.hideDialog()
                        Toast.makeText(
                            requireContext(),
                            "Login Failed: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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
    }

    fun updateButtonColor() {
        val email = binding.edtEmail.text?.toString()
        val password = binding.edtPass.text?.toString()

        if (isValidEmail(email) && isValidPassword(password)) {
            binding.btnLogin.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.white_blue)
            )
        } else {
            binding.btnLogin.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.gray)
            )
        }
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
}



