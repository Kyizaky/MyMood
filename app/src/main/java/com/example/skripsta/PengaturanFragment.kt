package com.example.skripsta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.skripsta.databinding.FragmentPengaturanBinding
import androidx.fragment.app.viewModels
import com.example.skripsta.viewmodel.PinLockViewModel

class PengaturanFragment : Fragment() {

    // ViewBinding untuk mengakses komponen UI pada fragment pengaturan
    private var _binding: FragmentPengaturanBinding? = null
    private val binding get() = _binding!!

    // ViewModel untuk mengelola logika PIN keamanan
    private val pinViewModel: PinLockViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate layout menggunakan ViewBinding
        _binding = FragmentPengaturanBinding.inflate(inflater, container, false)

        // Tombol menuju halaman pengingat
        binding.btnPengingat.setOnClickListener {
            val action =
                PengaturanFragmentDirections
                    .actionPengaturanFragmentToReminderFragment()

            findNavController().navigate(action)
        }

        // Tombol pengaturan PIN
        binding.btnPin.setOnClickListener {

            // Mengecek apakah PIN sudah pernah dibuat
            if (pinViewModel.hasPin(requireContext())) {

                // Menyimpan target verifikasi PIN
                findNavController()
                    .currentBackStackEntry
                    ?.savedStateHandle
                    ?.set("pin_verified_target", "pin_settings")

                // Navigasi ke halaman verifikasi PIN
                val action =
                    PengaturanFragmentDirections
                        .actionPengaturanFragmentToPinLockFragment("login")

                findNavController().navigate(action)

            } else {

                // Navigasi ke halaman pembuatan PIN baru
                val action =
                    PengaturanFragmentDirections
                        .actionPengaturanFragmentToPinSettingsFragment()

                findNavController().navigate(action)
            }
        }

        // Tombol menuju halaman backup data
        binding.btnBackup.setOnClickListener {
            val action =
                PengaturanFragmentDirections
                    .actionPengaturanFragmentToBackUpFragment()

            findNavController().navigate(action)
        }

        // Tombol menuju halaman ekspor dan impor data
        binding.btnExportImportdata.setOnClickListener {
            val action =
                PengaturanFragmentDirections
                    .actionPengaturanFragmentToExportImportFragment()

            findNavController().navigate(action)
        }

        // Mengembalikan root view dari binding
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Membersihkan binding untuk mencegah memory leak
        _binding = null
    }
}
