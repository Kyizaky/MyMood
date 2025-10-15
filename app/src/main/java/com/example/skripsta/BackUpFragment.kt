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
import com.example.skripsta.data.MoodEntryViewModel
import com.example.skripsta.databinding.FragmentBackUpBinding
import com.example.skripsta.utils.PdfUtils
import kotlinx.coroutines.launch

class BackUpFragment : Fragment() {

    private var _binding: FragmentBackUpBinding? = null
    private val binding get() = _binding!!

    private val moodEntryViewModel: MoodEntryViewModel by viewModels()
    private var currentMoodList = emptyList<com.example.skripsta.data.MoodEntry>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBackUpBinding.inflate(inflater, container, false)
        val view = binding.root

        moodEntryViewModel.readAllData.observe(viewLifecycleOwner) { moodList ->
            currentMoodList = moodList
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSavePdf.setOnClickListener {
            if (currentMoodList.isEmpty()) {
                showEmptyWarning()
                return@setOnClickListener
            }
            handleExportAction {
                PdfUtils.saveMoodEntriesToPdf(requireContext(), currentMoodList)
            }
        }

        binding.btnSaveCsv.setOnClickListener {
            if (currentMoodList.isEmpty()) {
                showEmptyWarning()
                return@setOnClickListener
            }
            handleExportAction {
                PdfUtils.saveMoodEntriesToPdf(requireContext(), currentMoodList)
            }
        }

        return view
    }

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
                e.printStackTrace()
                Toast.makeText(requireContext(), "❌ Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showEmptyWarning() {
        Toast.makeText(requireContext(), "⚠️ No mood data available to export!", Toast.LENGTH_SHORT).show()
    }

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
