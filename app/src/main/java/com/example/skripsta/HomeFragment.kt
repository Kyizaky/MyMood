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
import com.example.skripsta.data.entity.MoodEntry
import com.example.skripsta.viewmodel.MoodEntryViewModel
import com.example.skripsta.viewmodel.UserViewModel
import com.example.skripsta.databinding.FragmentHomeBinding
import com.example.skripsta.utils.ClaimPrefsHelper
import com.example.skripsta.utils.MoodUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kizitonwose.calendar.core.*
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class HomeFragment : Fragment() {

    // ViewBinding untuk fragment home
    private lateinit var binding: FragmentHomeBinding

    // ViewModel untuk user dan mood
    private lateinit var userViewModel: UserViewModel
    private lateinit var moodEntryViewModel: MoodEntryViewModel

    // List data mood
    private var moodEntries: List<MoodEntry> = emptyList()

    // Formatter tanggal database
    private val dbFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)

    // Set tanggal yang memiliki mood
    private val moodDates = mutableSetOf<LocalDate>()

    // Daftar nama bulan
    private val monthsList = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    // Cache mood per tanggal untuk performa kalender
    private val moodCache = mutableMapOf<LocalDate, Int?>()

    // ID icon checklist login mingguan
    private val loginIcons = listOf(
        R.id.login1, R.id.login2, R.id.login3, R.id.login4,
        R.id.login5, R.id.login6, R.id.login7
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate layout menggunakan ViewBinding
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Inisialisasi ViewModel
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        moodEntryViewModel = ViewModelProvider(this)[MoodEntryViewModel::class.java]

        // Setup kalender
        val currentMonth = YearMonth.now()
        val currentYear = LocalDate.now().year
        val firstDayOfWeek = firstDayOfWeekFromLocale()
        val calendarView = binding.calendarView

        // Konfigurasi rentang kalender
        calendarView.setup(
            startMonth = YearMonth.of(currentYear, 1),
            endMonth = YearMonth.of(currentYear + 5, 12),
            firstDayOfWeek = firstDayOfWeek
        )

        // Scroll ke bulan saat ini
        calendarView.scrollToMonth(currentMonth)

        // Header bulan dan tombol navigasi
        val monthYearText = binding.monthYearText
        val btnPrevious = binding.btnPreviousMonth
        val btnNext = binding.btnNextMonth

        // Update teks header bulan
        fun updateMonthHeader(month: YearMonth) {
            val formatter =
                DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
            monthYearText.text = month.format(formatter)
        }

        updateMonthHeader(currentMonth)

        // Navigasi bulan sebelumnya
        btnPrevious.setOnClickListener {
            val currentVisibleMonth =
                calendarView.findFirstVisibleMonth()?.yearMonth ?: currentMonth
            calendarView.smoothScrollToMonth(currentVisibleMonth.minusMonths(1))
        }

        // Navigasi bulan berikutnya
        btnNext.setOnClickListener {
            val currentVisibleMonth =
                calendarView.findFirstVisibleMonth()?.yearMonth ?: currentMonth
            calendarView.smoothScrollToMonth(currentVisibleMonth.plusMonths(1))
        }

        // Update cache mood untuk bulan tertentu
        fun updateMoodCacheForMonth(yearMonth: YearMonth) {
            moodCache.clear()
            val monthDates =
                yearMonth.atDay(1)
                    .datesUntil(yearMonth.plusMonths(1).atDay(1))
                    .toList()

            monthDates.forEach { date ->
                val entries =
                    moodEntries.filter {
                        it.tanggal == date.format(dbFormatter)
                    }

                if (entries.isNotEmpty()) {
                    val moodCountMap =
                        entries.groupingBy { it.mood }.eachCount()
                    val maxCount = moodCountMap.values.maxOrNull()
                    val mostFrequentMoods =
                        moodCountMap.filterValues { it == maxCount }.keys
                    moodCache[date] =
                        entries.lastOrNull { it.mood in mostFrequentMoods }?.mood
                } else {
                    moodCache[date] = null
                }
            }
        }

        // Listener scroll bulan kalender
        calendarView.monthScrollListener = { month ->
            updateMonthHeader(month.yearMonth)
            updateMoodCacheForMonth(month.yearMonth)
            binding.calendarView.notifyCalendarChanged()
        }

        // Klik header untuk memilih bulan & tahun
        monthYearText.setOnClickListener {
            showMonthYearPickerDialog(currentMonth) { selectedMonth, selectedYear ->
                val selectedYearMonth =
                    YearMonth.of(selectedYear, selectedMonth)
                calendarView.scrollToMonth(selectedYearMonth)
                updateMonthHeader(selectedYearMonth)
                updateMoodCacheForMonth(selectedYearMonth)
                binding.calendarView.notifyMonthChanged(selectedYearMonth)
            }
        }

        // Binder tampilan setiap hari di kalender
        calendarView.dayBinder =
            object : MonthDayBinder<DayViewContainer> {

                override fun create(view: View) =
                    DayViewContainer(view)

                override fun bind(container: DayViewContainer, data: CalendarDay) {
                    val dayText = container.textView
                    val emojiIcon = container.emojiIcon

                    if (data.position == DayPosition.MonthDate) {
                        val date = data.date
                        val mood = moodCache[date]

                        // Tampilkan icon mood atau placeholder
                        if (mood != null) {
                            emojiIcon.visibility = View.VISIBLE
                            emojiIcon.setImageResource(
                                MoodUtils.getMoodIcon(mood)
                            )
                        } else {
                            emojiIcon.visibility = View.VISIBLE
                            emojiIcon.setImageResource(R.drawable.circle_gray)
                        }

                        dayText.text = date.dayOfMonth.toString()

                        // Navigasi ke riwayat tanggal
                        container.view.setOnClickListener {
                            val selectedDate =
                                date.format(dbFormatter)
                            val action =
                                HomeFragmentDirections
                                    .actionHomeFragmentToRiwayatTanggalFragment(
                                        selectedDate
                                    )
                            findNavController().navigate(action)
                        }
                    } else {
                        dayText.text = ""
                        emojiIcon.visibility = View.GONE
                        container.view.setOnClickListener(null)
                    }
                }
            }

        // Ambil user aktif
        val sharedPreferences =
            requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val userId =
            sharedPreferences.getInt("current_user_id", 1)

        // Observasi data mood
        moodEntryViewModel.readAllData.observe(viewLifecycleOwner) { entries ->
            moodEntries = entries
            moodDates.clear()

            moodDates.addAll(entries.mapNotNull {
                try {
                    LocalDate.parse(it.tanggal, dbFormatter)
                } catch (e: Exception) {
                    null
                }
            })

            val currentVisibleMonth =
                calendarView.findFirstVisibleMonth()?.yearMonth ?: currentMonth
            updateMoodCacheForMonth(currentVisibleMonth)
            binding.calendarView.notifyCalendarChanged()
        }

        // Observasi data user
        userViewModel.readAllData.observe(viewLifecycleOwner) { userList ->
            val user = userList.find { it.id == userId }
            binding.pointsText.text =
                user?.let { "Total Day: ${it.points}" } ?: "Total: 0"
            updateWeeklyStatus()
        }

        // Navigasi ke daily login
        binding.cardviewDaily.setOnClickListener {
            findNavController()
                .navigate(R.id.action_homeFragment_to_dailyLoginFragment)
        }

        // Navigasi ke riwayat mood
        binding.riwayatButton.setOnClickListener {
            findNavController()
                .navigate(R.id.action_homeFragment_to_riwayatFragment)
        }

        // Penyesuaian padding status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.headerLayout) { view, insets ->
            val topInset =
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            val paddingTop =
                topInset + (15 * resources.displayMetrics.density).toInt()
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

    // Dialog pemilih bulan dan tahun
    private fun showMonthYearPickerDialog(
        currentMonth: YearMonth,
        onConfirm: (Int, Int) -> Unit
    ) {
        val dialog = BottomSheetDialog(requireContext())
        val dialogView =
            LayoutInflater.from(context)
                .inflate(R.layout.dialog_month_year_picker, null)

        dialog.setContentView(dialogView)

        val currentYear = LocalDate.now().year
        val yearsList =
            (currentYear..currentYear + 5).map { it.toString() }

        val monthPicker =
            dialogView.findViewById<NumberPicker>(R.id.monthPicker)
        monthPicker.minValue = 0
        monthPicker.maxValue = monthsList.size - 1
        monthPicker.displayedValues = monthsList.toTypedArray()
        monthPicker.value = currentMonth.monthValue - 1
        monthPicker.wrapSelectorWheel = false

        val yearPicker =
            dialogView.findViewById<NumberPicker>(R.id.yearPicker)
        yearPicker.minValue = 0
        yearPicker.maxValue = yearsList.size - 1
        yearPicker.displayedValues = yearsList.toTypedArray()
        yearPicker.value =
            yearsList.indexOf(currentMonth.year.toString())
        yearPicker.wrapSelectorWheel = false

        dialogView.findViewById<Button>(R.id.confirmButton)
            .setOnClickListener {
                val selectedMonthIndex = monthPicker.value + 1
                val selectedYear =
                    yearsList[yearPicker.value].toInt()
                onConfirm(selectedMonthIndex, selectedYear)
                dialog.dismiss()
            }

        dialog.show()
    }

    // Update checklist login mingguan
    private fun updateWeeklyStatus() {

        // Bersihkan klaim lama
        ClaimPrefsHelper.cleanOldClaimsKeepThisWeek(requireContext())

        // Reset semua icon
        loginIcons.forEach { iconId ->
            binding.root.findViewById<ImageView>(iconId)
                ?.setImageResource(R.drawable.ic_nocheck)
        }

        // Ambil tanggal klaim
        val claimDates =
            ClaimPrefsHelper.getAllClaimDates(requireContext())

        val today = LocalDate.now()
        val weekStart =
            today.minusDays((today.dayOfWeek.value % 7).toLong())

        claimDates.forEach { date ->
            if (date >= weekStart) {
                val dayIndex =
                    ChronoUnit.DAYS.between(weekStart, date).toInt()
                if (dayIndex in 0..6) {
                    binding.root
                        .findViewById<ImageView>(loginIcons[dayIndex])
                        ?.setImageResource(R.drawable.ic_checkbox)
                }
            }
        }
    }

    // Container untuk setiap tanggal di kalender
    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView =
            view.findViewById(R.id.calendarDayText)
        val emojiIcon: ImageView =
            view.findViewById(R.id.emojiIcon)
    }
}
