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
import com.example.skripsta.data.User
import com.example.skripsta.data.UserViewModel
import com.example.skripsta.databinding.FragmentRiwayatBinding
import com.example.skripsta.model.HistorySection
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class RiwayatFragment : Fragment() {

    private lateinit var binding: FragmentRiwayatBinding
    private lateinit var mUserViewModel: UserViewModel
    private lateinit var historyAdapter: HistorySectionAdapter
    private val monthsList = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )
    private val yearsList = (2020..2030).map { it.toString() } // Rentang tahun
    private val datesList = mutableListOf<String>()
    private var lastCheckedMonth: String? = null
    private var selectedYear: String? = null
    private var selectedMonth: String? = null
    private var selectedDate: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private val checkDateRunnable = object : Runnable {
        override fun run() {
            updateButtonsIfNeeded()
            handler.postDelayed(this, 60_000) // Check every minute
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRiwayatBinding.inflate(inflater, container, false)
        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        // Inisialisasi tahun, bulan, dan tanggal default
        val currentDate = LocalDate.now()
        selectedYear = currentDate.year.toString()
        selectedMonth = monthsList[currentDate.monthValue - 1]
        selectedDate = "All"
        binding.yearButton.text = selectedYear
        binding.monthButton.text = selectedMonth
        binding.dateButton.text = selectedDate

        // Set up the Buttons
        setupButtons()

        // Set up the RecyclerView
        setupRecyclerView()

        // Observe the User data and update the RecyclerView
        mUserViewModel.readAllData.observe(viewLifecycleOwner) { users ->
            updateDateList(users)
            updateRecyclerView(users)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // Start checking for date changes
        handler.post(checkDateRunnable)
    }

    override fun onPause() {
        super.onPause()
        // Stop checking for date changes
        handler.removeCallbacks(checkDateRunnable)
    }

    private fun setupButtons() {
        // Button untuk memilih tahun
        binding.yearButton.setOnClickListener {
            showPickerDialog("Pilih Tahun", yearsList) { selected ->
                selectedYear = selected
                binding.yearButton.text = selected
                mUserViewModel.readAllData.value?.let { users ->
                    updateDateList(users)
                    selectedDate = "All" // Reset tanggal ke "All"
                    binding.dateButton.text = selectedDate
                    updateRecyclerView(users)
                }
            }
        }

        // Button untuk memilih bulan
        binding.monthButton.setOnClickListener {
            showPickerDialog("Pilih Bulan", monthsList) { selected ->
                selectedMonth = selected
                binding.monthButton.text = selected
                mUserViewModel.readAllData.value?.let { users ->
                    updateDateList(users)
                    selectedDate = "All" // Reset tanggal ke "All"
                    binding.dateButton.text = selectedDate
                    updateRecyclerView(users)
                }
            }
        }

        // Button untuk memilih tanggal
        binding.dateButton.setOnClickListener {
            showPickerDialog("Pilih Tanggal", datesList) { selected ->
                selectedDate = selected
                binding.dateButton.text = selected
                mUserViewModel.readAllData.value?.let { users ->
                    updateRecyclerView(users)
                }
            }
        }
    }

    private fun showPickerDialog(title: String, options: List<String>, onItemSelected: (String) -> Unit) {
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_number_picker, null)
        dialog.setContentView(dialogView)

        // Set judul
        dialogView.findViewById<TextView>(R.id.dialogTitle).text = title

        // Set NumberPicker
        val numberPicker = dialogView.findViewById<NumberPicker>(R.id.numberPicker)
        numberPicker.minValue = 0
        numberPicker.maxValue = options.size - 1
        numberPicker.displayedValues = options.toTypedArray()
        numberPicker.wrapSelectorWheel = true

        // Set default selection
        val defaultIndex = when (title) {
            "Pilih Tahun" -> yearsList.indexOf(selectedYear ?: yearsList.first())
            "Pilih Bulan" -> monthsList.indexOf(selectedMonth ?: monthsList.first())
            "Pilih Tanggal" -> datesList.indexOf(selectedDate ?: "All")
            else -> 0
        }
        numberPicker.value = defaultIndex

        // Set listener untuk tombol konfirmasi
        dialogView.findViewById<Button>(R.id.confirmButton).setOnClickListener {
            val selectedValue = options[numberPicker.value]
            onItemSelected(selectedValue)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateDateList(users: List<User>) {
        // Pastikan tahun dan bulan sudah dipilih
        if (selectedYear == null || selectedMonth == null) return

        // Parse tahun dan bulan yang dipilih
        val selectedYearInt = selectedYear!!.toInt()
        val selectedMonthIndex = monthsList.indexOf(selectedMonth) + 1 // 1-12
        val tempDate = LocalDate.of(selectedYearInt, selectedMonthIndex, 1)

        // Filter users untuk tahun dan bulan yang dipilih
        val filteredUsers = users.filter { user ->
            val userDate = LocalDate.parse(user.tanggal, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
            userDate.monthValue == selectedMonthIndex && userDate.year == selectedYearInt
        }

        // Get unique dates, sort them, then format them
        val displayFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale("in", "ID"))
        val uniqueDates = filteredUsers
            .map { user ->
                LocalDate.parse(user.tanggal, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
            }
            .distinct()
            .sortedByDescending { it }
            .map { it.format(displayFormatter) }

        // Update the dates list
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
            mUserViewModel.readAllData.value?.let { users ->
                updateDateList(users)
                updateRecyclerView(users)
            }

            lastCheckedMonth = currentMonthYear
        }
    }

    private fun setupRecyclerView() {
        historyAdapter = HistorySectionAdapter(emptyList()) { user ->
            // Navigate ke IsiRiwayatFragment saat item diklik
            val action = RiwayatFragmentDirections.actionRiwayatFragmentToIsiRiwayatFragment(user)
            findNavController().navigate(action)
        }
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
        }
    }

    private fun updateRecyclerView(users: List<User>) {
        // Pastikan tahun dan bulan sudah dipilih
        if (selectedYear == null || selectedMonth == null) return

        // Parse tahun dan bulan yang dipilih
        val selectedYearInt = selectedYear!!.toInt()
        val selectedMonthIndex = monthsList.indexOf(selectedMonth) + 1 // 1-12

        // Filter users untuk tahun dan bulan yang dipilih
        var filteredUsers = users.filter { user ->
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