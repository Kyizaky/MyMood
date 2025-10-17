package com.example.skripsta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.skripsta.databinding.FragmentPinLockBinding
import com.google.android.material.snackbar.Snackbar

class PinLockFragment : Fragment() {

    private var _binding: FragmentPinLockBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PinLockViewModel by viewModels()

    private var enteredPin = ""
    private var tempPin = ""
    private var hasExistingPin = false
    private var mode = Mode.LOGIN
    private var currentStep = Step.ENTER_PIN

    private enum class Mode {
        LOGIN,
        CHANGE_PIN
    }

    private enum class Step {
        ENTER_PIN,
        ENTER_OLD_PIN,
        ENTER_NEW_PIN,
        CONFIRM_NEW_PIN
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPinLockBinding.inflate(inflater, container, false)

        // Ambil mode dari argument
        mode = when (arguments?.getString("mode")) {
            "change" -> Mode.CHANGE_PIN
            else -> Mode.LOGIN
        }

        hasExistingPin = viewModel.hasPin(requireContext())

        // Tentukan langkah awal
        currentStep = when {
            !hasExistingPin -> Step.ENTER_NEW_PIN // user pertama kali buat PIN
            mode == Mode.CHANGE_PIN -> Step.ENTER_OLD_PIN
            else -> Step.ENTER_PIN
        }

        binding.btnBack.visibility = if (mode == Mode.CHANGE_PIN) View.VISIBLE else View.GONE

         binding.btnBack.setOnClickListener {
             findNavController().popBackStack()
         }

        binding.btnDeletePin.setOnClickListener {
            if (viewModel.hasPin(requireContext())) {
                viewModel.deletePin(requireContext())
                hasExistingPin = false
                Snackbar.make(binding.root, "PIN deleted successfully", Snackbar.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_pinLockFragment_to_pengaturanFragment)
            } else {
                Snackbar.make(binding.root, "No PIN saved", Snackbar.LENGTH_SHORT).show()
            }
        }


        setupButtons()
        updatePinDots()
        updateGuideText()

        return binding.root
    }

    private fun setupButtons() {
        val numberButtons = listOf(
            binding.btn0, binding.btn1, binding.btn2, binding.btn3, binding.btn4,
            binding.btn5, binding.btn6, binding.btn7, binding.btn8, binding.btn9
        )

        numberButtons.forEach { button ->
            button.setOnClickListener {
                if (enteredPin.length < 4) {
                    enteredPin += button.text
                    updatePinDots()
                }
            }
        }

        binding.btnBackspace.setOnClickListener {
            if (enteredPin.isNotEmpty()) {
                enteredPin = enteredPin.dropLast(1)
                updatePinDots()
            }
        }

        binding.btnOk.setOnClickListener { handleStepLogic() }
    }

    private fun handleStepLogic() {
        if (enteredPin.length < 4) {
            return
        }

        when (currentStep) {

            // Mode login
            Step.ENTER_PIN -> {
                if (viewModel.verifyPin(requireContext(), enteredPin)) {
                    findNavController().navigate(
                        R.id.action_pinLockFragment_to_homeFragment,
                        null,
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.pinLockFragment, true) // hapus PinLock dari back stack
                            .setLaunchSingleTop(true) // hindari duplikat home fragment
                            .build()
                    )
                } else {
                    Snackbar.make(binding.root, "PIN salah", Snackbar.LENGTH_SHORT).show()
                }
                enteredPin = ""
                updatePinDots()
            }

            // Ubah PIN - verifikasi PIN lama
            Step.ENTER_OLD_PIN -> {
                if (viewModel.verifyPin(requireContext(), enteredPin)) {
                    currentStep = Step.ENTER_NEW_PIN
                } else {
                    Snackbar.make(binding.root, "Incorrect old PIN", Snackbar.LENGTH_SHORT).show()
                }
                enteredPin = ""
                updatePinDots()
                updateGuideText()
            }

            // Ubah PIN - masukkan PIN baru
            Step.ENTER_NEW_PIN -> {
                tempPin = enteredPin
                currentStep = Step.CONFIRM_NEW_PIN
                enteredPin = ""
                updatePinDots()
                updateGuideText()
            }

            // Ubah PIN - konfirmasi PIN baru
            Step.CONFIRM_NEW_PIN -> {
                if (enteredPin == tempPin) {
                    viewModel.savePin(requireContext(), enteredPin)
                    val message = if (mode == Mode.CHANGE_PIN) "PIN berhasil diubah" else "PIN berhasil dibuat"
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_pinLockFragment_to_pengaturanFragment)
                } else {
                    Snackbar.make(binding.root, "PINs do not match, please try again", Snackbar.LENGTH_SHORT).show()
                    currentStep = Step.ENTER_NEW_PIN
                }
                enteredPin = ""
                updatePinDots()
                updateGuideText()
            }
        }
    }

    private fun updateGuideText() {
        binding.tvGuide.text = when (currentStep) {
            Step.ENTER_PIN -> "Enter your PIN"
            Step.ENTER_OLD_PIN -> "Enter your old PIN"
            Step.ENTER_NEW_PIN -> "Enter your new PIN"
            Step.CONFIRM_NEW_PIN -> "Confirm your new PIN"
        }
    }

    private fun updatePinDots() {
        val dots = listOf(binding.pinDot1, binding.pinDot2, binding.pinDot3, binding.pinDot4)
        dots.forEachIndexed { index, view ->
            val drawableId = if (index < enteredPin.length)
                R.drawable.pin_dot_filled
            else
                R.drawable.pin_dot_empty
            view.setBackgroundResource(drawableId)
        }

        binding.btnDeletePin.visibility = if (hasExistingPin && currentStep != Step.ENTER_PIN) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
