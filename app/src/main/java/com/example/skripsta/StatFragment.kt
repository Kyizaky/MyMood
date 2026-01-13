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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.adapter.ActivityRankingAdapter
import com.example.skripsta.adapter.FeelingRankingAdapter
import com.example.skripsta.adapter.MoodLegendAdapter
import com.example.skripsta.viewmodel.MoodEntryViewModel
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
import com.example.skripsta.utils.MoodUtils
import com.example.skripsta.utils.MoodUtils.formatMonthName

class StatFragment : Fragment() {

    private lateinit var mMoodEntryViewModel: MoodEntryViewModel

    private lateinit var legendRecyclerView: RecyclerView
    private lateinit var recyclerViewRanking: RecyclerView
    private lateinit var recyclerViewFeelingRanking: RecyclerView
    private lateinit var activityRankingAdapter: ActivityRankingAdapter
    private lateinit var feelingRankingAdapter: FeelingRankingAdapter

    private lateinit var btnDateActivity: Button
    private lateinit var btnDateFeeling: Button
    private lateinit var btnDateLineChart: Button
    private lateinit var btnDateCalendar: Button
    private lateinit var btnDatePieChart: Button
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

    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate layout
        val view = inflater.inflate(R.layout.fragment_stat, container, false)

        // Inisialisasi ViewModel
        mMoodEntryViewModel = ViewModelProvider(this).get(MoodEntryViewModel::class.java)

        // Setup RecyclerView ranking aktivitas
        recyclerViewRanking = view.findViewById(R.id.recycler_view_ranking)
        recyclerViewRanking.layoutManager = LinearLayoutManager(requireContext())
        activityRankingAdapter = ActivityRankingAdapter(emptyList())
        recyclerViewRanking.adapter = activityRankingAdapter
        btnDateActivity = view.findViewById(R.id.btn_date_activity)

        // Setup RecyclerView ranking perasaan
        recyclerViewFeelingRanking = view.findViewById(R.id.recycler_view_feeling_ranking)
        recyclerViewFeelingRanking.layoutManager = LinearLayoutManager(requireContext())
        feelingRankingAdapter = FeelingRankingAdapter(emptyList())
        recyclerViewFeelingRanking.adapter = feelingRankingAdapter
        btnDateFeeling = view.findViewById(R.id.btn_date_feeling)

        // Setup LineChart trend
        btnDateLineChart = view.findViewById(R.id.btn_date_trend)
        lineChartTrend = view.findViewById(R.id.line_chart_trend)

        // Button kalender mood tahunan
        btnDateCalendar = view.findViewById(R.id.btn_date_calendar)

        // UI pendukung
        progressBar = view.findViewById(R.id.progress_bar)
        containerStat = view.findViewById(R.id.container_stat)
        containerLegend = view.findViewById(R.id.container_legend)

        // Setup PieChart
        btnDatePieChart = view.findViewById(R.id.btn_date_pie)
        pieChart = view.findViewById(R.id.moodPieChart)
        legendRecyclerView = view.findViewById(R.id.recycler_view_mood_legend)
        legendRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Set default bulan & tahun (bulan berjalan)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()

        selectedMonthPie = months[currentMonth]
        selectedYearPie = currentYear
        btnDatePieChart.text = "${formatMonthName(selectedMonthPie)} $currentYear"

        selectedMonthActivity = months[currentMonth]
        selectedYearActivity = currentYear
        btnDateActivity.text = "${formatMonthName(selectedMonthActivity)} $currentYear"

        selectedMonthFeeling = months[currentMonth]
        selectedYearFeeling = currentYear
        btnDateFeeling.text = "${formatMonthName(selectedMonthFeeling)} $currentYear"

        selectedMonthLineChart = months[currentMonth]
        selectedYearLineChart = currentYear
        btnDateLineChart.text = "${formatMonthName(selectedMonthLineChart)} $currentYear"

        selectedMonthPie = months[currentMonth]
        selectedYearPie = currentYear
        selectedYearCalendar = currentYear
        btnDateCalendar.text = currentYear

        // Setup listener & observer
        setupDateButtons()
        observeDataPie()
        observeDataRanking()
        observeDataFeelingRanking()
        observeDataLineChart()
        observeMoodData()
        return view
    }

    // ================= PIE CHART =================

    // Observasi data untuk pie chart mood
    private fun observeDataPie() {
        mMoodEntryViewModel.readAllData.observe(viewLifecycleOwner) { users ->
            // Map untuk menghitung jumlah tiap mood
            val moodCount = mutableMapOf<Int, Int>()

            val parseFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val monthFormat = SimpleDateFormat("MM", Locale.ENGLISH)
            val yearFormat = SimpleDateFormat("yyyy", Locale.ENGLISH)
            val targetMonth = "%02d".format(months.indexOf(selectedMonthPie) + 1)
            val targetYear = selectedYearPie

            // Filter data berdasarkan bulan & tahun
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

            // Update chart & legend
            Log.d("StatFragment", "Pie Chart Data: $moodCount")
            updatePieChart(moodCount)
            updateLegend(moodCount)
        }
    }

    // Membuat dan menampilkan PieChart
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
            1 to Color.parseColor("#cf4e3b"), // Marah
            2 to Color.parseColor("#f57e53"),
            3 to Color.parseColor("#f5bc6f"), // Takut
            4 to Color.parseColor("#d4d039"), // Sedih
            5 to Color.parseColor("#8dc363") // Netral
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

    // Update legenda pie chart
    private fun updateLegend(moodCount: Map<Int, Int>) {
        val total = moodCount.values.sum().toFloat()
        val moodData = moodCount.map { (moodInt, count) ->
            val percentage = if (total > 0) "%.1f%%".format((count / total) * 100) else "0.0%"
            Pair(moodInt, percentage)
        }

        legendRecyclerView.adapter = MoodLegendAdapter(moodData)
    }

    // ================= BUTTON DATE PICKER =================

    // Setup semua tombol pemilih tanggal
    private fun setupDateButtons() {
        val years = (2025..2030).map { it.toString() }

        btnDatePieChart.setOnClickListener {
            showPickerDialog(months, years, section = "pie")
        }

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

    // Menampilkan dialog BottomSheet untuk memilih bulan dan tahun
    private fun showPickerDialog(months: List<String>, years: List<String>, section: String) {
        // Membuat BottomSheetDialog
        val dialog = BottomSheetDialog(requireContext())
        // Inflate layout picker
        val view = layoutInflater.inflate(R.layout.bottom_sheet_picker, null)
        dialog.setContentView(view)

        // Inisialisasi komponen picker dan tombol
        val monthPicker = view.findViewById<NumberPicker>(R.id.month_picker)
        val yearPicker = view.findViewById<NumberPicker>(R.id.year_picker)
        val btnCancel = view.findViewById<ImageButton>(R.id.btn_cancel)
        val btnConfirm = view.findViewById<Button>(R.id.btn_confirm)

        // Konfigurasi NumberPicker untuk bulan
        monthPicker.apply {
            wrapSelectorWheel = false                    // Tidak loop angka
            minValue = 0                                 // Index awal
            maxValue = months.size - 1                   // Index akhir
            displayedValues = months.toTypedArray()      // Nama bulan ditampilkan
            value = when (section) {                     // Set default sesuai section
                "activity" -> months.indexOf(selectedMonthActivity).coerceAtLeast(0)
                "feeling" -> months.indexOf(selectedMonthFeeling).coerceAtLeast(0)
                "LineChart" -> months.indexOf(selectedMonthLineChart).coerceAtLeast(0)
                "pie" -> months.indexOf(selectedMonthPie).coerceAtLeast(0)
                else -> 0
            }
        }

        // Konfigurasi NumberPicker untuk tahun
        yearPicker.apply {
            wrapSelectorWheel = false
            minValue = 0
            maxValue = years.size - 1
            displayedValues = years.toTypedArray()
            value = when (section) {
                "activity" -> years.indexOf(selectedYearActivity).coerceAtLeast(0)
                "feeling" -> years.indexOf(selectedYearFeeling).coerceAtLeast(0)
                "LineChart" -> years.indexOf(selectedYearLineChart).coerceAtLeast(0)
                "pie" -> years.indexOf(selectedYearPie).coerceAtLeast(0)
                else -> 0
            }
        }

        // Tombol batal menutup dialog
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Tombol konfirmasi menyimpan pilihan bulan & tahun
        btnConfirm.setOnClickListener {
            when (section) {

                // Update filter Pie Chart
                "pie" -> {
                    selectedMonthPie = months[monthPicker.value]
                    selectedYearPie = years[yearPicker.value]
                    btnDatePieChart.text = "${formatMonthName(selectedMonthPie)} ${selectedYearPie}"
                    observeDataPie()
                }

                // Update ranking aktivitas
                "activity" -> {
                    selectedMonthActivity = months[monthPicker.value]
                    selectedYearActivity = years[yearPicker.value]
                    btnDateActivity.text = "${formatMonthName(selectedMonthActivity)} ${selectedYearActivity}"
                    observeDataRanking()
                }

                // Update ranking perasaan
                "feeling" -> {
                    selectedMonthFeeling = months[monthPicker.value]
                    selectedYearFeeling = years[yearPicker.value]
                    btnDateFeeling.text = "${formatMonthName(selectedMonthFeeling)} ${selectedYearFeeling}"
                    observeDataFeelingRanking()
                }

                // Update Line Chart tren mood
                "LineChart" -> {
                    selectedMonthLineChart = months[monthPicker.value]
                    selectedYearLineChart = years[yearPicker.value]
                    btnDateLineChart.text = "${formatMonthName(selectedMonthLineChart)} ${selectedYearLineChart}"
                    observeDataLineChart()
                }
            }
            dialog.dismiss()
        }

        // Menampilkan dialog
        dialog.show()
    }

    // Menampilkan dialog BottomSheet untuk memilih tahun kalender mood
    private fun showCalendarPickerDialog(years: List<String>) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_calendar_picker, null)
        dialog.setContentView(view)

        // Inisialisasi Year Picker dan tombol
        val yearPicker = view.findViewById<NumberPicker>(R.id.year_picker)
        val btnCancel = view.findViewById<ImageButton>(R.id.btn_cancel)
        val btnConfirm = view.findViewById<Button>(R.id.btn_confirm)

        // Konfigurasi picker tahun
        yearPicker.apply {
            wrapSelectorWheel = false
            minValue = 0
            maxValue = years.size - 1
            displayedValues = years.toTypedArray()
            value = years.indexOf(selectedYearCalendar).coerceAtLeast(0)
        }

        // Tombol batal
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Tombol konfirmasi memilih tahun kalender
        btnConfirm.setOnClickListener {
            selectedYearCalendar = years[yearPicker.value]
            btnDateCalendar.text = selectedYearCalendar
            observeMoodData()       // Refresh kalender mood
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun observeDataRanking() {

        // Mengamati seluruh data mood entry dari ViewModel
        mMoodEntryViewModel.readAllData.observe(viewLifecycleOwner) { users ->

            // Map untuk menyimpan jumlah aktivitas dan icon-nya
            // Key: nama aktivitas
            // Value: Pair(jumlah kemunculan, iconResId)
            val activityCount = mutableMapOf<String, Pair<Int, Int>>()

            // Formatter untuk parsing dan ekstraksi tanggal
            val parseFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val monthFormat = SimpleDateFormat("MM", Locale.ENGLISH)
            val yearFormat = SimpleDateFormat("yyyy", Locale.ENGLISH)

            // Menentukan bulan dan tahun yang dipilih user
            val targetMonth = "%02d".format(months.indexOf(selectedMonthActivity) + 1)
            val targetYear = selectedYearActivity

            // Log untuk debugging filter bulan dan tahun
            Log.d("StatFragment", "Ranking Target Month: $targetMonth, Year: $targetYear")

            // Iterasi seluruh data mood entry
            users.forEach { user ->
                // Mengambil string tanggal dari data
                val dateString = user.tanggal
                // Parsing string tanggal menjadi objek Date
                val date = try {
                    parseFormat.parse(dateString)
                } catch (e: Exception) {
                    // Log error jika parsing gagal
                    Log.e("StatFragment", "Failed to parse date: $dateString, error: $e")
                    null
                }

                // Jika parsing tanggal berhasil
                if (date != null) {

                    // Mengambil bulan dan tahun dari tanggal
                    val formattedMonth = monthFormat.format(date)
                    val formattedYear = yearFormat.format(date)

                    // Mengecek apakah data sesuai bulan dan tahun yang dipilih
                    if (formattedMonth == targetMonth && formattedYear == targetYear) {

                        // Mengambil nama aktivitas dan icon
                        val activity = user.activities.trim()
                        val iconResId = user.activityIcon

                        // Mengambil data aktivitas yang sudah ada
                        val currentData = activityCount[activity]

                        // Jika aktivitas sudah pernah dicatat, tambahkan jumlahnya
                        if (currentData != null) {
                            activityCount[activity] = Pair(currentData.first + 1, currentData.second)
                        } else {
                            // Jika aktivitas baru, set jumlah awal = 1
                            activityCount[activity] = Pair(1, iconResId)
                        }
                    }
                }
            }

            // Mengurutkan aktivitas berdasarkan jumlah terbanyak
            // Mengambil 3 aktivitas teratas
            val sortedActivities = activityCount.toList()
                .sortedByDescending { it.second.first }
                .take(3)
                .map { Triple(it.first, it.second.first, it.second.second) }

            // Log hasil ranking aktivitas
            Log.d("StatFragment", "Ranking Activities: $sortedActivities")
            // Mengirim data ranking ke adapter RecyclerView
            activityRankingAdapter.updateData(sortedActivities)
        }
    }

    private fun observeDataFeelingRanking() {

        // Mengamati seluruh data mood entry dari ViewModel
        mMoodEntryViewModel.readAllData.observe(viewLifecycleOwner) { users ->

            // Map untuk menyimpan jumlah kemunculan setiap perasaan
            // Key: nama perasaan
            // Value: Pair(jumlah kemunculan, icon/default value)
            val feelingCount = mutableMapOf<String, Pair<Int, Int>>()

            // Formatter untuk parsing dan pengambilan bulan serta tahun dari tanggal
            val parseFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val monthFormat = SimpleDateFormat("MM", Locale.ENGLISH)
            val yearFormat = SimpleDateFormat("yyyy", Locale.ENGLISH)

            // Menentukan bulan dan tahun yang dipilih user
            val targetMonth = "%02d".format(months.indexOf(selectedMonthFeeling) + 1)
            val targetYear = selectedYearFeeling

            // Log untuk memastikan filter bulan dan tahun
            Log.d("StatFragment", "Feeling Ranking Target Month: $targetMonth, Year: $targetYear")

            // Iterasi seluruh data mood entry
            users.forEach { user ->

                // Mengambil string tanggal dari data
                val dateString = user.tanggal

                // Parsing string tanggal menjadi objek Date
                val date = try {
                    parseFormat.parse(dateString)
                } catch (e: Exception) {
                    // Log error jika parsing tanggal gagal
                    Log.e("StatFragment", "Failed to parse date: $dateString, error: $e")
                    null
                }

                // Jika tanggal berhasil diparse
                if (date != null) {

                    // Mengambil bulan dan tahun dari tanggal
                    val formattedMonth = monthFormat.format(date)
                    val formattedYear = yearFormat.format(date)

                    // Mengecek apakah data sesuai dengan bulan dan tahun yang dipilih
                    if (formattedMonth == targetMonth && formattedYear == targetYear) {

                        // Mengambil nama perasaan dan menghapus spasi
                        val feeling = user.perasaan.trim()

                        // Mengambil data perasaan yang sudah tersimpan
                        val currentData = feelingCount[feeling]

                        // Jika perasaan sudah ada, tambahkan jumlahnya
                        if (currentData != null) {
                            feelingCount[feeling] =
                                Pair(currentData.first + 1, currentData.second)
                        } else {
                            // Jika perasaan baru, set jumlah awal = 1
                            feelingCount[feeling] = Pair(1, 0)
                        }
                    }
                }
            }

            // Mengurutkan perasaan berdasarkan jumlah terbanyak
            // Mengambil 3 perasaan teratas
            val sortedFeelings = feelingCount.toList()
                .sortedByDescending { it.second.first }
                .take(3)
                .map { Triple(it.first, it.second.first, it.second.second) }

            // Log hasil ranking perasaan
            Log.d("StatFragment", "Feeling Rankings: $sortedFeelings")

            // Mengirim data ranking perasaan ke adapter RecyclerView
            feelingRankingAdapter.updateData(sortedFeelings)
        }
    }

    private fun observeDataLineChart() {

        // Mengamati seluruh data mood dari ViewModel
        mMoodEntryViewModel.readAllData.observe(viewLifecycleOwner) { users ->

            // Formatter untuk parsing tanggal dan mengambil hari, bulan, tahun
            val parseFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val dayFormat = SimpleDateFormat("dd", Locale.ENGLISH)
            val monthFormat = SimpleDateFormat("MM", Locale.ENGLISH)
            val yearFormat = SimpleDateFormat("yyyy", Locale.ENGLISH)

            // Menentukan bulan dan tahun yang dipilih user
            val targetMonth = "%02d".format(months.indexOf(selectedMonthLineChart) + 1)
            val targetYear = selectedYearLineChart

            // Filter data berdasarkan bulan dan tahun yang dipilih
            val filteredData = users.mapNotNull { user ->

                // Parsing tanggal dari string
                val date = try {
                    parseFormat.parse(user.tanggal)
                } catch (e: Exception) {
                    // Log error jika parsing gagal
                    Log.e("StatFragment", "Failed to parse date: ${user.tanggal}, error: $e")
                    null
                }

                // Jika tanggal valid dan sesuai bulan & tahun
                if (date != null) {
                    val month = monthFormat.format(date)
                    val year = yearFormat.format(date)
                    if (month == targetMonth && year == targetYear) {
                        // Simpan pasangan tanggal dan nilai mood
                        Pair(date, user.mood)
                    } else null
                } else null
            }

            // Jika tidak ada data, tampilkan pesan kosong pada chart
            if (filteredData.isEmpty()) {
                lineChartTrend.data = null
                lineChartTrend.setNoDataText("No chart data available for $targetMonth/$targetYear")
                lineChartTrend.setNoDataTextColor(Color.parseColor("#FFC107"))
                lineChartTrend.invalidate()
                return@observe
            }

            // Mengelompokkan data berdasarkan tanggal (hari)
            // Mengambil mood yang paling sering muncul setiap hari
            val groupedByDate = filteredData
                .groupBy { dayFormat.format(it.first) }
                .mapValues { entry ->

                    // Hitung frekuensi setiap mood
                    val moodsCount = entry.value.groupBy { it.second }.mapValues { it.value.size }
                    val maxCount = moodsCount.values.maxOrNull() ?: 0

                    // Ambil mood dengan frekuensi terbanyak
                    val mostFrequent = moodsCount.filter { it.value == maxCount }.keys

                    // Jika frekuensi sama, ambil mood terakhir
                    val mood = if (mostFrequent.size > 1) {
                        entry.value.last().second
                    } else {
                        mostFrequent.first()
                    }

                    // Simpan tanggal dan mood terpilih
                    Pair(entry.value.first().first, mood)
                }

            // Urutkan data berdasarkan tanggal
            val sortedData = groupedByDate.entries
                .sortedBy { it.value.first }

            // Ambil daftar tanggal unik
            val dataDates = sortedData.map { it.value.first }.distinct()

            // Jika tanggal kosong, kosongkan chart
            if (dataDates.isEmpty()) {
                lineChartTrend.data = null
                lineChartTrend.invalidate()
                return@observe
            }

            // Ambil tanggal terakhir pada data
            val lastDate = dataDates.last()

            // Menghitung batas maksimal tanggal dalam bulan
            val calendar = Calendar.getInstance().apply { time = lastDate }
            val maxDayInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            calendar.set(Calendar.DAY_OF_MONTH, maxDayInMonth)
            val maxDateInMonth = calendar.time

            // Hitung tanggal H+5 dari tanggal terakhir
            calendar.time = lastDate
            calendar.add(Calendar.DAY_OF_MONTH, 5)
            val hPlusFiveDate = calendar.time

            // Tentukan batas akhir tanggal yang ditampilkan
            val maxDate = if (hPlusFiveDate > maxDateInMonth) maxDateInMonth else hPlusFiveDate

            // Membuat daftar tanggal sampai H+5
            val displayDates = dataDates.toMutableList()
            calendar.time = lastDate
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            while (calendar.time <= maxDate) {
                displayDates.add(calendar.time)
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            // Membuat label tanggal untuk sumbu X
            val calendarTmp = Calendar.getInstance()
            val dateLabels = displayDates.map {
                calendarTmp.time = it
                calendarTmp.get(Calendar.DAY_OF_MONTH).toString()
            }

            // Membuat entry data untuk LineChart
            val entries = sortedData.mapIndexed { index, entry ->
                Entry(index.toFloat(), entry.value.second.toFloat())
            }

            // Konfigurasi dataset LineChart
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

            // Set data ke LineChart
            val lineData = LineData(dataSet)
            lineChartTrend.data = lineData

            // Konfigurasi sumbu X
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

            // Konfigurasi tampilan umum LineChart
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

            // Konfigurasi sumbu Y (mood)
            lineChartTrend.axisLeft.apply {
                axisMinimum = 0.5f
                axisMaximum = 6.5f
                labelCount = 6

                // Menampilkan emoji sebagai label mood
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

            // Refresh LineChart
            lineChartTrend.invalidate()
        }
    }

    private fun observeMoodData() {

        // Mengamati seluruh data mood dari ViewModel
        mMoodEntryViewModel.readAllData.observe(viewLifecycleOwner) { users ->

            // Menyimpan data mood per hari dengan key tanggal (dd/MM/yyyy)
            val moodCountPerDay = mutableMapOf<String, Int>()

            // Formatter untuk parsing tanggal dan mengambil tahun
            val parseFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val yearFormat = SimpleDateFormat("yyyy", Locale.ENGLISH)

            // Tahun yang dipilih pada kalender
            val targetYear = selectedYearCalendar

            // Log untuk debugging tahun target
            Log.d("StatFragment", "Calendar Target Year: $targetYear")

            // Loop seluruh data user
            users.forEach { user ->

                // Parsing tanggal dari string
                val date = try {
                    parseFormat.parse(user.tanggal)
                } catch (e: Exception) {
                    // Log error jika parsing gagal
                    Log.e("StatFragment", "Failed to parse date: ${user.tanggal}, error: $e")
                    null
                }

                // Jika tanggal valid
                if (date != null) {

                    // Ambil tahun dari tanggal
                    val formattedYear = yearFormat.format(date)

                    // Filter data berdasarkan tahun yang dipilih
                    if (formattedYear == targetYear) {

                        // Format tanggal sebagai key (hari/bulan/tahun)
                        val key = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(date)

                        // Simpan nilai mood untuk tanggal tersebut
                        moodCountPerDay[key] = user.mood
                    }
                }
            }

            // Log data mood yang akan digunakan untuk kalender
            Log.d("StatFragment", "Calendar Mood Data: $moodCountPerDay")

            // Generate kalender mood berdasarkan data yang sudah difilter
            generateMoodCalendar(moodCountPerDay)
        }
    }

    private fun generateMoodCalendar(moodData: Map<String, Int>) {

        // Mengambil GridLayout kalender mood dari layout
        val gridLayout = requireView().findViewById<GridLayout>(R.id.moodCalendarGrid)

        // Menghapus seluruh view sebelumnya agar tidak duplikat
        gridLayout.removeAllViews()

        // Jumlah kolom: 1 kolom hari + 12 kolom bulan
        val columnCount = 13

        // Jumlah baris: 1 header + 31 hari + 1 footer
        val rowCount = 33

        // Set konfigurasi grid
        gridLayout.columnCount = columnCount
        gridLayout.rowCount = rowCount

        // Menunggu GridLayout selesai diukur sebelum menghitung ukuran sel
        gridLayout.post {

            // Lebar total GridLayout
            val availableWidth = gridLayout.width

            // Margin antar sel
            val marginBetweenCells = 1

            // Total margin horizontal
            val totalMargin = (columnCount - 1) * marginBetweenCells * 2

            // Menghitung ukuran sel secara dinamis
            val cellSize = maxOf((availableWidth - totalMargin) / columnCount, 20)

            // Menyesuaikan ukuran teks berdasarkan ukuran sel
            val textSize = when {
                cellSize < 25 -> 5f
                cellSize < 30 -> 6f
                cellSize < 40 -> 8f
                cellSize < 60 -> 10f
                else -> 12f
            }

            // Singkatan bulan untuk header dan footer kalender
            val monthAbbreviations = listOf(" ", "J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")

            // ================= HEADER =================
            // Membuat header bulan di baris pertama
            for (col in 0 until columnCount) {

                // Parameter layout untuk setiap header
                val params = GridLayout.LayoutParams().apply {
                    width = cellSize
                    height = cellSize
                    rowSpec = GridLayout.spec(0)
                    columnSpec = GridLayout.spec(col)
                    setMargins(marginBetweenCells, marginBetweenCells, marginBetweenCells, marginBetweenCells)
                }

                // TextView header bulan
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

            // ================= ISI KALENDER =================
            // Loop hari dari 1 sampai 31
            for (day in 1..31) {

                val row = day

                // Loop kolom bulan
                for (col in 0 until columnCount) {

                    // Parameter layout setiap sel
                    val params = GridLayout.LayoutParams().apply {
                        width = cellSize
                        height = cellSize
                        rowSpec = GridLayout.spec(row)
                        columnSpec = GridLayout.spec(col)
                        setMargins(marginBetweenCells, marginBetweenCells, marginBetweenCells, marginBetweenCells)
                    }

                    // Kolom pertama berisi angka hari
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

                        // Index bulan (1–12)
                        val monthIndex = col

                        // Key tanggal sesuai format dd/MM/yyyy
                        val key = "%02d/%02d/%s".format(day, monthIndex, selectedYearCalendar)

                        // Ambil warna mood berdasarkan data
                        val moodColor = moodData[key]?.let {
                            ContextCompat.getColor(requireContext(), MoodUtils.getMoodColor(it))
                        } ?: Color.TRANSPARENT

                        // View kotak mood
                        val view = View(requireContext()).apply {
                            background = GradientDrawable().apply {
                                setColor(moodColor)
                                setStroke(1, Color.LTGRAY)
                                cornerRadius =
                                    if (cellSize < 25) 1f
                                    else if (cellSize < 30) 2f
                                    else 4f
                            }
                            layoutParams = params
                        }

                        gridLayout.addView(view)
                    }
                }
            }

            // ================= FOOTER =================
            // Footer bulan di baris terakhir
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

}