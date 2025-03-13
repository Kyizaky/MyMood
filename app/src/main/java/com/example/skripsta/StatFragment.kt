package com.example.skripsta

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.adapter.ActivityRankingAdapter
import com.example.skripsta.adapter.MoodLegendAdapter
import com.example.skripsta.data.UserViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
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



}
