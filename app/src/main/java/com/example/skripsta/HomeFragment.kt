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
import com.example.skripsta.data.MoodEntry
import com.example.skripsta.data.MoodEntryViewModel
import com.example.skripsta.data.UserViewModel
import com.example.skripsta.databinding.FragmentHomeBinding
import com.example.skripsta.utils.ClaimPrefsHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kizitonwose.calendar.core.*
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var moodEntryViewModel: MoodEntryViewModel
    private var moodEntries: List<MoodEntry> = emptyList()
    private val dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    private val moodDates = mutableSetOf<LocalDate>()
    private val monthsList = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    private val moodCache = mutableMapOf<LocalDate, Int?>()
    private val loginIcons = listOf(
        R.id.login1, R.id.login2, R.id.login3, R.id.login4,
        R.id.login5, R.id.login6, R.id.login7
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        moodEntryViewModel = ViewModelProvider(this)[MoodEntryViewModel::class.java]

        // Setup kalender
        val currentMonth = YearMonth.now()
        val currentYear = LocalDate.now().year
        val firstDayOfWeek = firstDayOfWeekFromLocale()
        val calendarView = binding.calendarView
        calendarView.setup(
            startMonth = YearMonth.of(currentYear, 1), // Start from January of current year
            endMonth = YearMonth.of(currentYear + 5, 12), // End at December 5 years later
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
                val entries = moodEntries.filter { it.tanggal == date.format(dateFormatter) }
                if (entries.isNotEmpty()) {
                    val moodCountMap = entries.groupingBy { it.mood }.eachCount()
                    val maxCount = moodCountMap.values.maxOrNull()
                    val mostFrequentMoods = moodCountMap.filterValues { it == maxCount }.keys
                    moodCache[date] = entries.lastOrNull { it.mood in mostFrequentMoods }?.mood
                } else {
                    moodCache[date] = null
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

                if (data.position == DayPosition.MonthDate) {
                    val date = data.date

                    // Ambil mood dari cache
                    val mood = moodCache[date]
                    if (mood != null) {
                        emojiIcon.visibility = View.VISIBLE
                        emojiIcon.setImageResource(getMoodEmojiDrawable(mood))
                        dayText.text = date.dayOfMonth.toString()
                    } else {
                        emojiIcon.visibility = View.VISIBLE
                        emojiIcon.setImageResource(R.drawable.circle_gray)
                        dayText.text = date.dayOfMonth.toString() // Tampilkan tanggal jika tidak ada emoji
                    }

                    // Listener untuk klik pada hari
                    container.view.setOnClickListener {
                        val selectedDate = date.format(dateFormatter)
                        val action = HomeFragmentDirections.actionHomeFragmentToRiwayatTanggalFragment(selectedDate)
                        findNavController().navigate(action)
                    }
                } else {
                    // Hari di luar bulan (misalnya, padding hari dari bulan sebelumnya/berikutnya)
                    dayText.text = ""
                    emojiIcon.visibility = View.GONE
                    container.view.setOnClickListener(null)
                }
            }
        }

        // Observe data dari database untuk memperbarui moodDates, cache, poin, dan streak
        val sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("current_user_id", 1)
        moodEntryViewModel.readAllData.observe(viewLifecycleOwner) { entries ->
            moodEntries = entries
            moodDates.clear()
            moodDates.addAll(entries.mapNotNull {
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
        }

        userViewModel.readAllData.observe(viewLifecycleOwner) { userList ->
            // Update points and streak display
            val user = userList.find { it.id == userId }
            // Cek apakah user login hari ini, kalau tidak reset streakCount
            userViewModel.resetStreakIfMissed(userId)

            binding.pointsText.text = user?.let { "Poin: ${it.points}" } ?: "Poin: 0"
            binding.streakText.text = user?.let { "${it.streakCount}" } ?: "0"
            updateWeeklyStatus()
        }

        // Listener untuk CardView Daily
        binding.cardviewDaily.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_dailyLoginFragment)
        }

        // Listener untuk tombol Riwayat
        binding.riwayatButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_riwayatFragment)
        }

        // Tambahan padding top untuk header agar tidak bentrok dengan status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.headerLayout) { view, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            val paddingTop = topInset + (15 * resources.displayMetrics.density).toInt() // 15dp + status bar
            view.setPadding(
                view.paddingLeft,
                paddingTop,
                view.paddingRight,
                view.paddingBottom
            )
            insets
        }

        return binding.root
    }

    private fun showMonthYearPickerDialog(currentMonth: YearMonth, onConfirm: (Int, Int) -> Unit) {
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_month_year_picker, null)
        dialog.setContentView(dialogView)

        // Setup year range: from current year to 5 years in the future
        val currentYear = LocalDate.now().year
        val yearsList = (currentYear..currentYear + 5).map { it.toString() }

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

    private fun updateWeeklyStatus() {
        ClaimPrefsHelper.cleanOldClaimsKeepThisWeek(requireContext()) // auto hapus tanggal minggu lalu

        loginIcons.forEach { iconId ->
            binding.root.findViewById<ImageView>(iconId)?.setImageResource(R.drawable.ic_nocheck)
        }

        val claimDates = ClaimPrefsHelper.getAllClaimDates(requireContext())
        val today = LocalDate.now()
        val weekStart = today.minusDays((today.dayOfWeek.value % 7).toLong())

        claimDates.forEach { date ->
            if (date >= weekStart) {
                val dayIndex = ChronoUnit.DAYS.between(weekStart, date).toInt()
                if (dayIndex in 0..6) {
                    binding.root.findViewById<ImageView>(loginIcons[dayIndex])
                        ?.setImageResource(R.drawable.ic_checkbox)
                }
            }
        }
    }

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
        val emojiIcon: ImageView = view.findViewById(R.id.emojiIcon)
        lateinit var day: CalendarDay
    }

    fun getMoodEmojiDrawable(mood: Int): Int {
        return when (mood) {
            1 -> R.drawable.para1
            2 -> R.drawable.para2
            3 -> R.drawable.para3
            4 -> R.drawable.para4
            5 -> R.drawable.para5
            else -> R.drawable.ic_medi
        }
    }
}