package com.example.skripsta

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skripsta.adapter.HistorySectionAdapter
import com.example.skripsta.data.User
import com.example.skripsta.data.UserViewModel
import com.example.skripsta.databinding.FragmentRiwayatBinding
import com.example.skripsta.model.HistorySection
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class RiwayatFragment : Fragment() {

    private lateinit var binding: FragmentRiwayatBinding
    private lateinit var mUserViewModel: UserViewModel
    private lateinit var historyAdapter: HistorySectionAdapter
    private lateinit var monthsAdapter: ArrayAdapter<String>
    private lateinit var datesAdapter: ArrayAdapter<String>
    private val months = mutableListOf<String>()
    private val dates = mutableListOf<String>()
    private var lastCheckedMonth: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private val checkDateRunnable = object : Runnable {
        override fun run() {
            updateSpinnerIfNeeded()
            handler.postDelayed(this, 60_000) // Check every minute
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRiwayatBinding.inflate(inflater, container, false)
        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        // Set up the Spinners
        setupMonthYearSpinner()
        setupDateSpinner()

        // Set up the RecyclerView
        setupRecyclerView()

        // Observe the User data and update the RecyclerView
        mUserViewModel.readAllData.observe(viewLifecycleOwner) { users ->
            updateRecyclerView(users, getSelectedMonthYear(), getSelectedDate())
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

    private fun setupMonthYearSpinner() {
        // Initialize the months list
        updateMonthYearSpinnerList()

        // Set up the Spinner adapter
        monthsAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            months
        )
        monthsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.monthYearSpinner.adapter = monthsAdapter

        // Set the default selection to the current month
        val currentMonthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("in", "ID")))
        val defaultPosition = months.indexOf(currentMonthYear)
        binding.monthYearSpinner.setSelection(defaultPosition)

        // Handle Spinner selection changes
        binding.monthYearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedMonthYear = months[position]
                mUserViewModel.readAllData.value?.let { users ->
                    // Update the date spinner based on the new month/year
                    updateDateSpinner(users, selectedMonthYear)
                    // Update the RecyclerView with the new filters
                    updateRecyclerView(users, selectedMonthYear, getSelectedDate())
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupDateSpinner() {
        // Initialize the dates list with "All" as the default option
        dates.clear()
        dates.add("All")

        // Set up the Spinner adapter
        datesAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            dates
        )
        datesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.dateSpinner.adapter = datesAdapter

        // Set the default selection to "All"
        binding.dateSpinner.setSelection(0)

        // Handle Spinner selection changes
        binding.dateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                mUserViewModel.readAllData.value?.let { users ->
                    updateRecyclerView(users, getSelectedMonthYear(), getSelectedDate())
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun updateMonthYearSpinnerList() {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("in", "ID"))

        // Clear the existing list
        months.clear()

        // Add months: 6 months in the past and 6 months in the future (total 13 months)
        for (i in -6..6) {
            val date = currentDate.plusMonths(i.toLong())
            months.add(date.format(formatter))
        }

        // Sort months in descending order (most recent first)
        months.sortByDescending { date ->
            LocalDate.parse("1 $date", DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("in", "ID")))
        }

        // Update the last checked month
        lastCheckedMonth = currentDate.format(formatter)
    }

    private fun updateDateSpinner(users: List<User>, selectedMonthYear: String) {
        // Parse the selected month and year from the Spinner
        val tempDate = LocalDate.parse("1 $selectedMonthYear", DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("in", "ID")))
        val selectedMonth = tempDate.month
        val selectedYear = tempDate.year

        // Filter users for the selected month and year
        val filteredUsers = users.filter { user ->
            val userDate = LocalDate.parse(user.tanggal, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
            userDate.month == selectedMonth && userDate.year == selectedYear
        }

        // Get unique dates as LocalDate objects, sort them, then format them
        val displayFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale("in", "ID"))
        val uniqueDates = filteredUsers
            .map { user ->
                LocalDate.parse(user.tanggal, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
            }
            .distinct()
            .sortedByDescending { it }
            .map { it.format(displayFormatter) }

        // Update the dates list
        dates.clear()
        dates.add("All")
        dates.addAll(uniqueDates)

        // Notify the adapter of the change
        datesAdapter.notifyDataSetChanged()

        // Set the default selection to "All"
        binding.dateSpinner.setSelection(0)
    }

    private fun updateSpinnerIfNeeded() {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("in", "ID"))
        val currentMonthYear = currentDate.format(formatter)

        // If the month has changed, update the Spinner
        if (currentMonthYear != lastCheckedMonth) {
            updateMonthYearSpinnerList()
            monthsAdapter.notifyDataSetChanged()

            // Set the default selection to the current month
            val defaultPosition = months.indexOf(currentMonthYear)
            binding.monthYearSpinner.setSelection(defaultPosition)

            // Update the RecyclerView with the new current month
            mUserViewModel.readAllData.value?.let { users ->
                updateDateSpinner(users, currentMonthYear)
                updateRecyclerView(users, currentMonthYear, getSelectedDate())
            }
        }
    }

    private fun setupRecyclerView() {
        historyAdapter = HistorySectionAdapter(emptyList()) { user ->
            // Navigate to IsiRiwayatFragment when an entry is clicked
            val action = RiwayatFragmentDirections.actionRiwayatFragmentToIsiRiwayatFragment(user)
            findNavController().navigate(action)
        }
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
        }
    }

    private fun updateRecyclerView(users: List<User>, selectedMonthYear: String, selectedDate: String?) {
        // Parse the selected month and year from the Spinner
        val tempDate = LocalDate.parse("1 $selectedMonthYear", DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("in", "ID")))
        val selectedMonth = tempDate.month
        val selectedYear = tempDate.year

        // Filter users for the selected month and year
        var filteredUsers = users.filter { user ->
            val userDate = LocalDate.parse(user.tanggal, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
            userDate.month == selectedMonth && userDate.year == selectedYear
        }

        // Further filter by the selected date if it's not "All"
        if (selectedDate != null && selectedDate != "All") {
            filteredUsers = filteredUsers.filter { user ->
                val userDate = LocalDate.parse(user.tanggal, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                val formattedDate = userDate.format(DateTimeFormatter.ofPattern("d MMMM", Locale("in", "ID")))
                formattedDate == selectedDate
            }
        }

        // Group users by LocalDate and create sections
        val groupedUsers = filteredUsers.groupBy { user ->
            LocalDate.parse(user.tanggal, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
        }

        // Sort dates in descending order and create the HistorySection list
        val sections = mutableListOf<HistorySection>()
        val displayFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale("in", "ID"))
        groupedUsers.keys.sortedByDescending { it }.forEach { date ->
            val formattedDate = date.format(displayFormatter)
            val entriesForDate = groupedUsers[date]?.sortedByDescending { it.jam } ?: emptyList()
            sections.add(HistorySection(formattedDate, entriesForDate))
        }

        // Update the RecyclerView
        historyAdapter = HistorySectionAdapter(sections) { user ->
            val action = RiwayatFragmentDirections.actionRiwayatFragmentToIsiRiwayatFragment(user)
            findNavController().navigate(action)
        }
        binding.historyRecyclerView.adapter = historyAdapter
    }

    private fun getSelectedMonthYear(): String {
        return binding.monthYearSpinner.selectedItem?.toString() ?: LocalDate.now()
            .format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("in", "ID")))
    }

    private fun getSelectedDate(): String? {
        return binding.dateSpinner.selectedItem?.toString()
    }
}