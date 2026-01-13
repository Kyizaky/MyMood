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
import com.example.skripsta.databinding.FragmentBackUpBinding
import com.example.skripsta.data.entity.MoodEntry
import com.example.skripsta.utils.ExcelUtils
import com.example.skripsta.utils.PdfUtils
import kotlinx.coroutines.launch

class BackUpFragment : Fragment() {

    private var _binding: FragmentBackUpBinding? = null
    private val binding get() = _binding!!

    // ViewModel untuk mengambil data mood
    private val moodEntryViewModel: MoodEntryViewModel by viewModels()
    private var currentMoodList = emptyList<MoodEntry>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inisialisasi ViewBinding
        _binding = FragmentBackUpBinding.inflate(inflater, container, false)

        // Observasi data mood
        moodEntryViewModel.readAllData.observe(viewLifecycleOwner) { moodList ->
            currentMoodList = moodList
        }

        // Tombol kembali
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Simpan ke PDF
        binding.btnSavePdf.setOnClickListener {
            handleExportAction {
                PdfUtils.saveMoodEntriesToPdf(requireContext(), currentMoodList)
            }
        }

        // Simpan ke Excel
        binding.btnSaveCsv.setOnClickListener {
            handleExportAction {
                ExcelUtils.saveMoodEntriesToExcel(requireContext(), currentMoodList)
            }
        }

        return binding.root
    }

    // Menangani proses export data
    private fun handleExportAction(action: suspend () -> Unit) {
        if (currentMoodList.isEmpty()) {
            showEmptyWarning()
            return
        }

        showLoading(true)
        lifecycleScope.launch {
            try {
                action()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "❌ Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    // Peringatan jika data kosong
    private fun showEmptyWarning() {
        Toast.makeText(
            requireContext(),
            "⚠️ Data mood kosong",
            Toast.LENGTH_SHORT
        ).show()
    }

    // Mengatur tampilan loading
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.tvWarning.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.btnSavePdf.isEnabled = !isLoading
        binding.btnSaveCsv.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
