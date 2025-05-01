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
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
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
    private var selectedMonthActivity: String = ""
    private var selectedYearActivity: String = ""
    private var selectedMonthFeeling: String = ""
    private var selectedYearFeeling: String = ""
    private var selectedMonthPie: String = ""
    private lateinit var progressBar: ProgressBar
    private lateinit var containerStat: LinearLayout
    private lateinit var containerLegend: CardView
    private var tvSelectedDate: String = ""
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
        btnMonthActivity = view.findViewById(R.id.btn_month)
        btnYearActivity = view.findViewById(R.id.btn_year)

        recyclerViewFeelingRanking = view.findViewById(R.id.recycler_view_feeling_ranking)
        recyclerViewFeelingRanking.layoutManager = LinearLayoutManager(requireContext())
        feelingRankingAdapter = FeelingRankingAdapter(emptyList())
        recyclerViewFeelingRanking.adapter = feelingRankingAdapter
        btnMonthFeeling = view.findViewById(R.id.btn_month_feeling)
        btnYearFeeling = view.findViewById(R.id.btn_year_feeling)

        progressBar = view.findViewById(R.id.progress_bar)
        containerStat = view.findViewById(R.id.container_stat)
        containerLegend = view.findViewById(R.id.container_legend)
        pieChart = view.findViewById(R.id.moodPieChart)
        legendRecyclerView = view.findViewById(R.id.recycler_view_mood_legend)
        legendRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        monthSpinnerPie = view.findViewById(R.id.spinner_month_pie)

        showLoading(true) // <- Tambahkan ini
        val gridLayout = view.findViewById<GridLayout>(R.id.moodCalendarGrid)

        gridLayout.rowCount = 33
        gridLayout.columnCount = 13

        setupRankingButtons()
        observeDataRanking()
        observeDataFeelingRanking()

        setupSpinnerPie()


        val storageFormat = SimpleDateFormat("MM/dd/yyyy", Locale("id"))

        val today = Calendar.getInstance().time
        selectedDate = storageFormat.format(today)

        tvSelectedDate = "$selectedDate"


        return view
    }

    //Progress bar
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
        showLoading(true) // <- Tambahkan ini
        observeMoodData()
    }


    //Setup pie chart
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
        // Urutkan entri berdasarkan moodInt untuk memastikan urutan konsisten
        val entries = moodCount.entries
            .sortedBy { it.key } // Urutkan berdasarkan moodInt (1 sampai 6)
            .mapNotNull { (moodInt, count) ->
                if (count > 0) PieEntry(count.toFloat(), moodInt.toString()) else null
            }

        if (entries.isEmpty()) {
            pieChart.data = null
            pieChart.invalidate()
            return
        }

        // Definisikan warna untuk setiap mood (indeks 0 untuk mood 1, indeks 1 untuk mood 2, dst.)
        val colorMap = mapOf(
            1 to Color.parseColor("#FF6242"), // Marah (merah)
            2 to Color.parseColor("#6DD627"), // Jijik (ungu)
            3 to Color.parseColor("#CB88FF"), // Takut (biru)
            4 to Color.parseColor("#87C1FF"), // Sedih (biru muda)
            5 to Color.parseColor("#FFE500"), // Bahagia (hijau)
            6 to Color.parseColor("#FFEAC7")  // Netral (abu)
        )

        // Buat daftar warna berdasarkan urutan entri
        val colors = entries.map { entry ->
            val moodInt = entry.label.toInt()
            colorMap[moodInt] ?: Color.GRAY // Gunakan warna default jika moodInt tidak ada
        }

        val dataSet = PieDataSet(entries, "").apply {
            setColors(colors) // Atur warna untuk setiap segmen
            sliceSpace = 3f
            setDrawValues(false) // Hilangkan angka dalam chart
        }

        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.description.isEnabled = false // Hilangkan label deskripsi
        pieChart.legend.isEnabled = false // Hilangkan legend jika tidak diperlukan
        pieChart.setDrawEntryLabels(false)
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


    //ranking
    private fun setupRankingButtons() {
        // Daftar bulan
        val months = listOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )

        // Daftar tahun (misalnya, dari 2020 sampai 2025)
        val years = (2020..2025).map { it.toString() }

        // Atur teks default ke bulan dan tahun saat ini untuk Ranking Activity
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) // 0-11
        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
        selectedMonthActivity = months[currentMonth]
        selectedYearActivity = currentYear
        btnMonthActivity.text = selectedMonthActivity
        btnYearActivity.text = selectedYearActivity

        // Atur teks default ke bulan dan tahun saat ini untuk Ranking Feeling
        selectedMonthFeeling = months[currentMonth]
        selectedYearFeeling = currentYear
        btnMonthFeeling.text = selectedMonthFeeling
        btnYearFeeling.text = selectedYearFeeling

        // Listener untuk Button bulan (Ranking Activity)
        btnMonthActivity.setOnClickListener {
            showPickerDialog(isMonth = true, months, years, isActivity = true)
        }

        // Listener untuk Button tahun (Ranking Activity)
        btnYearActivity.setOnClickListener {
            showPickerDialog(isMonth = false, months, years, isActivity = true)
        }

        // Listener untuk Button bulan (Ranking Feeling)
        btnMonthFeeling.setOnClickListener {
            showPickerDialog(isMonth = true, months, years, isActivity = false)
        }

        // Listener untuk Button tahun (Ranking Feeling)
        btnYearFeeling.setOnClickListener {
            showPickerDialog(isMonth = false, months, years, isActivity = false)
        }
    }

    private fun showPickerDialog(isMonth: Boolean, months: List<String>, years: List<String>, isActivity: Boolean) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_picker, null)
        dialog.setContentView(view)

        val numberPicker = view.findViewById<NumberPicker>(R.id.number_picker)
        val btnCancel = view.findViewById<ImageButton>(R.id.btn_cancel)
        val btnConfirm = view.findViewById<Button>(R.id.btn_confirm)

        // Pastikan looping dinonaktifkan
        numberPicker.wrapSelectorWheel = false

        if (isMonth) {
            // Setup NumberPicker untuk bulan
            numberPicker.minValue = 0
            numberPicker.maxValue = months.size - 1
            numberPicker.displayedValues = months.toTypedArray()
            numberPicker.value = months.indexOf(if (isActivity) selectedMonthActivity else selectedMonthFeeling)
        } else {
            // Setup NumberPicker untuk tahun
            numberPicker.minValue = 0
            numberPicker.maxValue = years.size - 1
            numberPicker.displayedValues = years.toTypedArray()
            numberPicker.value = years.indexOf(if (isActivity) selectedYearActivity else selectedYearFeeling)
        }

        // Tombol Cancel
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Tombol Confirm
        btnConfirm.setOnClickListener {
            if (isActivity) {
                if (isMonth) {
                    selectedMonthActivity = months[numberPicker.value]
                    btnMonthActivity.text = selectedMonthActivity
                    observeDataRanking()
                } else {
                    selectedYearActivity = years[numberPicker.value]
                    btnYearActivity.text = selectedYearActivity
                    observeDataRanking()
                }
            } else {
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
                    val formattedDate = dateFormat.format(date) // Format: "April 2025"
                    val dateMonthYear = formattedDate.split(" ") // Pisah menjadi ["April", "2025"]
                    val dateMonth = dateMonthYear[0]
                    val dateYear = dateMonthYear[1]

                    if (dateMonth == selectedMonthActivity && dateYear == selectedYearActivity) {
                        println("Matching data: ${user.tanggal}, activities: ${user.activities}, icon: ${user.activityIcon}")
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
                    val formattedDate = dateFormat.format(date) // Format: "April 2025"
                    val dateMonthYear = formattedDate.split(" ") // Pisah menjadi ["April", "2025"]
                    val dateMonth = dateMonthYear[0]
                    val dateYear = dateMonthYear[1]

                    if (dateMonth == selectedMonthFeeling && dateYear == selectedYearFeeling) {
                        println("Matching data: ${user.tanggal}, feeling: ${user.perasaan}")
                        val feeling = user.perasaan.trim()

                        val currentData = feelingCount[feeling]
                        if (currentData != null) {
                            feelingCount[feeling] = Pair(currentData.first + 1, currentData.second)
                        } else {
                            feelingCount[feeling] = Pair(1, 0) // Ikon tidak digunakan, jadi gunakan 0
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


    //Grid
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
            1 -> Color.parseColor("#F44336") // Marah (merah)
            2 -> Color.parseColor("#9C27B0") // Jijik (ungu)
            3 -> Color.parseColor("#3F51B5") // Takut (biru)
            4 -> Color.parseColor("#03A9F4") // Sedih (biru muda)
            5 -> Color.parseColor("#4CAF50") // Bahagia (hijau)
            6 -> Color.parseColor("#9E9E9E") // Netral (abu)
            else -> Color.TRANSPARENT
        }
    }

}
