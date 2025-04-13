package com.example.andiezstore.user.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.andiezstore.R
import com.example.andiezstore.databinding.FragmentPhoneBinding

class PhoneFragment : Fragment() {
    private lateinit var binding: FragmentPhoneBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPhoneBinding.inflate(layoutInflater)
        setControler()
        return binding.root
    }

    private fun setControler() {
        binding.btnLogin.setOnClickListener {
            val number = binding.edtPhone.text.toString()
            if (number.length == 10) {
                val bundle = Bundle()
                bundle.putString("phoneNumber", number)
                findNavController().navigate(R.id.action_phoneFragment_to_otpFragment, bundle)
            } else {
                Toast.makeText(requireContext(), "Wrong number", Toast.LENGTH_SHORT).show()
            }
        }

        binding.edtPhone.addTextChangedListener(object : TextWatcher { // Theo dõi edtPhone
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                updateButtonColor()
            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0?.length!! > 1) {
                }
            }
        })
    }

    fun updateButtonColor() {
        val phone = binding.edtPhone.text?.toString()

        phone?.length?.let {
            if (it == 10) {
                binding.btnLogin.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.white_blue)
                )
            } else {
                binding.btnLogin.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.gray)
                )
            }
        }
    }
}