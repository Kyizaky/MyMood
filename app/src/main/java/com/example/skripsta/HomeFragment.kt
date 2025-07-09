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
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.skripsta.data.User
import com.example.skripsta.data.UserViewModel
import com.example.skripsta.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kizitonwose.calendar.core.*
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var userViewModel: UserViewModel
    private var usersWithData: List<User> = emptyList()
    private val dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    private val dbDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val moodDates = mutableSetOf<LocalDate>()
    private val monthsList = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )
    private val moodCache = mutableMapOf<LocalDate, Int?>() // Cache untuk mood per tanggal
    private val dayIcons = listOf(
        R.id.ic_back, R.id.ic_back, R.id.ic_back, R.id.ic_back,
        R.id.ic_back, R.id.ic_back, R.id.ic_back
    )

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
            showMonthYearPickerDialog(currentMonth) { selectedMonth, selectedYear ->
                val selectedYearMonth = YearMonth.of(selectedYear, selectedMonth)
                calendarView.scrollToMonth(selectedYearMonth)
                updateMonthHeader(selectedYearMonth)
                updateMoodCacheForMonth(selectedYearMonth)
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

        // Observe data dari database untuk memperbarui moodDates, cache, poin, dan streak
        val sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("current_user_id", 1)
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

            // Update points and streak display
            val user = userList.find { it.id == userId }
            binding.pointsText.text = user?.let { "Poin: ${it.points}" } ?: "Poin: 0"
            binding.streakText.text = user?.let { "${it.streakCount}" } ?: "0"
            // Update weekly status
            updateWeeklyStatus(user?.lastClaimDate)
        }

        binding.btnClaim.setOnClickListener {
            lifecycleScope.launch {
                userViewModel.claimStreakPoints(userId, 10)
                binding.btnClaim.isEnabled = false
                val user = userViewModel.getUserById(userId)
                Toast.makeText(
                    requireContext(),
                    "Claimed ${user?.streakCount?.times(10)} points! Streak: ${user?.streakCount}",
                    Toast.LENGTH_SHORT
                ).show()
                // Update points, streak, and weekly status
                binding.pointsText.text = user?.let { "Poin: ${it.points}" } ?: "Poin: 0"
                binding.streakText.text = user?.let { "${it.streakCount}" } ?: "0"
                updateWeeklyStatus(user?.lastClaimDate)
            }
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

    override fun onStart() {
        super.onStart()
        // Check claim eligibility every time the fragment becomes visible
        val sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("current_user_id", 1)
        lifecycleScope.launch {
            binding.btnClaim.isEnabled = userViewModel.canClaimStreakPoints(userId)
        }
    }

    private fun updateWeeklyStatus(lastClaimDate: String?) {
        // Reset semua ikon ke status tidak diklaim
        dayIcons.forEach { iconId ->
            binding.root.findViewById<ImageView>(iconId)?.setImageResource(R.drawable.ic_back)
        }

        // Jika tidak ada lastClaimDate, biarkan semua ikon tetap ic_back
        if (lastClaimDate == null) return

        try {
            val lastClaim = LocalDate.parse(lastClaimDate, dbDateFormatter)
            val today = LocalDate.now()
            val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1) // Senin sebagai hari pertama
            val weekEnd = weekStart.plusDays(6) // Minggu

            // Periksa tanggal claim dalam minggu ini
            val claimDates = mutableListOf<LocalDate>()
            var currentDate = lastClaim
            while (currentDate >= weekStart && currentDate <= weekEnd) {
                claimDates.add(currentDate)
                currentDate = currentDate.minusDays(1)
            }

            // Update ikon untuk hari yang diklaim
            claimDates.forEach { date ->
                val dayIndex = ChronoUnit.DAYS.between(weekStart, date).toInt()
                if (dayIndex in 0..6) {
                    binding.root.findViewById<ImageView>(dayIcons[dayIndex])?.setImageResource(R.drawable.ic_back)
                }
            }
        } catch (e: Exception) {
            // Tangani error parsing tanggal
            Toast.makeText(requireContext(), "Error parsing claim date", Toast.LENGTH_SHORT).show()
        }
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
        monthPicker.value = currentMonth.monthValue - 1
        monthPicker.wrapSelectorWheel = false

        // Setup NumberPicker untuk tahun
        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.yearPicker)
        yearPicker.minValue = 0
        yearPicker.maxValue = yearsList.size - 1
        yearPicker.displayedValues = yearsList.toTypedArray()
        yearPicker.value = yearsList.indexOf(currentMonth.year.toString())
        yearPicker.wrapSelectorWheel = false

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