package com.example.andiezstore.user.account

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.example.andiezstore.R
import com.example.andiezstore.databinding.FragmentPhoneBinding
import com.example.andiezstore.user.fragments.RegistedFragment

class PhoneFragment : Fragment() {
    private lateinit var binding: FragmentPhoneBinding
    private lateinit var tvRegisted: TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPhoneBinding.inflate(layoutInflater)
        setControler()
        setColorButton()
        checkToLogin()
        tvRegisted.setOnClickListener {
            val intent = Intent(activity, RegistedFragment::class.java)
            startActivity(intent)
        }
        return binding.root
    }

    private fun checkToLogin() {

    }

    private fun setColorButton() {
        binding.edtPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0?.length == 10) {
                    binding.btnNext.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.my_blue
                        )
                    )
                } else {
                    binding.btnNext.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.gray
                        )
                    )
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    private fun setControler() {
        binding.btnNext.setOnClickListener({
            val number = binding.edtPhone.text.toString()
            if (number.length == 10) {
                val bundle = Bundle()
                bundle.putString("phoneNumber", number)
                findNavController().navigate(
                    R.id.action_action_phoneFragment_to_action_otpFragment,
                    bundle
                )
            } else {
                Toast.makeText(requireContext(), "Wrong number", Toast.LENGTH_SHORT).show()
            }
        })
    }

}