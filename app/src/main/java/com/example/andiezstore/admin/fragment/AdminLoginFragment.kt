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
import com.google.firebase.auth.FirebaseAuth // Import Firebase Auth
import com.google.firebase.database.FirebaseDatabase // Import Firebase Database
import kotlinx.coroutines.launch
import com.example.andiezstore.admin.model.Account // Import your Account data class

class AdminLoginFragment : Fragment() {
    private lateinit var binding: FragmentAdminLoginBinding
    private lateinit var auth: FirebaseAuth


    private val ADMIN_EMAIL = "admin@thienan.com"
    private val ADMIN_PASSWORD = "123456"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminLoginBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance() // Initialize Firebase Auth
        setColorButton()
        checkToLogin()
        return binding.root
    }

    private fun checkToLogin() {
        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPass.text.toString().trim()

            if (email.isEmpty()) {
                binding.edtEmail.error = "Account cannot be empty"
                binding.edtEmail.requestFocus()
            } else if (password.isEmpty()) {
                binding.edtPass.error = "Password cannot be empty"
                binding.edtPass.requestFocus()
            } else {
                loginUser(email, password) // Pass email and password to loginUser
            }
        }
    }

    fun loginUser(inputEmail: String, inputPassword: String) {
        Util.showDialog(requireContext(), "Authenticating...")

        lifecycleScope.launch {
            if (inputEmail.equals(ADMIN_EMAIL, ignoreCase = true) && inputPassword == ADMIN_PASSWORD) {
                // Use Firebase Authentication to sign in the admin
                auth.signInWithEmailAndPassword(inputEmail, inputPassword)
                    .addOnCompleteListener(requireActivity()) { task ->
                        Util.hideDialog()
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            val user = auth.currentUser
                            user?.let { firebaseUser ->
                                // Optional: Store admin profile details in Realtime Database under their UID
                                val adminAccountData = Account(account = ADMIN_EMAIL.substringBefore("@")) // Store just the 'admin' part
                                FirebaseDatabase.getInstance().getReference("Admin")
                                    .child("Account")
                                    .child(firebaseUser.uid)
                                    .setValue(adminAccountData)
                                    .addOnSuccessListener {
                                        Log.d("FirebaseAdmin", "Admin profile saved to RTDB for UID: ${firebaseUser.uid}")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("FirebaseAdmin", "Failed to save admin profile to RTDB: ${e.message}")
                                    }

                                Toast.makeText(
                                    requireContext(),
                                    "Login Success: Welcome, Admin!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                findNavController().navigate(R.id.action_adminLoginFragment_to_adminHomeFragment)
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("FirebaseAuth", "signInWithEmail:failure", task.exception)
                            Toast.makeText(
                                requireContext(),
                                "Login Failed: Authentication failed. Check credentials or create admin account.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            } else {
                // If hardcoded values don't match, or Firebase Auth fails for some reason
                Util.hideDialog()
                Toast.makeText(
                    requireContext(),
                    "Login Failed: Incorrect hardcoded admin credentials.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setColorButton() {
        binding.edtEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                updateButtonColor()
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.edtPass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                updateButtonColor()
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    fun updateButtonColor() {
        val email = binding.edtEmail.text?.toString()?.trim()
        val password = binding.edtPass.text?.toString()?.trim()

        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
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