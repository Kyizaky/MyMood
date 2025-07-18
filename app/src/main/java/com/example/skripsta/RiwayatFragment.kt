package com.example.skripsta

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skripsta.adapter.HistorySectionAdapter
import com.example.skripsta.data.MoodEntry
import com.example.skripsta.data.MoodEntryViewModel
import com.example.skripsta.databinding.FragmentRiwayatBinding
import com.example.skripsta.model.HistorySection
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class RiwayatFragment : Fragment() {

    private lateinit var binding: FragmentRiwayatBinding
    private lateinit var moodEntryViewModel: MoodEntryViewModel
    private lateinit var historyAdapter: HistorySectionAdapter
    private val monthsList = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )
    private val yearsList = (2025..2030).map { it.toString() }
    private val datesList = mutableListOf<String>()
    private var lastCheckedMonth: String? = null
    private var selectedYear: String? = null
    private var selectedMonth: String? = null
    private var selectedDate: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private val checkDateRunnable = object : Runnable {
        override fun run() {
            updateButtonsIfNeeded()
            handler.postDelayed(this, 60_000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRiwayatBinding.inflate(inflater, container, false)
        moodEntryViewModel = ViewModelProvider(this).get(MoodEntryViewModel::class.java)

        val currentDate = LocalDate.now()
        selectedYear = currentDate.year.toString()
        selectedMonth = monthsList[currentDate.monthValue - 1]
        selectedDate = "All"
        binding.yearButton.text = selectedYear
        binding.monthButton.text = selectedMonth
        binding.dateButton.text = selectedDate

        setupButtons()
        setupRecyclerView()

        moodEntryViewModel.readAllData.observe(viewLifecycleOwner) { entries ->
            updateDateList(entries)
            updateRecyclerView(entries)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        handler.post(checkDateRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(checkDateRunnable)
    }

    private fun setupButtons() {
        // Button untuk memilih tahun
        binding.yearButton.setOnClickListener {
            showPickerDialog("Pilih Tahun", yearsList) { selected ->
                selectedYear = selected
                binding.yearButton.text = selected
                moodEntryViewModel.readAllData.value?.let { entries ->
                    updateDateList(entries)
                    selectedDate = "All" // Reset tanggal ke "All"
                    binding.dateButton.text = selectedDate
                    updateRecyclerView(entries)
                }
            }
        }

        binding.backHistory.setOnClickListener {
            findNavController().navigate(R.id.action_riwayatFragment_to_homeFragment)
        }

        // Button untuk memilih bulan
        binding.monthButton.setOnClickListener {
            showPickerDialog("Pilih Bulan", monthsList) { selected ->
                selectedMonth = selected
                binding.monthButton.text = selected
                moodEntryViewModel.readAllData.value?.let { entries ->
                    updateDateList(entries)
                    selectedDate = "All" // Reset tanggal ke "All"
                    binding.dateButton.text = selectedDate
                    updateRecyclerView(entries)
                }
            }
        }

        // Button untuk memilih tanggal
        binding.dateButton.setOnClickListener {
            showPickerDialog("Pilih Tanggal", datesList) { selected ->
                selectedDate = selected
                binding.dateButton.text = selected
                moodEntryViewModel.readAllData.value?.let { entries ->
                    updateRecyclerView(entries)
                }
            }
        }
    }

    private fun showPickerDialog(title: String, options: List<String>, onItemSelected: (String) -> Unit) {
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_number_picker, null)
        dialog.setContentView(dialogView)

        dialogView.findViewById<TextView>(R.id.dialogTitle).text = title
        val numberPicker = dialogView.findViewById<NumberPicker>(R.id.numberPicker)
        numberPicker.minValue = 0
        numberPicker.maxValue = options.size - 1
        numberPicker.displayedValues = options.toTypedArray()
        numberPicker.wrapSelectorWheel = true

        val defaultIndex = when (title) {
            "Pilih Tahun" -> yearsList.indexOf(selectedYear ?: yearsList.first())
            "Pilih Bulan" -> monthsList.indexOf(selectedMonth ?: monthsList.first())
            "Pilih Tanggal" -> datesList.indexOf(selectedDate ?: "All")
            else -> 0
        }
        numberPicker.value = defaultIndex

        dialogView.findViewById<Button>(R.id.confirmButton).setOnClickListener {
            val selectedValue = options[numberPicker.value]
            onItemSelected(selectedValue)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateDateList(entries: List<MoodEntry>) {
        if (selectedYear == null || selectedMonth == null) return

        val selectedYearInt = selectedYear!!.toInt()
        val selectedMonthIndex = monthsList.indexOf(selectedMonth) + 1

        val filteredEntries = entries.filter { entry ->
            val entryDate = LocalDate.parse(entry.tanggal, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
            entryDate.monthValue == selectedMonthIndex && entryDate.year == selectedYearInt
        }

        val displayFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale("in", "ID"))
        val uniqueDates = filteredEntries
            .map { LocalDate.parse(it.tanggal, DateTimeFormatter.ofPattern("MM/dd/yyyy")) }
            .distinct()
            .sortedByDescending { it }
            .map { it.format(displayFormatter) }

        datesList.clear()
        datesList.add("All")
        datesList.addAll(uniqueDates)
    }

    private fun updateButtonsIfNeeded() {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("in", "ID"))
        val currentMonthYear = currentDate.format(formatter)

        // Jika bulan berubah, update tombol dan data
        if (currentMonthYear != lastCheckedMonth) {
            selectedYear = currentDate.year.toString()
            selectedMonth = monthsList[currentDate.monthValue - 1]
            selectedDate = "All"
            binding.yearButton.text = selectedYear
            binding.monthButton.text = selectedMonth
            binding.dateButton.text = selectedDate

            // Update RecyclerView dengan bulan saat ini
            moodEntryViewModel.readAllData.value?.let { users ->
                updateDateList(users)
                updateRecyclerView(users)
            }

            lastCheckedMonth = currentMonthYear
        }
    }

    private fun setupRecyclerView() {
        historyAdapter = HistorySectionAdapter(
            emptyList(),
            onItemClick = { moodEntry ->
                val action = RiwayatFragmentDirections.actionRiwayatFragmentToIsiRiwayatFragment(moodEntry)
                findNavController().navigate(action)
            }
        )
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
        }
    }

    private fun updateRecyclerView(moodEntry: List<MoodEntry>) {
        // Pastikan tahun dan bulan sudah dipilih
        if (selectedYear == null || selectedMonth == null) return

        // Parse tahun dan bulan yang dipilih
        val selectedYearInt = selectedYear!!.toInt()
        val selectedMonthIndex = monthsList.indexOf(selectedMonth) + 1 // 1-12

        // Filter users untuk tahun dan bulan yang dipilih
        var filteredUsers = moodEntry.filter { user ->
            val userDate = LocalDate.parse(user.tanggal, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
            userDate.monthValue == selectedMonthIndex && userDate.year == selectedYearInt
        }

        // Filter lebih lanjut berdasarkan tanggal jika bukan "All"
        if (selectedDate != null && selectedDate != "All") {
            filteredUsers = filteredUsers.filter { user ->
                val userDate = LocalDate.parse(user.tanggal, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                val formattedDate = userDate.format(DateTimeFormatter.ofPattern("d MMMM", Locale("in", "ID")))
                formattedDate == selectedDate
            }
        }

        // Kelompokkan users berdasarkan LocalDate dan buat sections
        val groupedUsers = filteredUsers.groupBy { user ->
            LocalDate.parse(user.tanggal, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
        }

        // Urutkan tanggal secara descending dan buat daftar HistorySection
        val sections = mutableListOf<HistorySection>()
        val displayFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale("in", "ID"))
        groupedUsers.keys.sortedByDescending { it }.forEach { date ->
            val formattedDate = date.format(displayFormatter)
            val entriesForDate = groupedUsers[date]?.sortedByDescending { it.jam } ?: emptyList()
            sections.add(HistorySection(formattedDate, entriesForDate))
        }

        // Update RecyclerView
        historyAdapter = HistorySectionAdapter(sections) { user ->
            val action = RiwayatFragmentDirections.actionRiwayatFragmentToIsiRiwayatFragment(user)
            findNavController().navigate(action)
        }
        binding.historyRecyclerView.adapter = historyAdapter
    }
}