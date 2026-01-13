package com.example.skripsta

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.skripsta.viewmodel.PinLockViewModel
import com.example.skripsta.databinding.FragmentPinSettingsBinding

class PinSettingsFragment : Fragment() {

    // ViewBinding untuk mengakses UI
    private var _binding: FragmentPinSettingsBinding? = null
    private val binding get() = _binding!!

    // ViewModel untuk mengelola data PIN
    private val viewModel: PinLockViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate layout menggunakan ViewBinding
        _binding = FragmentPinSettingsBinding.inflate(inflater, container, false)

        // Mengatur toolbar (judul dan tombol kembali)
        setupToolbar()

        // Update tampilan berdasarkan status PIN
        updateUI()

        // Tombol set / change PIN
        binding.btnSetPin.setOnClickListener {

            // Menentukan mode: create atau change
            val mode = if (viewModel.hasPin(requireContext())) "change" else "create"

            // Navigasi ke PinLockFragment dengan mode yang sesuai
            val action =
                PinSettingsFragmentDirections
                    .actionPinSettingsFragmentToPinLockFragment(mode)

            findNavController().navigate(action)
        }

        // Tombol hapus PIN
        binding.btnRemovePin.setOnClickListener {
            showDeletePinConfirmation()
        }

        return binding.root
    }

    // Mengatur toolbar
    private fun setupToolbar() {

        // Tombol kembali ke halaman pengaturan
        binding.btnBack.setOnClickListener {
            val action =
                PinSettingsFragmentDirections
                    .actionPinSettingsFragmentToPengaturanFragment()
            findNavController().navigate(action)
        }

        // Judul toolbar
        binding.tvToolbarTitle.text = "Pin Lock"
    }

    // Menampilkan dialog konfirmasi penghapusan PIN
    private fun showDeletePinConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete PIN")
            .setMessage("Are you sure you want to delete the PIN? App security will be disabled.")
            .setPositiveButton("Delete") { _, _ ->

                // Menghapus PIN dari penyimpanan
                viewModel.deletePin(requireContext())

                // Menampilkan pesan berhasil
                Toast.makeText(
                    requireContext(),
                    "PIN deleted successfully",
                    Toast.LENGTH_SHORT
                ).show()

                // Update UI setelah PIN dihapus
                updateUI()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // Menutup dialog
                dialog.dismiss()
            }
            .show()
    }

    // Mengatur tampilan berdasarkan apakah PIN tersedia
    private fun updateUI() {
        val hasPin = viewModel.hasPin(requireContext())

        // Tombol hapus hanya tampil jika PIN ada
        binding.btnRemovePin.visibility =
            if (hasPin) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Membersihkan binding untuk mencegah memory leak
        _binding = null
    }
}
