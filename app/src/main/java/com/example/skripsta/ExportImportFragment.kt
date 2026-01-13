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
import com.example.skripsta.viewmodel.MoodEntryViewModel
import com.example.skripsta.viewmodel.UserViewModel
import com.example.skripsta.databinding.FragmentExportImportBinding
import com.example.skripsta.data.entity.MoodEntry
import com.example.skripsta.data.entity.User
import com.example.skripsta.utils.DatabaseExportUtils
import com.example.skripsta.utils.DatabaseImportUtils
import kotlinx.coroutines.launch

class ExportImportFragment : Fragment() {

    // Binding untuk layout fragment export-import
    private var _binding: FragmentExportImportBinding? = null
    private val binding get() = _binding!!

    // ViewModel untuk data mood dan user
    private val moodEntryViewModel: MoodEntryViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    // Menyimpan data mood dan user saat ini
    private var currentMoodList = emptyList<MoodEntry>()
    private var currentUserList = emptyList<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inisialisasi binding
        _binding = FragmentExportImportBinding.inflate(inflater, container, false)
        val view = binding.root

        // Observasi data mood dari database
        moodEntryViewModel.readAllData.observe(viewLifecycleOwner) { moodList ->
            currentMoodList = moodList
        }

        // Observasi data user dari database
        userViewModel.readAllData.observe(viewLifecycleOwner) { userList ->
            currentUserList = userList
        }

        // Tombol kembali ke halaman sebelumnya
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Tombol export data ke file CSV
        binding.btnExportCsv.setOnClickListener {
            if (currentMoodList.isEmpty() && currentUserList.isEmpty()) {
                // Menampilkan peringatan jika tidak ada data
                Toast.makeText(
                    requireContext(),
                    "Tidak ada data untuk diekspor",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Menjalankan proses export
                handleExportAction {
                    DatabaseExportUtils.exportDatabaseToCsv(
                        requireContext(),
                        currentMoodList,
                        currentUserList
                    )
                }
            }
        }

        // Tombol import file CSV
        binding.btnImportCsv.setOnClickListener {
            importFileLauncher.launch(
                arrayOf("text/csv", "text/plain", "*/*")
            )
        }

        return view
    }

    // Launcher untuk memilih file CSV dari storage
    private val importFileLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                lifecycleScope.launch {
                    // Import data dari file CSV
                    DatabaseImportUtils.importDatabaseFromCsv(
                        requireContext(),
                        it
                    ) { moodList, userList ->
                        // Mengganti data lama dengan data baru
                        moodEntryViewModel.replaceMoodEntry(moodList)
                        userViewModel.replaceUsers(userList)
                    }
                }
            }
        }

    // Fungsi pembungkus proses export dengan loading dan error handling
    private fun handleExportAction(action: suspend () -> Unit) {

        // Cek apakah data kosong
        if (currentMoodList.isEmpty() && currentUserList.isEmpty()) {
            showEmptyWarning()
            return
        }

        // Tampilkan loading
        showLoading(true)

        lifecycleScope.launch {
            try {
                // Jalankan proses export
                action()
            } catch (e: Exception) {
                // Menampilkan error jika gagal
                e.printStackTrace()
                Toast.makeText(
                    requireContext(),
                    "❌ Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                // Sembunyikan loading
                showLoading(false)
            }
        }
    }

    // Menampilkan peringatan jika tidak ada data
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

        binding.btnImportCsv.isEnabled = !isLoading
        binding.btnExportCsv.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Membersihkan binding untuk mencegah memory leak
        _binding = null
    }
}
