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
import com.example.skripsta.adapter.FeelingSelectionAdapter
import com.example.skripsta.data.Feeling
import com.example.skripsta.data.FeelingViewModel
import com.example.skripsta.databinding.FragmentSelectFeelingBinding

class SelectFeelingFragment : Fragment() {

    private var _binding: FragmentSelectFeelingBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: FeelingSelectionAdapter
    private lateinit var feelingViewModel: FeelingViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private val feelings = mutableListOf<Feeling>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectFeelingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        feelingViewModel = ViewModelProvider(this).get(FeelingViewModel::class.java)

        // Load feelings from database
        feelingViewModel.allFeelings.observe(viewLifecycleOwner) { feelingsList ->
            feelings.clear()
            feelings.addAll(feelingsList)
            setupRecyclerView()
        }

        binding.applyButton.setOnClickListener {
            val selectedNames = adapter.getSelectedNames()
            if (selectedNames.size != 5) {
                Toast.makeText(context, "Please select exactly 5 feelings", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveSelectedFeelings(selectedNames)
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        val selectedNames = sharedPreferences.getStringSet("selected_feeling_names", emptySet()) ?: emptySet()
        adapter = FeelingSelectionAdapter(feelings, selectedNames) { selectedNames ->
            saveSelectedFeelings(selectedNames)
        }
        binding.recyclerViewSelection.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewSelection.adapter = adapter
    }

    private fun saveSelectedFeelings(selectedNames: List<String>) {
        sharedPreferences.edit()
            .putStringSet("selected_feeling_names", selectedNames.toSet())
            .apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}