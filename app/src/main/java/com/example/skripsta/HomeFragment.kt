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
import com.example.skripsta.data.entity.User
import com.example.skripsta.viewmodel.MoodEntryViewModel
import com.example.skripsta.viewmodel.UserViewModel
import com.example.skripsta.databinding.FragmentHomeBinding
import com.example.skripsta.utils.MoodUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kizitonwose.calendar.core.*
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
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

    val petDrawables = listOf("pet1", "pet2", "pet3")
    val pointsToEvolveList = listOf(7, 14, 30)

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
            updateWeeklyStatus()

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

            // Update total day
            binding.pointsText.text = "Total Day: ${moodEntries.map { it.tanggal }.distinct().size}"

            // Update pet di Home
            user?.let {
                updateHomePet(it)
                updateHomePetProgress(it)
            }

            updateWeeklyStatus()
        }

        // Navigasi ke daily login
        binding.cardviewPet.setOnClickListener {
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

        // Reset semua icon ke default
        loginIcons.forEach { iconId ->
            binding.root.findViewById<ImageView>(iconId)
                ?.setImageResource(R.drawable.ic_nocheck)
        }

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val weekStart = today.minusDays(today.dayOfWeek.value.toLong() % 7)
        val weekEnd = weekStart.plusDays(6)

        // Ambil mood hanya untuk minggu ini
        val moodDatesThisWeek: Set<LocalDate> = moodEntries
            .mapNotNull {
                runCatching { LocalDate.parse(it.tanggal, dbFormatter) }.getOrNull()
            }
            .filter { it in weekStart..weekEnd }
            .toSet()

        // Evaluasi tiap hari dalam minggu
        for (i in 0..6) {
            val date = weekStart.plusDays(i.toLong())
            val iconView = binding.root.findViewById<ImageView>(loginIcons[i])

            when {
                // ✅ Ada data mood
                date <= today && moodDatesThisWeek.contains(date) -> {
                    iconView?.setImageResource(R.drawable.ic_checkbox)
                }

                // 🚫 Sudah lewat / hari ini tapi kosong
                date <= yesterday && !moodDatesThisWeek.contains(date) -> {
                    iconView?.setImageResource(R.drawable.disable_check)
                }

                // ⬜ Hari depan
                else -> {
                    iconView?.setImageResource(R.drawable.ic_nocheck)
                }
            }
        }
    }

    // Pet update
    private fun updateHomePet(user: com.example.skripsta.data.entity.User) {

        val unlockedPets =
            user.unlockedPets.split(",").filter { it.isNotEmpty() }

        // Jika belum ada pet yang terbuka
        if (unlockedPets.isEmpty()) {
            binding.imageView4.setImageResource(R.drawable.ic_ask)
            return
        }

        // Ambil pet TERAKHIR (evolusi tertinggi)
        val latestPetName = unlockedPets.last()

        val drawableRes = resources.getIdentifier(
            latestPetName,
            "drawable",
            requireContext().packageName
        )

        if (drawableRes != 0) {
            binding.imageView4.setImageResource(drawableRes)
        } else {
            binding.imageView4.setImageResource(R.drawable.ic_ask)
        }
    }

    private fun updateHomePetProgress(user: User) {

        val currentIndex =
            user.currentPetIndex.coerceIn(0, petDrawables.size - 1)

        val isFinalEvolution =
            currentIndex >= petDrawables.size - 1

        if (isFinalEvolution) {
            binding.petProgress.visibility = View.GONE
            binding.petPoints.text = "Max evolution reached"
            return
        }

        val pointsToEvolve = pointsToEvolveList[currentIndex]
        val progress = user.points.coerceAtMost(pointsToEvolve)

        // Progress bar
        binding.petProgress.visibility = View.VISIBLE
        binding.petProgress.max = pointsToEvolve
        binding.petProgress.progress = progress

        // Text info
        val remaining = pointsToEvolve - user.points
        binding.petPoints.text =
            if (remaining > 0)
                "$remaining more points to evolve"
            else
                "Ready to evolve!"
    }

    // Container untuk setiap tanggal di kalender
    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView =
            view.findViewById(R.id.calendarDayText)
        val emojiIcon: ImageView =
            view.findViewById(R.id.emojiIcon)
    }
}
