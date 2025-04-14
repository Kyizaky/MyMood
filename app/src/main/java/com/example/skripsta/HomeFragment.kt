package com.example.skripsta

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.skripsta.data.User
import com.example.skripsta.data.UserViewModel
import com.example.skripsta.databinding.FragmentHomeBinding
import com.kizitonwose.calendar.core.*
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var userViewModel: UserViewModel
    private var usersWithData: List<User> = emptyList()
    private val dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    private val moodDates = mutableSetOf<LocalDate>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        userViewModel.readAllData.observe(viewLifecycleOwner) { users ->
            usersWithData = users
            binding.calendarView.notifyCalendarChanged() // refresh calendar setelah data di-load
        }


        val currentMonth = YearMonth.now()
        val firstDayOfWeek = firstDayOfWeekFromLocale()
        val calendarView = binding.calendarView
        calendarView.setup(
            startMonth = currentMonth.minusMonths(12),
            endMonth = currentMonth.plusMonths(12),
            firstDayOfWeek = firstDayOfWeek
        )
        calendarView.scrollToMonth(currentMonth)

        val monthYearText = binding.monthYearText
        val btnPrevious = binding.btnPreviousMonth
        val btnNext = binding.btnNextMonth
        fun updateMonthHeader(month: YearMonth) {
            val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
            monthYearText.text = month.format(formatter)
        }

        updateMonthHeader(currentMonth)

        calendarView.monthScrollListener = { month ->
            updateMonthHeader(month.yearMonth)
        }

        btnPrevious.setOnClickListener {
            calendarView.findFirstVisibleMonth()?.let {
                calendarView.smoothScrollToMonth(it.yearMonth.minusMonths(1))
            }
        }

        btnNext.setOnClickListener {
            calendarView.findFirstVisibleMonth()?.let {
                calendarView.smoothScrollToMonth(it.yearMonth.plusMonths(1))
            }
        }
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

                    // Data mood di tanggal ini
                    val entriesOnThisDate = usersWithData.filter {
                        it.tanggal == date.format(dateFormatter)
                    }

                    if (entriesOnThisDate.isNotEmpty()) {
                        // Hitung mood yang paling sering
                        val moodCountMap = entriesOnThisDate.groupingBy { it.mood }.eachCount()
                        val maxCount = moodCountMap.values.maxOrNull()
                        val mostFrequentMoods = moodCountMap.filterValues { it == maxCount }.keys

                        // Pilih mood terbaru jika imbang
                        val chosenMood = entriesOnThisDate.lastOrNull { it.mood in mostFrequentMoods }?.mood

                        if (chosenMood != null) {
                            emojiIcon.visibility = View.VISIBLE
                            emojiIcon.setImageResource(getMoodEmojiDrawable(chosenMood))
                            dayText.text = "" // Kosongkan angka, hanya tampilkan emoji
                        } else {
                            emojiIcon.visibility = View.GONE
                            dayText.text = date.dayOfMonth.toString()
                        }
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

        // Observe data dari database
        userViewModel.readAllData.observe(viewLifecycleOwner) { userList ->
            moodDates.clear()
            moodDates.addAll(userList.mapNotNull {
                try {
                    LocalDate.parse(it.tanggal, dateFormatter)
                } catch (e: Exception) {
                    null
                }
            })
            calendarView.notifyCalendarChanged() // Refresh tampilan kalender
        }

        return binding.root
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
            else -> R.drawable.ic_mood
        }
    }

}