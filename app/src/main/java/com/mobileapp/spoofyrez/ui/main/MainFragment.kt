package com.mobileapp.spoofyrez.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mobileapp.spoofyrez.R
import com.mobileapp.spoofyrez.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnGetStarted.setOnClickListener {
            val action = MainFragmentDirections.actionMainToParameters()
            findNavController().navigate(action)
        }

        binding.btnHelp.setOnClickListener {
            findNavController().navigate(R.id.action_main_to_help)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}