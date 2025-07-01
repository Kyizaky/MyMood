package com.example.skripsta

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.adapter.ActivityRankingAdapter
import com.example.skripsta.adapter.FeelingRankingAdapter
import com.example.skripsta.adapter.MoodLegendAdapter
import com.example.skripsta.data.UserViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StatFragment : Fragment() {

    private lateinit var mUserViewModel: UserViewModel

    private lateinit var legendRecyclerView: RecyclerView
    private lateinit var recyclerViewRanking: RecyclerView
    private lateinit var recyclerViewFeelingRanking: RecyclerView
    private lateinit var activityRankingAdapter: ActivityRankingAdapter
    private lateinit var feelingRankingAdapter: FeelingRankingAdapter

    private lateinit var btnMonthActivity: Button
    private lateinit var btnYearActivity: Button
    private lateinit var btnMonthFeeling: Button
    private lateinit var btnYearFeeling: Button
    private lateinit var monthSpinnerPie: Spinner
    private lateinit var pieChart: PieChart

    private lateinit var btnMonthLineChart: Button
    private lateinit var btnYearLineChart: Button
    private lateinit var lineChartTrend: LineChart

    private lateinit var btnMonthCalendar: Button
    private lateinit var btnYearCalendar: Button

    private lateinit var progressBar: ProgressBar
    private lateinit var containerStat: LinearLayout
    private lateinit var containerLegend: CardView

    private var selectedMonthActivity: String = ""
    private var selectedYearActivity: String = ""
    private var selectedMonthFeeling: String = ""
    private var selectedYearFeeling: String = ""
    private var selectedMonthPie: String = ""
    private var selectedMonthTrend: String = ""
    private var selectedYearTrend: String = ""
    private var selectedMonthCalendar: String = ""
    private var selectedYearCalendar: String = ""

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
        btnMonthActivity = view.findViewById(R.id.btn_month)
        btnYearActivity = view.findViewById(R.id.btn_year)

        recyclerViewFeelingRanking = view.findViewById(R.id.recycler_view_feeling_ranking)
        recyclerViewFeelingRanking.layoutManager = LinearLayoutManager(requireContext())
        feelingRankingAdapter = FeelingRankingAdapter(emptyList())
        recyclerViewFeelingRanking.adapter = feelingRankingAdapter
        btnMonthFeeling = view.findViewById(R.id.btn_month_feeling)
        btnYearFeeling = view.findViewById(R.id.btn_year_feeling)

        btnMonthLineChart = view.findViewById(R.id.btn_month_trend)
        btnYearLineChart = view.findViewById(R.id.btn_year_trend)
        lineChartTrend = view.findViewById(R.id.line_chart_trend)

        btnMonthCalendar = view.findViewById(R.id.btn_month_calendar)
        btnYearCalendar = view.findViewById(R.id.btn_year_calendar)

        progressBar = view.findViewById(R.id.progress_bar)
        containerStat = view.findViewById(R.id.container_stat)
        containerLegend = view.findViewById(R.id.container_legend)

        pieChart = view.findViewById(R.id.moodPieChart)
        legendRecyclerView = view.findViewById(R.id.recycler_view_mood_legend)
        legendRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        monthSpinnerPie = view.findViewById(R.id.spinner_month_pie)

        val months = listOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()

        selectedMonthActivity = months[currentMonth]
        selectedYearActivity = currentYear
        btnMonthActivity.text = selectedMonthActivity
        btnYearActivity.text = selectedYearActivity

        selectedMonthFeeling = months[currentMonth]
        selectedYearFeeling = currentYear
        btnMonthFeeling.text = selectedMonthFeeling
        btnYearFeeling.text = selectedYearFeeling

        selectedMonthTrend = months[currentMonth]
        selectedYearTrend = currentYear
        btnMonthLineChart.text = selectedMonthTrend
        btnYearLineChart.text = selectedYearTrend

        selectedMonthCalendar = months[currentMonth]
        selectedYearCalendar = currentYear
        btnMonthCalendar.text = selectedMonthCalendar
        btnYearCalendar.text = selectedYearCalendar

        showLoading(true)

        setupRankingButtons()
        setupLineChartButtons()
        setupCalendarButtons()
        setupSpinnerPie()
        observeDataRanking()
        observeDataFeelingRanking()
        observeMoodData()

        return view
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            containerStat.visibility = View.GONE
            containerLegend.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
        } else {
            containerLegend.visibility = View.VISIBLE
            containerStat.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        showLoading(true)
        observeMoodData()
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
                    val mood = user.mood
                    moodCount[mood] = moodCount.getOrDefault(mood, 0) + 1
                }
            }

            updatePieChart(moodCount)
            updateLegend(moodCount)
        }
    }

    private fun updatePieChart(moodCount: Map<Int, Int>) {
        val entries = moodCount.entries
            .sortedBy { it.key }
            .mapNotNull { (moodInt, count) ->
                if (count > 0) PieEntry(count.toFloat(), moodInt.toString()) else null
            }

        if (entries.isEmpty()) {
            pieChart.data = null
            pieChart.invalidate()
            return
        }

        val colorMap = mapOf(
            1 to Color.parseColor("#FF6242"), // Marah
            2 to Color.parseColor("#82DF45"), // Jijik
            3 to Color.parseColor("#D19DFF"), // Takut
            4 to Color.parseColor("#B0DDFF"), // Sedih
            5 to Color.parseColor("#FFEE56"), // Bahagia
            6 to Color.parseColor("#FFF1D8")  // Netral
        )

        val colors = entries.map { entry ->
            val moodInt = entry.label.toInt()
            colorMap[moodInt] ?: Color.GRAY
        }

        val dataSet = PieDataSet(entries, "").apply {
            setColors(colors)
            sliceSpace = 3f
            setDrawValues(false)
        }

        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = false
        pieChart.setDrawEntryLabels(false)
        pieChart.invalidate()
    }

    private fun updateLegend(moodCount: Map<Int, Int>) {
        val total = moodCount.values.sum().toFloat()
        val moodData = moodCount.map { (moodInt, count) ->
            val percentage = if (total > 0) "%.1f%%".format((count / total) * 100) else "0.0%"
            Pair(moodInt, percentage)
        }

        legendRecyclerView.adapter = MoodLegendAdapter(moodData)
    }

    private fun setupRankingButtons() {
        val months = listOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        val years = (2020..2025).map { it.toString() }

        selectedMonthActivity = months[Calendar.getInstance().get(Calendar.MONTH)]
        selectedYearActivity = Calendar.getInstance().get(Calendar.YEAR).toString()
        btnMonthActivity.text = selectedMonthActivity
        btnYearActivity.text = selectedYearActivity

        selectedMonthFeeling = months[Calendar.getInstance().get(Calendar.MONTH)]
        selectedYearFeeling = Calendar.getInstance().get(Calendar.YEAR).toString()
        btnMonthFeeling.text = selectedMonthFeeling
        btnYearFeeling.text = selectedYearFeeling

        btnMonthActivity.setOnClickListener {
            showPickerDialog(isMonth = true, months, years, section = "activity")
        }

        btnYearActivity.setOnClickListener {
            showPickerDialog(isMonth = false, months, years, section = "activity")
        }

        btnMonthFeeling.setOnClickListener {
            showPickerDialog(isMonth = true, months, years, section = "feeling")
        }

        btnYearFeeling.setOnClickListener {
            showPickerDialog(isMonth = false, months, years, section = "feeling")
        }
    }

    private fun setupCalendarButtons() {
        val months = listOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        val years = (2020..2025).map { it.toString() }

        selectedMonthCalendar = months[Calendar.getInstance().get(Calendar.MONTH)]
        selectedYearCalendar = Calendar.getInstance().get(Calendar.YEAR).toString()
        btnMonthCalendar.text = selectedMonthCalendar
        btnYearCalendar.text = selectedYearCalendar

        btnMonthCalendar.setOnClickListener {
            showPickerDialog(isMonth = true, months, years, section = "calendar")
        }

        btnYearCalendar.setOnClickListener {
            showPickerDialog(isMonth = false, months, years, section = "calendar")
        }
    }

    private fun showPickerDialog(isMonth: Boolean, months: List<String>, years: List<String>, section: String) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_picker, null)
        dialog.setContentView(view)

        val numberPicker = view.findViewById<NumberPicker>(R.id.number_picker)
        val btnCancel = view.findViewById<ImageButton>(R.id.btn_cancel)
        val btnConfirm = view.findViewById<Button>(R.id.btn_confirm)

        numberPicker.wrapSelectorWheel = false

        when (section) {
            "activity" -> {
                if (isMonth) {
                    numberPicker.minValue = 0
                    numberPicker.maxValue = months.size - 1
                    numberPicker.displayedValues = months.toTypedArray()
                    numberPicker.value = months.indexOf(selectedMonthActivity).coerceAtLeast(0)
                } else {
                    numberPicker.minValue = 0
                    numberPicker.maxValue = years.size - 1
                    numberPicker.displayedValues = years.toTypedArray()
                    numberPicker.value = years.indexOf(selectedYearActivity).coerceAtLeast(0)
                }
            }
            "feeling" -> {
                if (isMonth) {
                    numberPicker.minValue = 0
                    numberPicker.maxValue = months.size - 1
                    numberPicker.displayedValues = months.toTypedArray()
                    numberPicker.value = months.indexOf(selectedMonthFeeling).coerceAtLeast(0)
                } else {
                    numberPicker.minValue = 0
                    numberPicker.maxValue = years.size - 1
                    numberPicker.displayedValues = years.toTypedArray()
                    numberPicker.value = years.indexOf(selectedYearFeeling).coerceAtLeast(0)
                }
            }
            "trend" -> {
                if (isMonth) {
                    numberPicker.minValue = 0
                    numberPicker.maxValue = months.size - 1
                    numberPicker.displayedValues = months.toTypedArray()
                    numberPicker.value = months.indexOf(selectedMonthTrend).coerceAtLeast(0)
                } else {
                    numberPicker.minValue = 0
                    numberPicker.maxValue = years.size - 1
                    numberPicker.displayedValues = years.toTypedArray()
                    numberPicker.value = years.indexOf(selectedYearTrend).coerceAtLeast(0)
                }
            }
            "calendar" -> {
                if (isMonth) {
                    numberPicker.minValue = 0
                    numberPicker.maxValue = months.size - 1
                    numberPicker.displayedValues = months.toTypedArray()
                    numberPicker.value = months.indexOf(selectedMonthCalendar).coerceAtLeast(0)
                } else {
                    numberPicker.minValue = 0
                    numberPicker.maxValue = years.size - 1
                    numberPicker.displayedValues = years.toTypedArray()
                    numberPicker.value = years.indexOf(selectedYearCalendar).coerceAtLeast(0)
                }
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            when (section) {
                "activity" -> {
                    if (isMonth) {
                        selectedMonthActivity = months[numberPicker.value]
                        btnMonthActivity.text = selectedMonthActivity
                        observeDataRanking()
                    } else {
                        selectedYearActivity = years[numberPicker.value]
                        btnYearActivity.text = selectedYearActivity
                        observeDataRanking()
                    }
                }
                "feeling" -> {
                    if (isMonth) {
                        selectedMonthFeeling = months[numberPicker.value]
                        btnMonthFeeling.text = selectedMonthFeeling
                        observeDataFeelingRanking()
                    } else {
                        selectedYearFeeling = years[numberPicker.value]
                        btnYearFeeling.text = selectedYearFeeling
                        observeDataFeelingRanking()
                    }
                }
                "trend" -> {
                    if (isMonth) {
                        selectedMonthTrend = months[numberPicker.value]
                        btnMonthLineChart.text = selectedMonthTrend
                        observeDataLineChart()
                    } else {
                        selectedYearTrend = years[numberPicker.value]
                        btnYearLineChart.text = selectedYearTrend
                        observeDataLineChart()
                    }
                }
                "calendar" -> {
                    if (isMonth) {
                        selectedMonthCalendar = months[numberPicker.value]
                        btnMonthCalendar.text = selectedMonthCalendar
                        observeMoodData()
                    } else {
                        selectedYearCalendar = years[numberPicker.value]
                        btnYearCalendar.text = selectedYearCalendar
                        observeMoodData()
                    }
                }
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun observeDataRanking() {
        mUserViewModel.readAllData.observe(viewLifecycleOwner) { users ->
            val activityCount = mutableMapOf<String, Pair<Int, Int>>()
            val dateFormat = SimpleDateFormat("MMMM yyyy", Locale("id"))
            val parseFormat = SimpleDateFormat("MM/dd/yyyy", Locale("id"))

            users.forEach { user ->
                val dateString = user.tanggal
                val date = try {
                    parseFormat.parse(dateString)
                } catch (e: Exception) {
                    null
                }

                if (date != null) {
                    val formattedDate = dateFormat.format(date)
                    val dateMonthYear = formattedDate.split(" ")
                    val dateMonth = dateMonthYear[0]
                    val dateYear = dateMonthYear[1]

                    if (dateMonth == selectedMonthActivity && dateYear == selectedYearActivity) {
                        val activity = user.activities.trim()
                        val iconResId = user.activityIcon

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

    private fun observeDataFeelingRanking() {
        mUserViewModel.readAllData.observe(viewLifecycleOwner) { users ->
            val feelingCount = mutableMapOf<String, Pair<Int, Int>>()
            val dateFormat = SimpleDateFormat("MMMM yyyy", Locale("id"))
            val parseFormat = SimpleDateFormat("MM/dd/yyyy", Locale("id"))

            users.forEach { user ->
                val dateString = user.tanggal
                val date = try {
                    parseFormat.parse(dateString)
                } catch (e: Exception) {
                    null
                }

                if (date != null) {
                    val formattedDate = dateFormat.format(date)
                    val dateMonthYear = formattedDate.split(" ")
                    val dateMonth = dateMonthYear[0]
                    val dateYear = dateMonthYear[1]

                    if (dateMonth == selectedMonthFeeling && dateYear == selectedYearFeeling) {
                        val feeling = user.perasaan.trim()

                        val currentData = feelingCount[feeling]
                        if (currentData != null) {
                            feelingCount[feeling] = Pair(currentData.first + 1, currentData.second)
                        } else {
                            feelingCount[feeling] = Pair(1, 0)
                        }
                    }
                }
            }

            val sortedFeelings = feelingCount.toList()
                .sortedByDescending { it.second.first }
                .take(3)
                .map { Triple(it.first, it.second.first, it.second.second) }

            feelingRankingAdapter.updateData(sortedFeelings)
        }
    }

    private fun setupLineChartButtons() {
        val months = listOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        val years = (2020..2025).map { it.toString() }

        selectedMonthTrend = months[Calendar.getInstance().get(Calendar.MONTH)]
        selectedYearTrend = Calendar.getInstance().get(Calendar.YEAR).toString()
        btnMonthLineChart.text = selectedMonthTrend
        btnYearLineChart.text = selectedYearTrend

        btnMonthLineChart.setOnClickListener {
            showPickerDialog(isMonth = true, months, years, section = "trend")
        }

        btnYearLineChart.setOnClickListener {
            showPickerDialog(isMonth = false, months, years, section = "trend")
        }

        lineChartTrend.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(false)
            setDrawGridBackground(false)
            axisRight.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(true)
            xAxis.gridColor = Color.parseColor("#2A3637")
            xAxis.setDrawLabels(true)
            xAxis.setDrawAxisLine(true)
            xAxis.textColor = Color.BLACK
            axisLeft.setDrawGridLines(false)
            axisLeft.setDrawLabels(true)
            axisLeft.setDrawAxisLine(true)
            axisLeft.textColor = Color.BLACK
            setNoDataText("No chart data available.")
            setNoDataTextColor(Color.parseColor("#FFC107"))
        }

        observeDataLineChart()
    }

    private fun observeDataLineChart() {
        mUserViewModel.readAllData.observe(viewLifecycleOwner) { users ->
            val dateFormat = SimpleDateFormat("MMMM yyyy", Locale("id"))
            val parseFormat = SimpleDateFormat("MM/dd/yyyy", Locale("id"))

            val filteredData = users
                .mapNotNull { user ->
                    val date = try {
                        parseFormat.parse(user.tanggal)
                    } catch (e: Exception) {
                        println("Failed to parse date: ${user.tanggal}, error: $e")
                        null
                    }
                    if (date != null) {
                        val formattedDate = dateFormat.format(date)
                        val dateMonthYear = formattedDate.split(" ")
                        val dateMonth = dateMonthYear[0]
                        val dateYear = dateMonthYear[1]
                        if (dateMonth == selectedMonthTrend && dateYear == selectedYearTrend) {
                            Pair(date, user.mood)
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                }

            if (filteredData.isEmpty()) {
                lineChartTrend.data = null
                lineChartTrend.setNoDataText("No chart data available.")
                lineChartTrend.setNoDataTextColor(Color.parseColor("#FFC107"))
                lineChartTrend.invalidate()
                return@observe
            }

            val groupedByDate = filteredData
                .groupBy { parseFormat.format(it.first).split("/")[1] }
                .mapValues { entry ->
                    val moodCounts = entry.value.groupBy { it.second }.mapValues { it.value.size }
                    val maxCount = moodCounts.values.maxOrNull() ?: 0
                    val mostFrequentMoods = moodCounts.filter { it.value == maxCount }.keys
                    val mood = if (mostFrequentMoods.size > 1) {
                        entry.value.last().second
                    } else {
                        mostFrequentMoods.first()
                    }
                    Pair(entry.value.first().first, mood)
                }

            val sortedData = groupedByDate.entries
                .sortedBy { it.value.first }

            val dataDates = sortedData.map { it.value.first }.distinct()

            if (dataDates.isEmpty()) {
                lineChartTrend.data = null
                lineChartTrend.invalidate()
                return@observe
            }

            val lastDate = dataDates.last()

            val calendar = Calendar.getInstance().apply { time = lastDate }
            val maxDayInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            calendar.set(Calendar.DAY_OF_MONTH, maxDayInMonth)
            val maxDateInMonth = calendar.time

            calendar.time = lastDate
            calendar.add(Calendar.DAY_OF_MONTH, 5)
            val hPlusFiveDate = calendar.time
            val maxDate = if (hPlusFiveDate > maxDateInMonth) maxDateInMonth else hPlusFiveDate

            val displayDates = dataDates.toMutableList()
            calendar.time = lastDate
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            while (calendar.time <= maxDate) {
                displayDates.add(calendar.time)
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            val dateLabels = displayDates.map { parseFormat.format(it).split("/")[1] }

            val entries = sortedData.mapIndexed { index, entry ->
                Entry(index.toFloat(), entry.value.second.toFloat())
            }

            val dataSet = LineDataSet(entries, "Mood Trend").apply {
                setDrawFilled(true)
                fillDrawable = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(Color.parseColor("#4CAF50"), Color.parseColor("#2196F3"))
                )
                color = Color.parseColor("#4CAF50")
                setDrawCircles(true)
                setDrawValues(false)
                lineWidth = 2f
                circleRadius = 4f
                setCircleColor(Color.parseColor("#FF6242"))
            }

            val lineData = LineData(dataSet)
            lineChartTrend.data = lineData

            lineChartTrend.xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(dateLabels)
                granularity = 1f
                labelCount = dateLabels.size
                axisMinimum = 0f
                axisMaximum = (dateLabels.size - 1).toFloat()
                setDrawLabels(true)
                setDrawAxisLine(true)
                labelRotationAngle = 0f
            }

            lineChartTrend.axisLeft.apply {
                axisMinimum = 0.5f
                axisMaximum = 6.5f
                labelCount = 6
                valueFormatter = object : ValueFormatter() {
                    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                        return when (value.toInt()) {
                            1 -> "😡" // Marah
                            2 -> "🤢" // Jijik
                            3 -> "😨" // Takut
                            4 -> "😢" // Sedih
                            5 -> "😊" // Bahagia
                            6 -> "😐" // Netral
                            else -> ""
                        }
                    }
                }
                setDrawLabels(true)
                setDrawAxisLine(true)
                textColor = Color.WHITE
                setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            }

            lineChartTrend.invalidate()
        }
    }

    private fun observeMoodData() {
        mUserViewModel.readAllData.observe(viewLifecycleOwner) { users ->
            val moodCountPerDay = mutableMapOf<String, Int>()
            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale("id"))

            users.forEach { user ->
                val date = try {
                    dateFormat.parse(user.tanggal)
                } catch (e: Exception) {
                    null
                }

                if (date != null) {
                    val key = SimpleDateFormat("dd/MM", Locale("id")).format(date)
                    moodCountPerDay[key] = user.mood
                }
            }
            generateMoodCalendar(moodCountPerDay)
            showLoading(false)  // Saat mulai generate
        }
    }

    private fun generateMoodCalendar(moodData: Map<String, Int>) {
        val gridLayout = requireView().findViewById<GridLayout>(R.id.moodCalendarGrid)
        gridLayout.removeAllViews()

        val columnCount = 13 // 1 untuk angka hari + 12 bulan
        val rowCount = 33    // 1 header + 31 hari + 1 footer
        gridLayout.columnCount = columnCount
        gridLayout.rowCount = rowCount

        // Gunakan post untuk memastikan lebar gridLayout tersedia
        gridLayout.post {
            // Lebar aktual GridLayout (setelah layout diukur)
            val availableWidth = gridLayout.width
            val marginBetweenCells = 1 // Margin antar sel (kiri dan kanan masing-masing 1dp)
            val totalMargin = (columnCount - 1) * marginBetweenCells * 2 // Total margin antar sel
            val cellSize = maxOf((availableWidth - totalMargin) / columnCount, 20) // Minimal 20dp untuk visibilitas

            // Sesuaikan ukuran teks berdasarkan lebar sel
            val textSize = when {
                cellSize < 25 -> 5f
                cellSize < 30 -> 6f
                cellSize < 40 -> 8f
                cellSize < 60 -> 10f
                else -> 12f
            }

            val months = listOf(" ", "J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")

            // Header (baris 0)
            for (col in 0 until columnCount) {
                val params = GridLayout.LayoutParams().apply {
                    width = cellSize
                    height = cellSize
                    rowSpec = GridLayout.spec(0)
                    columnSpec = GridLayout.spec(col)
                    setMargins(marginBetweenCells, marginBetweenCells, marginBetweenCells, marginBetweenCells)
                }

                val tv = TextView(requireContext()).apply {
                    text = months[col]
                    gravity = Gravity.CENTER
                    this.textSize = textSize
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(Color.BLACK)
                    layoutParams = params
                }

                gridLayout.addView(tv)
            }

            // Isi grid: baris 1–31 (hari ke-1 s.d. 31)
            for (day in 1..31) {
                val row = day

                for (col in 0 until columnCount) {
                    val params = GridLayout.LayoutParams().apply {
                        width = cellSize
                        height = cellSize
                        rowSpec = GridLayout.spec(row)
                        columnSpec = GridLayout.spec(col)
                        setMargins(marginBetweenCells, marginBetweenCells, marginBetweenCells, marginBetweenCells)
                    }

                    if (col == 0) {
                        // Kolom angka hari
                        val tv = TextView(requireContext()).apply {
                            text = day.toString()
                            gravity = Gravity.CENTER
                            this.textSize = textSize
                            setTextColor(Color.BLACK)
                            layoutParams = params
                        }
                        gridLayout.addView(tv)
                    } else {
                        val key = "%02d/%02d".format(day, col)
                        val moodColor = moodData[key]?.let { getMoodColor(it) } ?: Color.TRANSPARENT

                        val view = View(requireContext()).apply {
                            background = GradientDrawable().apply {
                                setColor(moodColor)
                                setStroke(1, Color.LTGRAY)
                                cornerRadius = if (cellSize < 25) 1f else if (cellSize < 30) 2f else 4f
                            }
                            layoutParams = params
                        }
                        gridLayout.addView(view)
                    }
                }
            }

            // Footer (baris ke-32, index 32 karena 0-based)
            for (col in 0 until columnCount) {
                val footerText = if (col == 0) "" else months[col]

                val params = GridLayout.LayoutParams().apply {
                    width = cellSize
                    height = cellSize
                    rowSpec = GridLayout.spec(rowCount - 1)
                    columnSpec = GridLayout.spec(col)
                    setMargins(marginBetweenCells, marginBetweenCells, marginBetweenCells, marginBetweenCells)
                }

                val tv = TextView(requireContext()).apply {
                    text = footerText
                    gravity = Gravity.CENTER
                    this.textSize = textSize
                    setTypeface(null, Typeface.BOLD_ITALIC)
                    setTextColor(Color.BLACK)
                    layoutParams = params
                }

                gridLayout.addView(tv)
            }
        }
    }

    private fun getMoodColor(mood: Int): Int {
        return when (mood) {
            1 -> Color.parseColor("#FF6242") // Marah
            2 -> Color.parseColor("#82DF45") // Jijik
            3 -> Color.parseColor("#D19DFF") // Takut
            4 -> Color.parseColor("#B0DDFF") // Sedih
            5 -> Color.parseColor("#FFEE56") // Bahagia
            6 -> Color.parseColor("#FFF1D8") // Netral
            else -> Color.TRANSPARENT
        }
    }
}