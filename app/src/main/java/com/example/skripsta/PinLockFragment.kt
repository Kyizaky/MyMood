package com.example.skripsta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.skripsta.viewmodel.PinLockViewModel
import com.example.skripsta.databinding.FragmentPinLockBinding
import com.google.android.material.snackbar.Snackbar

class PinLockFragment : Fragment() {

    // ViewBinding untuk mengakses komponen UI
    private var _binding: FragmentPinLockBinding? = null
    private val binding get() = _binding!!

    // ViewModel untuk mengelola logika PIN
    private val viewModel: PinLockViewModel by viewModels()

    // Argument dari Navigation Component
    private val args: PinLockFragmentArgs by navArgs()

    // Menyimpan PIN yang sedang diketik
    private var enteredPin = ""

    // Menyimpan PIN sementara saat proses pembuatan / penggantian
    private var tempPin = ""

    // Menyimpan langkah saat ini
    private var currentStep = Step.ENTER_PIN

    // Mode penggunaan PIN
    private var mode = Mode.LOGIN

    // Enum untuk mode PIN
    private enum class Mode {
        LOGIN,        // Mode verifikasi PIN
        CREATE_PIN,   // Mode membuat PIN baru
        CHANGE_PIN    // Mode mengganti PIN
    }

    // Enum untuk tahapan input PIN
    private enum class Step {
        ENTER_PIN,        // Memasukkan PIN
        ENTER_OLD_PIN,    // Memasukkan PIN lama
        ENTER_NEW_PIN,    // Memasukkan PIN baru
        CONFIRM_NEW_PIN   // Konfirmasi PIN baru
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate layout menggunakan ViewBinding
        _binding = FragmentPinLockBinding.inflate(inflater, container, false)

        // Menentukan mode dan langkah awal
        setupMode()

        // Mengatur tombol angka dan kontrol PIN
        setupButtons()

        // Menampilkan teks panduan awal
        updateGuideText()

        // Menampilkan titik PIN awal
        updatePinDots()

        // Tombol kembali
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        return binding.root
    }

    // Mengatur mode PIN berdasarkan argument
    private fun setupMode() {
        mode = when (args.mode) {
            "create" -> Mode.CREATE_PIN
            "change" -> Mode.CHANGE_PIN
            else -> Mode.LOGIN
        }

        // Menentukan langkah awal berdasarkan mode
        currentStep = when (mode) {
            Mode.LOGIN -> Step.ENTER_PIN
            Mode.CREATE_PIN -> Step.ENTER_NEW_PIN
            Mode.CHANGE_PIN -> Step.ENTER_OLD_PIN
        }

        // Tombol back hanya tampil jika bukan mode login
        binding.btnBack.visibility =
            if (mode == Mode.LOGIN) View.GONE else View.VISIBLE
    }

    // Mengatur tombol angka, hapus, dan OK
    private fun setupButtons() {

        // Daftar tombol angka
        val numberButtons = listOf(
            binding.btn1, binding.btn2, binding.btn3,
            binding.btn4, binding.btn5, binding.btn6,
            binding.btn7, binding.btn8, binding.btn9,
            binding.btn0
        )

        // Listener tombol angka
        numberButtons.forEach { btn ->
            btn.setOnClickListener {
                if (enteredPin.length < 4) {
                    enteredPin += btn.text
                    updatePinDots()
                }
            }
        }

        // Tombol hapus PIN
        binding.btnBackspace.setOnClickListener {
            if (enteredPin.isNotEmpty()) {
                enteredPin = enteredPin.dropLast(1)
                updatePinDots()
            }
        }

        // Tombol OK untuk memproses PIN
        binding.btnOk.setOnClickListener {
            if (enteredPin.length == 4) {
                handlePinLogic()
            }
        }
    }

    // Logika utama pengolahan PIN
    private fun handlePinLogic() {
        when (currentStep) {

            // Verifikasi PIN
            Step.ENTER_PIN -> {
                if (viewModel.verifyPin(requireContext(), enteredPin)) {

                    // Mengecek target setelah PIN diverifikasi
                    val target =
                        findNavController()
                            .previousBackStackEntry
                            ?.savedStateHandle
                            ?.get<String>("pin_verified_target")

                    if (target == "pin_settings") {
                        findNavController().navigate(
                            PinLockFragmentDirections
                                .actionPinLockFragmentToPinSettingsFragment()
                        )
                    } else {
                        findNavController().popBackStack()
                    }

                } else {
                    showError("Incorrect PIN")
                }
            }

            // Verifikasi PIN lama
            Step.ENTER_OLD_PIN -> {
                if (viewModel.verifyPin(requireContext(), enteredPin)) {
                    currentStep = Step.ENTER_NEW_PIN
                } else {
                    showError("Incorrect current PIN")
                }
            }

            // Menyimpan PIN baru sementara
            Step.ENTER_NEW_PIN -> {
                tempPin = enteredPin
                currentStep = Step.CONFIRM_NEW_PIN
            }

            // Konfirmasi PIN baru
            Step.CONFIRM_NEW_PIN -> {
                if (enteredPin == tempPin) {
                    viewModel.savePin(requireContext(), enteredPin)
                    Snackbar.make(binding.root, "PIN saved successfully", Snackbar.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    showError("PINs do not match")
                    currentStep = Step.ENTER_NEW_PIN
                }
            }
        }

        // Reset input PIN setelah proses
        enteredPin = ""
        updatePinDots()
        updateGuideText()
    }

    // Memperbarui teks panduan sesuai langkah
    private fun updateGuideText() {
        binding.tvGuide.text = when (currentStep) {
            Step.ENTER_PIN -> "Enter your PIN"
            Step.ENTER_OLD_PIN -> "Enter your current PIN"
            Step.ENTER_NEW_PIN -> "Create a new PIN"
            Step.CONFIRM_NEW_PIN -> "Confirm your new PIN"
        }
    }

    // Memperbarui indikator titik PIN
    private fun updatePinDots() {
        val dots = listOf(
            binding.pinDot1,
            binding.pinDot2,
            binding.pinDot3,
            binding.pinDot4
        )

        dots.forEachIndexed { index, view ->
            view.setBackgroundResource(
                if (index < enteredPin.length)
                    R.drawable.pin_dot_filled
                else
                    R.drawable.pin_dot_empty
            )
        }
    }

    // Menampilkan pesan error
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Membersihkan binding untuk mencegah memory leak
        _binding = null
    }
}
