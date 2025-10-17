package com.example.skripsta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.skripsta.data.MoodEntryViewModel
import com.example.skripsta.data.UserViewModel
import com.example.skripsta.databinding.FragmentExportImportBinding
import com.example.skripsta.utils.DatabaseExportUtils
import com.example.skripsta.utils.DatabaseImportUtils
import kotlinx.coroutines.launch

class ExportImportFragment : Fragment() {

    private var _binding: FragmentExportImportBinding? = null
    private val binding get() = _binding!!

    private val moodEntryViewModel: MoodEntryViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private var currentMoodList = emptyList<com.example.skripsta.data.MoodEntry>()
    private var currentUserList = emptyList<com.example.skripsta.data.User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExportImportBinding.inflate(inflater, container, false)
        val view = binding.root

        moodEntryViewModel.readAllData.observe(viewLifecycleOwner) { moodList ->
            currentMoodList = moodList
        }

        userViewModel.readAllData.observe(viewLifecycleOwner){ userList ->
            currentUserList = userList
        }


        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnExportCsv.setOnClickListener {
                    if (currentMoodList.isEmpty() && currentUserList.isEmpty()) {
                        Toast.makeText(requireContext(), "Tidak ada data untuk diekspor", Toast.LENGTH_SHORT).show()
                    } else {
                        handleExportAction {
                            DatabaseExportUtils.exportDatabaseToCsv(requireContext(), currentMoodList, currentUserList)
                        }
                    }
        }

        binding.btnImportCsv.setOnClickListener {
            importFileLauncher.launch(arrayOf("text/csv", "text/plain", "*/*"))
            }

        return view
    }

    private val importFileLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                lifecycleScope.launch {
                    DatabaseImportUtils.importDatabaseFromCsv(requireContext(), it) { moodList, userList ->
                        // Ganti data lama dengan data baru
                        moodEntryViewModel.replaceMoodEntry(moodList)
                        userViewModel.replaceUsers(userList)
                    }
                }
            }
        }

    private fun handleExportAction(action: suspend () -> Unit) {
        if (currentMoodList.isEmpty() && currentUserList.isEmpty()) {
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
        binding.btnImportCsv.isEnabled = !isLoading
        binding.btnExportCsv.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
