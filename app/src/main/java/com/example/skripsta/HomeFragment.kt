package com.example.skripsta

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.skripsta.data.User
import com.example.skripsta.data.UserViewModel
import com.example.skripsta.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kizitonwose.calendar.core.*
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var userViewModel: UserViewModel
    private var usersWithData: List<User> = emptyList()
    private val dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    private val moodDates = mutableSetOf<LocalDate>()
    private val monthsList = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )
    private val moodCache = mutableMapOf<LocalDate, Int?>() // Cache untuk mood per tanggal

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        // Setup kalender
        val currentMonth = YearMonth.now()
        val currentYear = LocalDate.now().year
        val firstDayOfWeek = firstDayOfWeekFromLocale()
        val calendarView = binding.calendarView
        calendarView.setup(
            startMonth = YearMonth.of(currentYear, 1), // Start from January of current year
            endMonth = YearMonth.of(currentYear + 10, 12), // End at December 10 years later
            firstDayOfWeek = firstDayOfWeek
        )
        calendarView.scrollToMonth(currentMonth)

        // Setup header kalender
        val monthYearText = binding.monthYearText
        val btnPrevious = binding.btnPreviousMonth
        val btnNext = binding.btnNextMonth

        fun updateMonthHeader(month: YearMonth) {
            val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
            monthYearText.text = month.format(formatter)
        }

        updateMonthHeader(currentMonth)

        // Listener untuk tombol Previous
        btnPrevious.setOnClickListener {
            val currentVisibleMonth = calendarView.findFirstVisibleMonth()?.yearMonth ?: currentMonth
            val previousMonth = currentVisibleMonth.minusMonths(1)
            calendarView.smoothScrollToMonth(previousMonth)
        }

        // Listener untuk tombol Next
        btnNext.setOnClickListener {
            val currentVisibleMonth = calendarView.findFirstVisibleMonth()?.yearMonth ?: currentMonth
            val nextMonth = currentVisibleMonth.plusMonths(1)
            calendarView.smoothScrollToMonth(nextMonth)
        }

        // Fungsi untuk memperbarui moodCache berdasarkan bulan
        fun updateMoodCacheForMonth(yearMonth: YearMonth) {
            moodCache.clear()
            val monthDates = yearMonth.atDay(1).datesUntil(yearMonth.plusMonths(1).atDay(1)).toList()
            monthDates.forEach { date ->
                val entries = usersWithData.filter { it.tanggal == date.format(dateFormatter) }
                if (entries.isNotEmpty()) {
                    val moodCountMap = entries.groupingBy { it.mood }.eachCount()
                    val maxCount = moodCountMap.values.maxOrNull()
                    val mostFrequentMoods = moodCountMap.filterValues { it == maxCount }.keys
                    moodCache[date] = entries.lastOrNull { it.mood in mostFrequentMoods }?.mood
                }
            }
        }

        // Listener untuk scroll kalender
        calendarView.monthScrollListener = { month ->
            updateMonthHeader(month.yearMonth)
            updateMoodCacheForMonth(month.yearMonth)
            binding.calendarView.notifyCalendarChanged()
        }

        // Tambahkan listener untuk TextView monthYearText
        monthYearText.setOnClickListener {
            val currentVisibleMonth = calendarView.findFirstVisibleMonth()?.yearMonth ?: currentMonth
            showMonthYearPickerDialog(currentVisibleMonth) { selectedMonth, selectedYear ->
                val selectedYearMonth = YearMonth.of(selectedYear, selectedMonth)
                calendarView.scrollToMonth(selectedYearMonth) // Directly jump to selected month
                updateMonthHeader(selectedYearMonth)
                // Perbarui cache untuk bulan yang dipilih
                updateMoodCacheForMonth(selectedYearMonth)
                // Pastikan data diperbarui untuk bulan yang dipilih
                binding.calendarView.notifyMonthChanged(selectedYearMonth)
            }
        }

        // Setup dayBinder untuk kalender
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data

                val dayText = container.textView
                val emojiIcon = container.emojiIcon
                val todayBg = container.todayBackground

                if (data.position == DayPosition.MonthDate) {
                    val date = data.date

                    // Lingkaran untuk hari ini
                    todayBg.visibility = if (date == LocalDate.now()) View.VISIBLE else View.GONE

                    // Ambil mood dari cache
                    val mood = moodCache[date]
                    if (mood != null) {
                        emojiIcon.visibility = View.VISIBLE
                        emojiIcon.setImageResource(getMoodEmojiDrawable(mood))
                        dayText.text = ""
                    } else {
                        emojiIcon.visibility = View.GONE
                        dayText.text = date.dayOfMonth.toString()
                    }

                    dayText.setOnClickListener {
                        val selectedDate = date.format(dateFormatter)
                        val action = HomeFragmentDirections.actionHomeFragmentToRiwayatTanggalFragment(selectedDate)
                        findNavController().navigate(action)
                    }
                } else {
                    dayText.text = ""
                    emojiIcon.visibility = View.GONE
                    todayBg.visibility = View.GONE
                    dayText.setOnClickListener(null)
                }
            }
        }

        // Observe data dari database untuk memperbarui moodDates dan cache
        userViewModel.readAllData.observe(viewLifecycleOwner) { userList ->
            usersWithData = userList
            moodDates.clear()
            moodDates.addAll(userList.mapNotNull {
                try {
                    LocalDate.parse(it.tanggal, dateFormatter)
                } catch (e: Exception) {
                    null
                }
            })
            // Perbarui cache untuk bulan saat ini
            val currentVisibleMonth = calendarView.findFirstVisibleMonth()?.yearMonth ?: currentMonth
            updateMoodCacheForMonth(currentVisibleMonth)
            binding.calendarView.notifyCalendarChanged()

            // Update points display
            val sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getInt("current_user_id", 0)
            val user = userList.find { it.id == userId }
            binding.pointsText.text = user?.let { "Poin: ${it.points}" } ?: "Poin: 0"
        }

        // Listener untuk tombol Riwayat
        binding.riwayatButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_riwayatFragment)
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            binding.headerLayout.setPadding(
                binding.headerLayout.paddingLeft,
                statusBarHeight,
                binding.headerLayout.paddingRight,
                binding.headerLayout.paddingBottom
            )
            insets
        }

        return binding.root
    }

    private fun showMonthYearPickerDialog(currentMonth: YearMonth, onConfirm: (Int, Int) -> Unit) {
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_month_year_picker, null)
        dialog.setContentView(dialogView)

        // Setup year range: from current year (app download year) to 10 years in the future
        val currentYear = LocalDate.now().year
        val yearsList = (currentYear..currentYear + 10).map { it.toString() }

        // Setup NumberPicker untuk bulan
        val monthPicker = dialogView.findViewById<NumberPicker>(R.id.monthPicker)
        monthPicker.minValue = 0
        monthPicker.maxValue = monthsList.size - 1
        monthPicker.displayedValues = monthsList.toTypedArray()
        // Set default to current displayed month
        monthPicker.value = currentMonth.monthValue - 1
        monthPicker.wrapSelectorWheel = false // Nonaktifkan looping untuk bulan

        // Setup NumberPicker untuk tahun
        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.yearPicker)
        yearPicker.minValue = 0
        yearPicker.maxValue = yearsList.size - 1
        yearPicker.displayedValues = yearsList.toTypedArray()
        // Set default to current displayed year
        yearPicker.value = yearsList.indexOf(currentMonth.year.toString())
        yearPicker.wrapSelectorWheel = false // Nonaktifkan looping untuk tahun

        // Listener untuk tombol Setuju
        dialogView.findViewById<Button>(R.id.confirmButton).setOnClickListener {
            val selectedMonthIndex = monthPicker.value + 1 // 1-12
            val selectedYear = yearsList[yearPicker.value].toInt()
            onConfirm(selectedMonthIndex, selectedYear)
            dialog.dismiss()
        }

        dialog.show()
    }

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
        val emojiIcon: ImageView = view.findViewById(R.id.emojiIcon)
        val todayBackground: View = view.findViewById(R.id.todayBackground)
        lateinit var day: CalendarDay
    }

    fun getMoodEmojiDrawable(mood: Int): Int {
        return when (mood) {
            1 -> R.drawable.mood1
            2 -> R.drawable.mood2
            3 -> R.drawable.mood3
            4 -> R.drawable.mood4
            5 -> R.drawable.mood5
            6 -> R.drawable.mood6
            else -> R.drawable.ic_medi
        }
    }
}