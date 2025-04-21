package com.example.andiezstore.admin.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.andiezstore.R
import com.example.andiezstore.databinding.FragmentAdminClassroomBinding

class AdminClassroomFragment : Fragment() {
    private val fromBottom = android.view.animation.AnimationUtils.loadAnimation(
        requireContext(),
        R.anim.from_bottom_anim
    )
    private val toBottom =
        android.view.animation.AnimationUtils.loadAnimation(requireContext(), R.anim.to_bottom_anim)
    private val rotateOpen = android.view.animation.AnimationUtils.loadAnimation(
        requireContext(),
        R.anim.rotate_open_anim
    )
    private val rotateClose = android.view.animation.AnimationUtils.loadAnimation(
        requireContext(),
        R.anim.rotate_close_anim
    )
    private lateinit var binding: FragmentAdminClassroomBinding
    private var clicked = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminClassroomBinding.inflate(layoutInflater)
        binding.btnMain.setOnClickListener {
            onAddButtonClicked()
        }
        binding.btnAdd.setOnClickListener {

        }
        binding.btnDelete.setOnClickListener {
            onDeleteButtonClicked()
        }
        binding.btnUpdate.setOnClickListener {
            onUpdateButtonClicked()
        }
        return binding.root
    }

    private fun onUpdateButtonClicked() {

    }

    private fun onDeleteButtonClicked() {

    }

    private fun onAddButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        clicked = !clicked
    }

    private fun setVisibility(clicked: Boolean) {
        if (!clicked) {
            binding.btnAdd.visibility = View.VISIBLE
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnUpdate.visibility = View.VISIBLE
        } else {
            binding.btnAdd.visibility = View.INVISIBLE
            binding.btnDelete.visibility = View.INVISIBLE
            binding.btnUpdate.visibility = View.INVISIBLE
        }
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            binding.btnAdd.startAnimation(fromBottom)
            binding.btnDelete.startAnimation(fromBottom)
            binding.btnUpdate.startAnimation(fromBottom)
            binding.btnMain.startAnimation(rotateOpen)
        } else {
            binding.btnAdd.startAnimation(toBottom)
            binding.btnDelete.startAnimation(toBottom)
            binding.btnUpdate.startAnimation(toBottom)
            binding.btnMain.startAnimation(rotateClose)
        }
    }
}