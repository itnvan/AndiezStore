package com.example.andiezstore.user.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
        binding= FragmentPhoneBinding.inflate(layoutInflater)
        setControler()
        return binding.root
    }
    private fun setControler() {
        binding.btnLogin.setOnClickListener({
            val number = binding.edtPhone.text.toString()
            if (number.length == 10) {
                val bundle=Bundle()
                bundle.putString("phoneNumber",number)
                findNavController().navigate(R.id.action_phoneFragment_to_otpFragment,bundle)
            } else {
                Toast.makeText(requireContext(), "Wrong number", Toast.LENGTH_SHORT).show()
            }
        })
    }
}