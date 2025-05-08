package com.example.andiezstore.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.andiezstore.admin.AdminMainActivity
import com.example.andiezstore.databinding.FragmentChoiceBinding
import com.example.andiezstore.utils.Util
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.jvm.java


class ChoiceFragment : Fragment() {
    private lateinit var binding: FragmentChoiceBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentChoiceBinding.inflate(layoutInflater)
        binding.imgStudent.setOnClickListener {
            if (binding.imgStudent.isClickable) {
                Util.showDialog(requireContext(), "Wait a second")
                binding.imgStudent.isClickable = false // Ngăn click nhiều lần trong khi chờ

                lifecycleScope.launch {
                    delay(2000L) // Chờ 2 giây (2000 milliseconds)
                    Util.hideDialog() // Ẩn dialog sau 3 giây
                    findNavController().navigate(com.example.andiezstore.R.id.action_choiceFragment_to_loginFragment)
                    binding.imgStudent.isClickable = true // Cho phép click lại sau khi chuyển màn
                }
            }
        }

        binding.imgTeacher.setOnClickListener {
            if (binding.imgTeacher.isClickable) {
                Util.showDialog(requireContext(), "Wait a second")
                binding.imgTeacher.isClickable = false // Ngăn click nhiều lần trong khi chờ

                lifecycleScope.launch {
                    delay(2000L) // Chờ 3 giây (3000 milliseconds)
                    Util.hideDialog() // Ẩn dialog sau 3 giây
                    val intent = Intent(requireContext(), AdminMainActivity::class.java)
                    startActivity(intent)
                    binding.imgTeacher.isClickable = true // Cho phép click lại sau khi chuyển màn
                }
            }
        }
        return binding.root
    }
}
