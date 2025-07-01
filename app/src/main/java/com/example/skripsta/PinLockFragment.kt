package com.example.skripsta

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.skripsta.databinding.FragmentPinLockBinding
import com.google.android.material.snackbar.Snackbar

class PinLockFragment : Fragment() {

    private var _binding: FragmentPinLockBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PinLockViewModel by viewModels()
    private var isSettingNewPin = false
    private var tempPin = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPinLockBinding.inflate(inflater, container, false)

        // Check if PIN exists
        isSettingNewPin = !viewModel.hasPin(requireContext())
        updateUI()

        setupNumberPad()
        setupClearButton()
        setupConfirmButton()

        return binding.root
    }

    private fun updateUI() {
        binding.tvPinPrompt.text = when {
            isSettingNewPin -> "Set New PIN"
            tempPin.isNotEmpty() -> "Confirm PIN"
            else -> "Enter PIN"
        }
        binding.btnConfirm.isEnabled = binding.pinInput.text.length == 4
    }

    private fun setupNumberPad() {
        val numberButtons = listOf(
            binding.btn0, binding.btn1, binding.btn2, binding.btn3, binding.btn4,
            binding.btn5, binding.btn6, binding.btn7, binding.btn8, binding.btn9
        )

        numberButtons.forEach { button ->
            button.setOnClickListener {
                if (binding.pinInput.text.length < 4) {
                    binding.pinInput.append(button.text)
                    updateUI()
                }
            }
        }
    }

    private fun setupClearButton() {
        binding.btnClear.setOnClickListener {
            binding.pinInput.text.clear()
            updateUI()
        }
    }

    private fun setupConfirmButton() {
        binding.btnConfirm.setOnClickListener {
            val enteredPin = binding.pinInput.text.toString()

            when {
                isSettingNewPin -> {
                    tempPin = enteredPin
                    isSettingNewPin = false
                    binding.pinInput.text.clear()
                    updateUI()
                    binding.tvPinPrompt.text = "Confirm New PIN"
                }
                tempPin.isNotEmpty() -> {
                    if (enteredPin == tempPin) {
                        viewModel.savePin(requireContext(), enteredPin)
                        Snackbar.make(binding.root, "PIN set successfully", Snackbar.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_pinLockFragment_to_pengaturanFragment)
                    } else {
                        Snackbar.make(binding.root, "PINs don't match", Snackbar.LENGTH_SHORT).show()
                        tempPin = ""
                        isSettingNewPin = true
                        binding.pinInput.text.clear()
                        updateUI()
                    }
                }
                else -> {
                    if (viewModel.verifyPin(requireContext(), enteredPin)) {
                        Snackbar.make(binding.root, "PIN verified", Snackbar.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_pinLockFragment_to_homeFragment)
                    } else {
                        Snackbar.make(binding.root, "Incorrect PIN", Snackbar.LENGTH_SHORT).show()
                        binding.pinInput.text.clear()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}