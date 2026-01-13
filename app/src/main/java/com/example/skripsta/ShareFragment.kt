package com.example.skripsta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.skripsta.viewmodel.MoodEntryViewModel
import com.example.skripsta.databinding.FragmentShareBinding
import com.example.skripsta.data.entity.MoodEntry
import com.example.skripsta.utils.ExcelUtils
import com.example.skripsta.utils.PdfUtils
import kotlinx.coroutines.launch

class ShareFragment : Fragment() {

    // Binding untuk menghubungkan fragment dengan layout XML
    private var _binding: FragmentShareBinding? = null
    private val binding get() = _binding!!

    // ViewModel untuk mengambil data mood dari database
    private val moodEntryViewModel: MoodEntryViewModel by viewModels()

    // Menyimpan daftar mood yang sedang aktif
    private var currentMoodList = emptyList<MoodEntry>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inisialisasi binding
        _binding = FragmentShareBinding.inflate(inflater, container, false)
        val view = binding.root

        // Mengamati perubahan data mood dari database
        moodEntryViewModel.readAllData.observe(viewLifecycleOwner) { moodList ->
            currentMoodList = moodList
        }

        // Tombol kembali ke fragment sebelumnya
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Tombol berbagi data mood dalam format PDF
        binding.btnSharePdf.setOnClickListener {

            // Cek apakah data mood kosong
            if (currentMoodList.isEmpty()) {
                showEmptyWarning()
                return@setOnClickListener
            }

            // Proses ekspor dan berbagi PDF
            handleExportAction {
                PdfUtils.shareMoodEntriesAsPdf(
                    requireContext(),
                    currentMoodList
                )
            }
        }

        // Tombol berbagi data mood dalam format CSV/Excel
        binding.btnShareCsv.setOnClickListener {

            // Cek apakah data mood kosong
            if (currentMoodList.isEmpty()) {
                showEmptyWarning()
                return@setOnClickListener
            }

            // Proses ekspor dan berbagi file Excel
            handleExportAction {
                ExcelUtils.shareMoodEntriesAsExcel(
                    requireContext(),
                    currentMoodList
                )
            }
        }

        return view
    }

    // Menangani proses ekspor data menggunakan coroutine
    private fun handleExportAction(action: suspend () -> Unit) {

        // Validasi ulang jika data kosong
        if (currentMoodList.isEmpty()) {
            showEmptyWarning()
            return
        }

        // Tampilkan loading
        showLoading(true)

        lifecycleScope.launch {
            try {
                // Jalankan aksi ekspor (PDF / Excel)
                action()
            } catch (e: Exception) {
                // Menangani error saat ekspor
                e.printStackTrace()
                Toast.makeText(
                    requireContext(),
                    "❌ Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                // Sembunyikan loading setelah proses selesai
                showLoading(false)
            }
        }
    }

    // Menampilkan peringatan jika tidak ada data mood
    private fun showEmptyWarning() {
        Toast.makeText(
            requireContext(),
            "⚠️ No mood data available to export!",
            Toast.LENGTH_SHORT
        ).show()
    }

    // Mengatur tampilan loading dan status tombol
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility =
            if (isLoading) View.VISIBLE else View.GONE

        binding.tvWarning.visibility =
            if (isLoading) View.GONE else View.VISIBLE

        binding.btnSharePdf.isEnabled = !isLoading
        binding.btnShareCsv.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Menghindari memory leak
        _binding = null
    }
}
