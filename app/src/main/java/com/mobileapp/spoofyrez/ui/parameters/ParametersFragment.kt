package com.mobileapp.spoofyrez.ui.parameters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mobileapp.spoofyrez.R
import com.mobileapp.spoofyrez.databinding.FragmentParametersBinding

class ParametersFragment : Fragment() {
    private var _binding: FragmentParametersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentParametersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnFindSimilar.setOnClickListener {
            findNavController().navigate(R.id.action_parameters_to_results)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}