package com.example.skripsta

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.adapter.FeelingListAdapter
import com.example.skripsta.data.Feeling
import com.example.skripsta.data.FeelingViewModel
import kotlinx.coroutines.runBlocking

class FeelingFragment : Fragment() {

    private lateinit var mFeelingViewModel: FeelingViewModel
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_feeling, container, false)

        mFeelingViewModel = ViewModelProvider(this).get(FeelingViewModel::class.java)
        sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        // Setup RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_feelings)
        val adapter = FeelingListAdapter(
            onEditClick = { feeling ->
                val action = FeelingFragmentDirections.actionFeelingFragmentToFragmentAddFeeling(
                    feelingId = feeling.id,
                    feelingName = feeling.name
                )
                findNavController().navigate(action)
            },
            onDeleteClick = { feeling ->
                val selectedNames = sharedPreferences.getStringSet("selected_feeling_names", emptySet()) ?: emptySet()
                val feelingCount = runBlocking { mFeelingViewModel.getFeelingCount() }
                when {
                    feelingCount <= 5 -> {
                        Toast.makeText(requireContext(), "Cannot delete feeling, minimum 5 feelings required", Toast.LENGTH_SHORT).show()
                    }
                    feeling.name in selectedNames -> {
                        Toast.makeText(requireContext(), "Cannot delete feeling '${feeling.name}' as it is currently selected", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        mFeelingViewModel.deleteFeeling(feeling)
                        Toast.makeText(requireContext(), "Feeling '${feeling.name}' deleted", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        // Observe feelings
        mFeelingViewModel.allFeelings.observe(viewLifecycleOwner) { feelings ->
            adapter.submitList(feelings)
        }

        // Back button
        view.findViewById<ImageView>(R.id.ic_back).setOnClickListener {
            findNavController().popBackStack()
        }

        // Add button
        view.findViewById<Button>(R.id.btn_add_feeling).setOnClickListener {
            findNavController().navigate(R.id.action_feelingFragment_to_fragmentAddFeeling)
        }

        return view
    }
}