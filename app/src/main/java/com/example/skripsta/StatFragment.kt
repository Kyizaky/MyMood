package com.example.skripsta

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*

class StatFragment : Fragment() {

    private lateinit var mUserViewModel: UserViewModel

    private lateinit var legendRecyclerView: RecyclerView
    private lateinit var recyclerViewRanking: RecyclerView
    private lateinit var recyclerViewFeelingRanking: RecyclerView
    private lateinit var activityRankingAdapter: ActivityRankingAdapter
    private lateinit var feelingRankingAdapter: FeelingRankingAdapter

    private lateinit var btnDateActivity: Button
    private lateinit var btnDateFeeling: Button
    private lateinit var btnDateLineChart: Button
    private lateinit var btnDateCalendar: Button
    private lateinit var monthSpinnerPie: Spinner
    private lateinit var pieChart: PieChart
    private lateinit var lineChartTrend: LineChart

    private lateinit var progressBar: ProgressBar
    private lateinit var containerStat: LinearLayout
    private lateinit var containerLegend: CardView

    private var selectedMonthActivity: String = ""
    private var selectedYearActivity: String = ""

    private var selectedMonthFeeling: String = ""
    private var selectedYearFeeling: String = ""

    private var selectedMonthLineChart: String = ""
    private var selectedYearLineChart: String = ""

    private var selectedMonthPie: String = ""
    private var selectedYearPie: String = ""
    private var selectedYearCalendar: String = ""

    private val months = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

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
        btnDateActivity = view.findViewById(R.id.btn_date_activity)

        recyclerViewFeelingRanking = view.findViewById(R.id.recycler_view_feeling_ranking)
        recyclerViewFeelingRanking.layoutManager = LinearLayoutManager(requireContext())
        feelingRankingAdapter = FeelingRankingAdapter(emptyList())
        recyclerViewFeelingRanking.adapter = feelingRankingAdapter
        btnDateFeeling = view.findViewById(R.id.btn_date_feeling)

        btnDateLineChart = view.findViewById(R.id.btn_date_trend)
        lineChartTrend = view.findViewById(R.id.line_chart_trend)

        btnDateCalendar = view.findViewById(R.id.btn_date_calendar)

        progressBar = view.findViewById(R.id.progress_bar)
        containerStat = view.findViewById(R.id.container_stat)
        containerLegend = view.findViewById(R.id.container_legend)

        pieChart = view.findViewById(R.id.moodPieChart)
        legendRecyclerView = view.findViewById(R.id.recycler_view_mood_legend)
        legendRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        monthSpinnerPie = view.findViewById(R.id.spinner_month_pie)

        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()

        selectedMonthActivity = months[currentMonth]
        selectedYearActivity = currentYear
        btnDateActivity.text = "${months[currentMonth]}/$currentYear"

        selectedMonthFeeling = months[currentMonth]
        selectedYearFeeling = currentYear
        btnDateFeeling.text = "${months[currentMonth]}/$currentYear"

        selectedMonthLineChart = months[currentMonth]
        selectedYearLineChart = currentYear
        btnDateLineChart.text = "${months[currentMonth]}/$currentYear"

        selectedMonthPie = months[currentMonth]
        selectedYearPie = currentYear
        selectedYearCalendar = currentYear
        btnDateCalendar.text = currentYear

        setupDateButtons()
        setupSpinnerPie()
        observeDataRanking()
        observeDataFeelingRanking()
        observeDataLineChart()
        observeMoodData()
        return view
    }

    // private fun showLoading(show: Boolean) {
    //    if (show) {
    //        containerStat.visibility = View.GONE
    //        containerLegend.visibility = View.GONE
    //        progressBar.visibility = View.VISIBLE
    //    } else {
    //        containerLegend.visibility = View.VISIBLE
    //        containerStat.visibility = View.VISIBLE
    //        progressBar.visibility = View.GONE
    //    }
    //}

    private fun setupSpinnerPie() {
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
            val parseFormat = SimpleDateFormat("MM/dd/yyyy", Locale("id"))
            val monthFormat = SimpleDateFormat("MM", Locale("id"))
            val yearFormat = SimpleDateFormat("yyyy", Locale("id"))
            val targetMonth = "%02d".format(months.indexOf(selectedMonthPie) + 1)
            val targetYear = selectedYearPie

            users.forEach { user ->
                val dateString = user.tanggal
                val date = try {
                    parseFormat.parse(dateString)
                } catch (e: Exception) {
                    Log.e("StatFragment", "Failed to parse date: $dateString, error: $e")
                    null
                }

                if (date != null) {
                    val formattedMonth = monthFormat.format(date)
                    val formattedYear = yearFormat.format(date)
                    if (formattedMonth == targetMonth && formattedYear == targetYear) {
                        val mood = user.mood
                        moodCount[mood] = moodCount.getOrDefault(mood, 0) + 1
                    }
                }
            }

            Log.d("StatFragment", "Pie Chart Data: $moodCount")
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
            Log.d("StatFragment", "No data for pie chart")
            return
        }

        val colorMap = mapOf(
            1 to Color.parseColor("#FF6242"), // Marah
            2 to Color.parseColor("#82DF45"),
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

    private fun setupDateButtons() {
        val years = (2020..2025).map { it.toString() }

        btnDateActivity.setOnClickListener {
            showPickerDialog(months, years, section = "activity")
        }

        btnDateFeeling.setOnClickListener {
            showPickerDialog(months, years, section = "feeling")
        }

        btnDateLineChart.setOnClickListener {
            showPickerDialog(months, years, section = "LineChart")
        }

        btnDateCalendar.setOnClickListener {
            showCalendarPickerDialog(years)
        }
    }

    private fun showPickerDialog(months: List<String>, years: List<String>, section: String) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_picker, null)
        dialog.setContentView(view)

        val monthPicker = view.findViewById<NumberPicker>(R.id.month_picker)
        val yearPicker = view.findViewById<NumberPicker>(R.id.year_picker)
        val btnCancel = view.findViewById<ImageButton>(R.id.btn_cancel)
        val btnConfirm = view.findViewById<Button>(R.id.btn_confirm)

        // Setup NumberPickers
        monthPicker.apply {
            wrapSelectorWheel = false
            minValue = 0
            maxValue = months.size - 1
            displayedValues = months.toTypedArray()
            value = when (section) {
                "activity" -> months.indexOf(selectedMonthActivity).coerceAtLeast(0)
                "feeling" -> months.indexOf(selectedMonthFeeling).coerceAtLeast(0)
                "LineChart" -> months.indexOf(selectedMonthLineChart).coerceAtLeast(0)
                else -> 0
            }
        }

        yearPicker.apply {
            wrapSelectorWheel = false
            minValue = 0
            maxValue = years.size - 1
            displayedValues = years.toTypedArray()
            value = when (section) {
                "activity" -> years.indexOf(selectedYearActivity).coerceAtLeast(0)
                "feeling" -> years.indexOf(selectedYearFeeling).coerceAtLeast(0)
                "LineChart" -> years.indexOf(selectedYearLineChart).coerceAtLeast(0)
                else -> 0
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            when (section) {
                "activity" -> {
                    selectedMonthActivity = months[monthPicker.value]
                    selectedYearActivity = years[yearPicker.value]
                    btnDateActivity.text = "${selectedMonthActivity}/${selectedYearActivity}"
                    observeDataRanking()
                }
                "feeling" -> {
                    selectedMonthFeeling = months[monthPicker.value]
                    selectedYearFeeling = years[yearPicker.value]
                    btnDateFeeling.text = "${selectedMonthFeeling}/${selectedYearFeeling}"
                    observeDataFeelingRanking()
                }
                "LineChart" -> {
                    selectedMonthLineChart = months[monthPicker.value]
                    selectedYearLineChart = years[yearPicker.value]
                    btnDateLineChart.text = "${selectedMonthLineChart}/${selectedYearLineChart}"
                    observeDataLineChart()
                }
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showCalendarPickerDialog(years: List<String>) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_calendar_picker, null)
        dialog.setContentView(view)

        val yearPicker = view.findViewById<NumberPicker>(R.id.year_picker)
        val btnCancel = view.findViewById<ImageButton>(R.id.btn_cancel)
        val btnConfirm = view.findViewById<Button>(R.id.btn_confirm)

        // Setup Year Picker
        yearPicker.apply {
            wrapSelectorWheel = false
            minValue = 0
            maxValue = years.size - 1
            displayedValues = years.toTypedArray()
            value = years.indexOf(selectedYearCalendar).coerceAtLeast(0)
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            selectedYearCalendar = years[yearPicker.value]
            btnDateCalendar.text = selectedYearCalendar
            observeMoodData()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun observeDataRanking() {
        mUserViewModel.readAllData.observe(viewLifecycleOwner) { users ->
            val activityCount = mutableMapOf<String, Pair<Int, Int>>()
            val parseFormat = SimpleDateFormat("MM/dd/yyyy", Locale("id"))
            val monthFormat = SimpleDateFormat("MM", Locale("id"))
            val yearFormat = SimpleDateFormat("yyyy", Locale("id"))
            val targetMonth = "%02d".format(months.indexOf(selectedMonthActivity) + 1)
            val targetYear = selectedYearActivity

            Log.d("StatFragment", "Ranking Target Month: $targetMonth, Year: $targetYear")

            users.forEach { user ->
                val dateString = user.tanggal
                val date = try {
                    parseFormat.parse(dateString)
                } catch (e: Exception) {
                    Log.e("StatFragment", "Failed to parse date: $dateString, error: $e")
                    null
                }

                if (date != null) {
                    val formattedMonth = monthFormat.format(date)
                    val formattedYear = yearFormat.format(date)
                    if (formattedMonth == targetMonth && formattedYear == targetYear) {
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

            Log.d("StatFragment", "Ranking Activities: $sortedActivities")
            activityRankingAdapter.updateData(sortedActivities)
        }
    }

    private fun observeDataFeelingRanking() {
        mUserViewModel.readAllData.observe(viewLifecycleOwner) { users ->
            val feelingCount = mutableMapOf<String, Pair<Int, Int>>()
            val parseFormat = SimpleDateFormat("MM/dd/yyyy", Locale("id"))
            val monthFormat = SimpleDateFormat("MM", Locale("id"))
            val yearFormat = SimpleDateFormat("yyyy", Locale("id"))
            val targetMonth = "%02d".format(months.indexOf(selectedMonthFeeling) + 1)
            val targetYear = selectedYearFeeling

            Log.d("StatFragment", "Feeling Ranking Target Month: $targetMonth, Year: $targetYear")

            users.forEach { user ->
                val dateString = user.tanggal
                val date = try {
                    parseFormat.parse(dateString)
                } catch (e: Exception) {
                    Log.e("StatFragment", "Failed to parse date: $dateString, error: $e")
                    null
                }

                if (date != null) {
                    val formattedMonth = monthFormat.format(date)
                    val formattedYear = yearFormat.format(date)
                    if (formattedMonth == targetMonth && formattedYear == targetYear) {
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

            Log.d("StatFragment", "Feeling Rankings: $sortedFeelings")
            feelingRankingAdapter.updateData(sortedFeelings)
        }
    }

    private fun observeDataLineChart() {
        mUserViewModel.readAllData.observe(viewLifecycleOwner) { users ->
            val parseFormat = SimpleDateFormat("MM/dd/yyyy", Locale("id"))
            val dayFormat = SimpleDateFormat("dd", Locale("id"))
            val monthFormat = SimpleDateFormat("MM", Locale("id"))
            val yearFormat = SimpleDateFormat("yyyy", Locale("id"))

            val targetMonth = "%02d".format(months.indexOf(selectedMonthLineChart) + 1)
            val targetYear = selectedYearLineChart

            val filteredData = users.mapNotNull { user ->
                val date = try {
                    parseFormat.parse(user.tanggal)
                } catch (e: Exception) {
                    Log.e("StatFragment", "Failed to parse date: ${user.tanggal}, error: $e")
                    null
                }
                if (date != null) {
                    val month = monthFormat.format(date)
                    val year = yearFormat.format(date)
                    if (month == targetMonth && year == targetYear) {
                        Pair(date, user.mood)
                    } else null
                } else null
            }

            if (filteredData.isEmpty()) {
                lineChartTrend.data = null
                lineChartTrend.setNoDataText("No chart data available for $targetMonth/$targetYear")
                lineChartTrend.setNoDataTextColor(Color.parseColor("#FFC107"))
                lineChartTrend.invalidate()
                return@observe
            }

            // Group by day, ambil mood paling sering
            val groupedByDate = filteredData
                .groupBy { dayFormat.format(it.first) }
                .mapValues { entry ->
                    val moodsCount = entry.value.groupBy { it.second }.mapValues { it.value.size }
                    val maxCount = moodsCount.values.maxOrNull() ?: 0
                    val mostFrequent = moodsCount.filter { it.value == maxCount }.keys
                    val mood = if (mostFrequent.size > 1) {
                        entry.value.last().second
                    } else {
                        mostFrequent.first()
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

            // Buat daftar tanggal sampai H+5
            val displayDates = dataDates.toMutableList()
            calendar.time = lastDate
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            while (calendar.time <= maxDate) {
                displayDates.add(calendar.time)
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            val calendarTmp = Calendar.getInstance()
            val dateLabels = displayDates.map {
                calendarTmp.time = it
                calendarTmp.get(Calendar.DAY_OF_MONTH).toString()
            }

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

            lineChartTrend.axisLeft.apply {
                axisMinimum = 0.5f
                axisMaximum = 6.5f
                labelCount = 6
                valueFormatter = object : ValueFormatter() {
                    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                        return when (value.toInt()) {
                            1 -> "😡"
                            2 -> "🤢"
                            3 -> "😨"
                            4 -> "😢"
                            5 -> "😊"
                            6 -> "😐"
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
            val parseFormat = SimpleDateFormat("MM/dd/yyyy", Locale("id"))
            val yearFormat = SimpleDateFormat("yyyy", Locale("id"))
            val targetYear = selectedYearCalendar

            Log.d("StatFragment", "Calendar Target Year: $targetYear")

            users.forEach { user ->
                val date = try {
                    parseFormat.parse(user.tanggal)
                } catch (e: Exception) {
                    Log.e("StatFragment", "Failed to parse date: ${user.tanggal}, error: $e")
                    null
                }

                if (date != null) {
                    val formattedYear = yearFormat.format(date)
                    if (formattedYear == targetYear) {
                        val key = SimpleDateFormat("dd/MM/yyyy", Locale("id")).format(date)
                        moodCountPerDay[key] = user.mood
                    }
                }
            }

            Log.d("StatFragment", "Calendar Mood Data: $moodCountPerDay")
            generateMoodCalendar(moodCountPerDay)
        }
    }

    private fun generateMoodCalendar(moodData: Map<String, Int>) {
        val gridLayout = requireView().findViewById<GridLayout>(R.id.moodCalendarGrid)
        gridLayout.removeAllViews()

        val columnCount = 13 // 1 untuk angka hari + 12 bulan
        val rowCount = 33    // 1 header + 31 hari + 1 footer
        gridLayout.columnCount = columnCount
        gridLayout.rowCount = rowCount

        gridLayout.post {
            val availableWidth = gridLayout.width
            val marginBetweenCells = 1
            val totalMargin = (columnCount - 1) * marginBetweenCells * 2
            val cellSize = maxOf((availableWidth - totalMargin) / columnCount, 20)

            val textSize = when {
                cellSize < 25 -> 5f
                cellSize < 30 -> 6f
                cellSize < 40 -> 8f
                cellSize < 60 -> 10f
                else -> 12f
            }

            val monthAbbreviations = listOf(" ", "J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")

            // Header
            for (col in 0 until columnCount) {
                val params = GridLayout.LayoutParams().apply {
                    width = cellSize
                    height = cellSize
                    rowSpec = GridLayout.spec(0)
                    columnSpec = GridLayout.spec(col)
                    setMargins(marginBetweenCells, marginBetweenCells, marginBetweenCells, marginBetweenCells)
                }

                val tv = TextView(requireContext()).apply {
                    text = monthAbbreviations[col]
                    gravity = Gravity.CENTER
                    this.textSize = textSize
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(Color.BLACK)
                    layoutParams = params
                }

                gridLayout.addView(tv)
            }

            // Days
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
                        val tv = TextView(requireContext()).apply {
                            text = day.toString()
                            gravity = Gravity.CENTER
                            this.textSize = textSize
                            setTextColor(Color.BLACK)
                            layoutParams = params
                        }
                        gridLayout.addView(tv)
                    } else {
                        val monthIndex = col
                        val key = "%02d/%02d/%s".format(day, monthIndex, selectedYearCalendar)
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

            // Footer
            for (col in 0 until columnCount) {
                val footerText = if (col == 0) "" else monthAbbreviations[col]
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