package com.example.skripsta

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skripsta.adapter.ActivitySelectionAdapter
import com.example.skripsta.data.Activity
import com.example.skripsta.data.ActivityViewModel
import com.example.skripsta.databinding.FragmentSelectActivityBinding

class SelectActivityFragment : Fragment() {

    private var _binding: FragmentSelectActivityBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ActivitySelectionAdapter
    private lateinit var activityViewModel: ActivityViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private val activities = mutableListOf<Activity>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        activityViewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)

        // Load activities from database
        activityViewModel.allActivities.observe(viewLifecycleOwner) { activitiesList ->
            activities.clear()
            activities.addAll(activitiesList)
            setupRecyclerView()
        }

        binding.applyButton.setOnClickListener {
            val selectedNames = adapter.getSelectedNames()
            if (selectedNames.size != 5) {
                Toast.makeText(context, "Please select exactly 5 activities", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveSelectedActivities(selectedNames)
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        val selectedNames = sharedPreferences.getStringSet("selected_activity_names", emptySet()) ?: emptySet()
        adapter = ActivitySelectionAdapter(activities, selectedNames) { selectedNames ->
            saveSelectedActivities(selectedNames)
        }
        binding.recyclerViewSelection.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewSelection.adapter = adapter
    }

    private fun saveSelectedActivities(selectedNames: List<String>) {
        sharedPreferences.edit()
            .putStringSet("selected_activity_names", selectedNames.toSet())
            .apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}