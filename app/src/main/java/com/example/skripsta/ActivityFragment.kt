package com.example.skripsta

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.adapter.ActivityListAdapter
import com.example.skripsta.data.Activity
import com.example.skripsta.data.ActivityViewModel
import kotlinx.coroutines.runBlocking

class ActivityFragment : Fragment() {

    private lateinit var mActivityViewModel: ActivityViewModel
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_activity, container, false)

        mActivityViewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)
        sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        // Setup RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_activities)
        val adapter = ActivityListAdapter(
            onEditClick = { activity ->
                val action = ActivityFragmentDirections.actionActivityFragmentToFragmentAddActivity(
                    activityId = activity.id,
                    activityName = activity.name,
                    iconRes = activity.iconRes,
                    selectedIconRes = activity.selectedIconRes
                )
                findNavController().navigate(action)
            },
            onDeleteClick = { activity ->
                val selectedNames = sharedPreferences.getStringSet("selected_activity_names", emptySet()) ?: emptySet()
                val activityCount = runBlocking { mActivityViewModel.getActivityCount() }
                when {
                    activityCount <= 5 -> {
                        Toast.makeText(requireContext(), "Cannot delete activity, minimum 5 activities required", Toast.LENGTH_SHORT).show()
                    }
                    activity.name in selectedNames -> {
                        Toast.makeText(requireContext(), "Cannot delete activity '${activity.name}' as it is currently selected", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        mActivityViewModel.deleteActivity(activity)
                        Toast.makeText(requireContext(), "Activity '${activity.name}' deleted", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Observe activities and log for debugging
        mActivityViewModel.allActivities.observe(viewLifecycleOwner) { activities ->
            Log.d("ActivityFragment", "Received activities: ${activities?.map { it.name } ?: "null"}")
            adapter.submitList(activities ?: emptyList())
        }

        // Back button
        view.findViewById<ImageView>(R.id.ic_back).setOnClickListener {
            findNavController().popBackStack()
        }

        // Add button
        view.findViewById<Button>(R.id.btn_add_activity).setOnClickListener {
            try {
                findNavController().navigate(R.id.action_activityFragment_to_fragmentAddActivity)
            } catch (e: Exception) {
                Log.e("ActivityFragment", "Navigation error: ${e.message}", e)
                Toast.makeText(requireContext(), "Error navigating to Add Activity", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}