package com.example.andiezstore.user.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.andiezstore.ui.MainActivity
import com.example.andiezstore.R
import com.example.andiezstore.databinding.FragmentOtpBinding
import com.example.andiezstore.user.viewmodel.OtpViewModel
import com.example.andiezstore.utils.Util
import kotlinx.coroutines.launch


class OtpFragment : Fragment() {
    private lateinit var binding: FragmentOtpBinding
    var phoneNumber: String = ""
    private val viewModel :OtpViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOtpBinding.inflate(layoutInflater)
        getPhoneNumber()
        autoFocusEdt()
        sendOtpToUser()
        buttonNextClick()
        return binding.root
    }

    private fun buttonNextClick() {
        Util.showDialog(requireContext(),"Verify otp")
        var str_otp=""
        binding.btnNext.setOnClickListener{
            val edt_array = arrayOf(
                binding.edtOtp1,
                binding.edtOtp2,
                binding.edtOtp3,
                binding.edtOtp4,
                binding.edtOtp5,
                binding.edtOtp6
            )
            str_otp=edt_array.joinToString("") { it.text.toString()}
            if(str_otp.length==6){
                edt_array.forEach { it.text?.clear() }
                viewModel.signInWithPhoneAuthCredential(str_otp)
                lifecycleScope.launch {
                    viewModel.isLogin.collect{
                        if (it){
                            Util.showDialog(requireContext(),"Wait a second")
                            Toast.makeText(requireContext(),"Login Success",Toast.LENGTH_SHORT).show()
                            val intent = Intent(activity, MainActivity::class.java)
                            startActivity(intent)

                        }
                    }
                }
            }
            else{
                Util.hideDialog()
                Toast.makeText(requireContext(),"Otp Error",Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun sendOtpToUser() {
        viewModel.sendOtp(requireActivity(),phoneNumber)
        lifecycleScope.launch{
            viewModel.isOtpSend.collect{
                if(it){
                    Util.hideDialog()
                    Toast.makeText(requireContext(),"Otp sent",Toast.LENGTH_SHORT).show()
                }
            }
        }


    }

    private fun autoFocusEdt() {
        val edt_array = arrayOf(
            binding.edtOtp1,
            binding.edtOtp2,
            binding.edtOtp3,
            binding.edtOtp4,
            binding.edtOtp5,
            binding.edtOtp6
        )
        for (i in edt_array.indices) {
            edt_array[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (p0?.length == 1) {
                        if (i < 5) {
                            edt_array[i + 1].requestFocus()
                        }
                    } else if (p0?.length == 0) {
                        if (i > 0) {
                            edt_array[i - 1].requestFocus()
                        }
                    }
                }

                override fun afterTextChanged(p0: Editable?) {
                    if (p0?.length !! > 1) {
                        Toast.makeText(requireContext(),"Error",Toast.LENGTH_SHORT).show()
                    }
                }

            })
        }
    }

    private fun getPhoneNumber() {
        val bundle = arguments
        phoneNumber = bundle?.getString("phoneNumber").toString()
        binding.tvPhone.text = "We have just sent a code to +84 ${phoneNumber.substring(1)}"
    }
}