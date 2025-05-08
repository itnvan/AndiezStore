package com.example.andiezstore.admin.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.andiezstore.R
import com.example.andiezstore.databinding.FragmentAdminLoginBinding
import com.example.andiezstore.utils.Util
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class AdminLoginFragment : Fragment() {
    private lateinit var binding: FragmentAdminLoginBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminLoginBinding.inflate(layoutInflater)
        setColorButton()
        checkToLogin()
        return binding.root
    }

    private fun checkToLogin() {
        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPass.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Util.hideDialog()
                if (email.isEmpty()) {
                    binding.edtEmail.error = "Invalid email"
                    binding.edtEmail.requestFocus()
                } else {
                    binding.edtEmail.error = null
                }
                if (password.isEmpty()) {
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
        Util.showDialog(requireContext(), "Wait A Second")

        lifecycleScope.launch {
            try {
                // 1. Get a reference to the "Admin" node in your database
                val adminRef = FirebaseDatabase.getInstance().getReference("Admin")

                // 2. Use a ValueEventListener to check the data
                adminRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // 3. Check for the "account" and "password" children
                            val accountSnapshot = snapshot.child("Account")
                            val passwordSnapshot = snapshot.child("Password")

                            if (accountSnapshot.exists() && passwordSnapshot.exists()) {
                                // 4. Get the values
                                val storedAccount = accountSnapshot.value.toString()
                                val storedPassword = passwordSnapshot.value.toString()

                                // 5.  Perform the check (case-insensitive email comparison)
                                if (storedAccount.equals(
                                        email,
                                        ignoreCase = true
                                    ) && storedPassword == password
                                ) {
                                    // 6. Login is successful
                                    Util.hideDialog() // Hide dialog on success
                                    Toast.makeText(
                                        requireContext(),
                                        "Login Success",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    findNavController().navigate(R.id.action_adminLoginFragment_to_adminHomeFragment)
                                } else {
                                    // 7.  Login failed - incorrect credentials
                                    Util.hideDialog()
                                    Toast.makeText(
                                        requireContext(),
                                        "Login Failed: Incorrect account or password",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                // 8.  The expected structure ("account" or "password" child missing)
                                Util.hideDialog()
                                Toast.makeText(
                                    requireContext(),
                                    "Login Failed: Invalid data structure in database",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Util.hideDialog()
                            }
                        } else {
                            // 9.  "Admin" node does not exist
                            Util.hideDialog()
                            Toast.makeText(
                                requireContext(),
                                "Login Failed: Admin account not found",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // 10. Handle errors from Firebase
                        Util.hideDialog()
                        Toast.makeText(
                            requireContext(),
                            "Login Failed: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("Firebase", "Database error: ${error.message}") // Log the error
                    }
                })
            } catch (e: Exception) {
                // 11. Handle general exceptions (e.g., network issues)
                Util.hideDialog()
                Toast.makeText(
                    requireContext(),
                    "Login Failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("Exception", "General error: ${e.message}", e) // Log the exception
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

        if (email?.isEmpty() == true || password?.isEmpty() == true) {
            binding.btnLogin.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.gray)
            )
        } else {
            binding.btnLogin.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.white_blue)
            )
        }
    }

}
