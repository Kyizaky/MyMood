package com.example.skripsta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.adapter.ActivityRankingAdapter
import com.example.skripsta.data.UserViewModel

class StatFragment : Fragment() {

    private lateinit var mUserViewModel: UserViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var activityRankingAdapter: ActivityRankingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stat, container, false)

        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        recyclerView = view.findViewById(R.id.recycler_view_ranking)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        activityRankingAdapter = ActivityRankingAdapter(emptyList()) // Awalnya kosong
        recyclerView.adapter = activityRankingAdapter

        observeData()

        return view
    }

    private fun observeData() {
        mUserViewModel.readAllData.observe(viewLifecycleOwner, { users ->
            val activityCount = mutableMapOf<String, Pair<Int, Int>>() // Map nama aktivitas ke (jumlah, ikon)

            users.forEach { user ->
                val activities = user.activities.split(", ") // Asumsikan aktivitas dipisahkan koma
                activities.forEach { activity ->
                    val iconResId = user.activityIcon // Ambil ikon dari database

                    // Jika aktivitas sudah ada di map, tambahkan jumlahnya
                    val currentData = activityCount[activity]
                    if (currentData != null) {
                        activityCount[activity] = Pair(currentData.first + 1, currentData.second)
                    } else {
                        activityCount[activity] = Pair(1, iconResId)
                    }
                }
            }

            val sortedActivities = activityCount.toList()
                .sortedByDescending { it.second.first } // Urutkan berdasarkan jumlah aktivitas
                .take(3) // Ambil 3 aktivitas teratas
                .map { Triple(it.first, it.second.first, it.second.second) } // Konversi ke Triple

            activityRankingAdapter.updateData(sortedActivities)
        })
    }

}
