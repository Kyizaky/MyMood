package com.example.skripsta

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.adapter.ActivityRankingAdapter
import com.example.skripsta.adapter.MoodLegendAdapter
import com.example.skripsta.data.UserViewModel
import com.example.skripsta.utils.MoodMarkerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StatFragment : Fragment() {

    private lateinit var mUserViewModel: UserViewModel
    private lateinit var legendRecyclerView: RecyclerView
    private lateinit var recyclerViewRanking: RecyclerView
    private lateinit var activityRankingAdapter: ActivityRankingAdapter
    private lateinit var monthSpinnerRanking: Spinner
    private lateinit var monthSpinnerPie: Spinner
    private lateinit var pieChart: PieChart
    private var selectedMonthRanking: String = ""
    private var selectedMonthPie: String = ""
    private lateinit var barChart: BarChart
    private lateinit var monthSpinnerBar: Spinner
    private var selectedMonthBar: String = ""
    private lateinit var btnPickDate: Button
    private lateinit var tvSelectedDate: TextView
    private var selectedDate: String = ""



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stat, container, false)

        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        recyclerViewRanking = view.findViewById(R.id.recycler_view_ranking)
        recyclerViewRanking.layoutManager = LinearLayoutManager(requireContext())
        activityRankingAdapter = ActivityRankingAdapter(emptyList())
        recyclerViewRanking.adapter = activityRankingAdapter
        monthSpinnerRanking = view.findViewById(R.id.spinner_month_ranking)

        pieChart = view.findViewById(R.id.moodPieChart)
        legendRecyclerView = view.findViewById(R.id.recycler_view_mood_legend)
        legendRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        monthSpinnerPie = view.findViewById(R.id.spinner_month_pie)

        barChart = view.findViewById(R.id.bar_chart_mood_time)
        monthSpinnerBar = view.findViewById(R.id.spinner_month_bar)

        btnPickDate = view.findViewById(R.id.btn_pick_date)
        tvSelectedDate = view.findViewById(R.id.tv_selected_date)

        btnPickDate.setOnClickListener {
            showDatePickerDialog()
        }
        setupSpinnerBar()
        setupSpinnerRanking()
        setupSpinnerPie()
        return view
    }

    private fun setupSpinnerPie() {
        val months = listOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        monthSpinnerPie.adapter = adapter

        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        monthSpinnerPie.setSelection(currentMonth)
        selectedMonthPie = months[currentMonth]

        monthSpinnerPie.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedMonthPie = months[position]
                observeDataPie()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun observeDataPie() {
        mUserViewModel.readAllData.observe(viewLifecycleOwner) { users ->
            val moodCount = mutableMapOf<Int, Int>()
            val dateFormat = SimpleDateFormat("MMMM", Locale("id"))

            users.forEach { user ->
                val dateString = user.tanggal
                val date = try {
                    SimpleDateFormat("MM/dd/yyyy", Locale("id")).parse(dateString)
                } catch (e: Exception) {
                    null
                }

                if (date != null && dateFormat.format(date) == selectedMonthPie) {
                    val mood = user.mood // INT
                    moodCount[mood] = moodCount.getOrDefault(mood, 0) + 1
                }
            }

            updatePieChart(moodCount)
            updateLegend(moodCount)
        }
    }

    private fun updatePieChart(moodCount: Map<Int, Int>) {
        val entries = moodCount.mapNotNull { (moodInt, count) ->
            val label = moodMapping[moodInt] ?: moodInt.toString() // Ambil label atau angka default
            PieEntry(count.toFloat(), label)
        }

        val colors = listOf(Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW, Color.MAGENTA)

        val dataSet = PieDataSet(entries, "").apply {
            setColors(colors)
            setDrawValues(false) // Hilangkan angka dalam chart
        }

        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.description.isEnabled = false // Hilangkan label deskripsi
        pieChart.legend.isEnabled = false // Hilangkan legend jika tidak diperlukan
        pieChart.invalidate() // Refresh chart
    }

    private fun updateLegend(moodCount: Map<Int, Int>) {
        val total = moodCount.values.sum().toFloat()
        val moodData = moodCount.map { (moodInt, count) ->
            val percentage = "%.1f%%".format((count / total) * 100)
            Pair(moodInt, percentage)
        }

        legendRecyclerView.adapter = MoodLegendAdapter(moodData)
    }

    private val moodMapping = mapOf(
        1 to "Marah",
        2 to "Jijik",
        3 to "Takut",
        4 to "Sedih",
        5 to "Bahagia",
        6 to "Netral"
    )



    private fun setupSpinnerRanking() {
        val months = listOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        monthSpinnerRanking.adapter = adapter

        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        monthSpinnerRanking.setSelection(currentMonth)
        selectedMonthRanking = months[currentMonth]

        monthSpinnerRanking.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedMonthRanking = months[position]
                observeDataRanking()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun observeDataRanking() {
        mUserViewModel.readAllData.observe(viewLifecycleOwner) { users ->
            val activityCount = mutableMapOf<String, Pair<Int, Int>>()
            val dateFormat = SimpleDateFormat("MMMM", Locale("id"))

            users.forEach { user ->
                val dateString = user.tanggal
                val date = try {
                    SimpleDateFormat("MM/dd/yyyy", Locale("id")).parse(dateString)
                } catch (e: Exception) {
                    null
                }


                if (date != null && dateFormat.format(date) == selectedMonthRanking) {
                    println("Matching data: ${user.tanggal}, activities: ${user.activities}")
                    val activities = user.activities.split(",").map { it.trim() }
                    val iconResId = user.activityIcon

                    activities.forEach { activity ->
                        val currentData = activityCount[activity]
                        if (currentData != null) {
                            activityCount[activity] = Pair(currentData.first + 1, currentData.second)
                        } else {
                            activityCount[activity] = Pair(1, iconResId)
                        }
                    }
                }
            }

            val sortedActivities = activityCount.toList()
                .sortedByDescending { it.second.first }
                .take(3)
                .map { Triple(it.first, it.second.first, it.second.second) }

            activityRankingAdapter.updateData(sortedActivities)
        }
    }



    private fun setupSpinnerBar() {
        val months = listOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        monthSpinnerBar.adapter = adapter

        val currentCalendar = Calendar.getInstance()
        val currentMonth = currentCalendar.get(Calendar.MONTH)
        val currentDate = SimpleDateFormat("MM/dd/yyyy", Locale("id")).format(currentCalendar.time)

        selectedMonthBar = months[currentMonth]
        selectedDate = currentDate
        tvSelectedDate.text = "Tanggal yang dipilih: $selectedDate"
        monthSpinnerBar.setSelection(currentMonth)

        observeDataBarChart() // Refresh data saat pertama kali tampil

        monthSpinnerBar.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedMonthBar = months[position]
                observeDataBarChart()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun observeDataBarChart() {
        mUserViewModel.readAllData.observe(viewLifecycleOwner) { users ->
            val moodByHour = mutableMapOf<Int, MutableList<Pair<Int, Long>>>()

            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale("id"))
            val timeFormat = SimpleDateFormat("HH:mm", Locale("id"))
            val selected = dateFormat.parse(selectedDate)

            users.forEach { user ->
                val userDate = try {
                    dateFormat.parse(user.tanggal)
                } catch (e: Exception) {
                    null
                }

                if (userDate == selected) {
                    val time = try {
                        timeFormat.parse(user.jam)
                    } catch (e: Exception) {
                        null
                    }

                    val hour = time?.hours ?: 0
                    val timestamp = time?.time ?: 0L

                    moodByHour.getOrPut(hour) { mutableListOf() }.add(user.mood to timestamp)
                }
            }

            val processedMoodByHour = (0..23).associateWith { hour ->
                val moodList = moodByHour[hour] ?: return@associateWith 0f

                // Hitung frekuensi mood
                val freqMap = moodList.groupingBy { it.first }.eachCount()

                // Ambil mood dengan frekuensi tertinggi
                val maxFreq = freqMap.values.maxOrNull() ?: 0
                val candidateMoods = freqMap.filterValues { it == maxFreq }.keys

                // Kalau hanya satu kandidat, langsung ambil
                if (candidateMoods.size == 1) {
                    candidateMoods.first().toFloat()
                } else {
                    // Ambil data terakhir dari kandidat yang memiliki waktu terbaru
                    val latest = moodList.filter { it.first in candidateMoods }
                        .maxByOrNull { it.second }

                    latest?.first?.toFloat() ?: 0f
                }
            }

            updateBarChart(processedMoodByHour)
        }
    }

    private fun updateBarChart(moodByHour: Map<Int, Float>) {
        val entries = moodByHour.map { (hour, avgMood) ->
            BarEntry(hour.toFloat(), avgMood)
        }

        val barDataSet = BarDataSet(entries, "Mood per Jam").apply {
            color = Color.CYAN
            valueTextSize = 12f
            setDrawValues(false) // Hilangkan angka di atas bar
        }


        val barData = BarData(barDataSet)
        barChart.data = barData
        barChart.description.isEnabled = false

        barChart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 6f
            granularity = 1f
            textSize = 14f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return when (value.toInt()) {
                        1 -> "😠"
                        2 -> "🤢"
                        3 -> "😱"
                        4 -> "😢"
                        5 -> "😊"
                        6 -> "😐"
                        else -> ""
                    }
                }
            }
        }

        barChart.axisRight.isEnabled = false

        barChart.xAxis.apply {
            setDrawGridLines(false)
            granularity = 1f
            textSize = 12f
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return String.format("%02d:00", value.toInt())
                }
            }
        }

        val marker = MoodMarkerView(requireContext(), R.layout.custom_marker_view)
        marker.chartView = barChart
        barChart.marker = marker

        barChart.invalidate()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale("id"))
            val selectedCalendar = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth, selectedDay)
            }
            selectedDate = dateFormat.format(selectedCalendar.time)
            tvSelectedDate.text = "Tanggal yang dipilih: $selectedDate"
            observeDataBarChart() // Refresh data saat tanggal berubah
        }, year, month, day).show()
    }



}
