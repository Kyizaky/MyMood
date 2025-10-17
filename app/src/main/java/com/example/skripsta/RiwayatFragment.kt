package com.example.skripsta

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skripsta.adapter.MoodHistoryAdapter
import com.example.skripsta.data.MoodEntry
import com.example.skripsta.data.MoodEntryViewModel
import com.example.skripsta.databinding.FragmentRiwayatBinding
import com.example.skripsta.utils.MoodUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class RiwayatFragment : Fragment() {

    private lateinit var binding: FragmentRiwayatBinding
    private lateinit var moodEntryViewModel: MoodEntryViewModel
    private lateinit var moodHistoryAdapter: MoodHistoryAdapter

    private val monthsList = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    private val yearsList = (2025..2030).map { it.toString() }

    private var selectedFullDate: LocalDate? = null
    private var lastCheckedMonth: String? = null

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

        selectedFullDate = LocalDate.now()
        binding.dateButton.text = MoodUtils.formatTanggal(selectedFullDate!!.toString())

        setupDatePickerButton()
        setupRecyclerView()

        moodEntryViewModel.readAllData.observe(viewLifecycleOwner) { entries ->
            updateRecyclerView(entries)
        }

        binding.backHistory.setOnClickListener {
            val action = RiwayatFragmentDirections.actionRiwayatFragmentToHomeFragment()
            findNavController().navigate(action)
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

    private fun setupDatePickerButton() {
        binding.dateButton.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun showDatePickerDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_picker3, null)
        dialog.setContentView(view)

        val datePicker = view.findViewById<NumberPicker>(R.id.date_picker)
        val monthPicker = view.findViewById<NumberPicker>(R.id.month_picker)
        val yearPicker = view.findViewById<NumberPicker>(R.id.year_picker)
        val btnCancel = view.findViewById<ImageButton>(R.id.btn_cancel)
        val btnConfirm = view.findViewById<Button>(R.id.btn_confirm)

        val initialDate = selectedFullDate ?: LocalDate.now()
        val initialDay = initialDate.dayOfMonth
        val initialMonthIndex = initialDate.monthValue - 1
        val initialYearIndex = yearsList.indexOf(initialDate.year.toString()).coerceAtLeast(0)

        yearPicker.minValue = 0
        yearPicker.maxValue = yearsList.size - 1
        yearPicker.displayedValues = yearsList.toTypedArray()
        yearPicker.value = initialYearIndex

        monthPicker.minValue = 0
        monthPicker.maxValue = monthsList.size - 1
        monthPicker.displayedValues = monthsList.toTypedArray()
        monthPicker.value = initialMonthIndex

        fun getMaxDay(month: Int, year: Int): Int {
            return when (month + 1) {
                1, 3, 5, 7, 8, 10, 12 -> 31
                4, 6, 9, 11 -> 30
                2 -> if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) 29 else 28
                else -> 30
            }
        }

        fun updateDatePicker() {
            val selectedYear = yearsList[yearPicker.value].toInt()
            val selectedMonth = monthPicker.value
            val maxDay = getMaxDay(selectedMonth, selectedYear)
            val dayList = (1..maxDay).map { it.toString() }.toTypedArray()

            datePicker.minValue = 0
            datePicker.maxValue = dayList.size - 1
            datePicker.displayedValues = dayList
            datePicker.value = (initialDay - 1).coerceAtMost(maxDay - 1)
        }

        updateDatePicker()
        monthPicker.setOnValueChangedListener { _, _, _ -> updateDatePicker() }
        yearPicker.setOnValueChangedListener { _, _, _ -> updateDatePicker() }

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            val day = datePicker.value + 1
            val month = monthPicker.value + 1
            val year = yearsList[yearPicker.value].toInt()
            selectedFullDate = LocalDate.of(year, month, day)

            binding.dateButton.text = MoodUtils.formatTanggal(selectedFullDate!!.toString())
            moodEntryViewModel.readAllData.value?.let {
                updateRecyclerView(it)
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateButtonsIfNeeded() {
        val now = LocalDate.now()
        val currentMonthYear = now.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH))
        if (currentMonthYear != lastCheckedMonth) {
            selectedFullDate = now
            binding.dateButton.text = MoodUtils.formatTanggal(selectedFullDate!!.toString())
            moodEntryViewModel.readAllData.value?.let {
                updateRecyclerView(it)
            }
            lastCheckedMonth = currentMonthYear
        }
    }

    private fun setupRecyclerView() {
        moodHistoryAdapter = MoodHistoryAdapter { moodEntry ->
            val action =
                RiwayatFragmentDirections.actionRiwayatFragmentToIsiRiwayatFragment(moodEntry)
            findNavController().navigate(action)
        }

        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = moodHistoryAdapter
        }
    }

    private fun updateRecyclerView(moodEntries: List<MoodEntry>) {
        val selectedDate = selectedFullDate ?: return

        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)

        val filteredEntries = moodEntries.filter { entry ->
            try {
                val entryDate = LocalDate.parse(entry.tanggal, inputFormatter)
                entryDate == selectedDate
            } catch (e: Exception) {
                false
            }
        }.sortedByDescending { it.jam }

        // Perbarui adapter
        moodHistoryAdapter.submitList(filteredEntries)

        // Atur visibilitas berdasarkan data
        if (filteredEntries.isEmpty()) {
            binding.historyRecyclerView.visibility = View.GONE
            binding.tvNoData.visibility = View.VISIBLE
        } else {
            binding.historyRecyclerView.visibility = View.VISIBLE
            binding.tvNoData.visibility = View.GONE
        }
    }

}
