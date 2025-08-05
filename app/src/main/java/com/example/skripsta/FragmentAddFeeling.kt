package com.example.skripsta

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.skripsta.data.Feeling
import com.example.skripsta.data.FeelingViewModel
import kotlinx.coroutines.runBlocking

class FragmentAddFeeling : Fragment() {

    private lateinit var mFeelingViewModel: FeelingViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private val args: FragmentAddFeelingArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_feeling, container, false)

        mFeelingViewModel = ViewModelProvider(this).get(FeelingViewModel::class.java)
        sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        // Update title for edit mode
        val titleTextView = view.findViewById<TextView>(R.id.tv_add_feeling_title)
        val editTextFeeling = view.findViewById<EditText>(R.id.edit_text_feeling)
        val saveButton = view.findViewById<Button>(R.id.btn_save_feeling)

        if (args.feelingId != -1) {
            titleTextView.text = "Edit Feeling"
            editTextFeeling.setText(args.feelingName)
            saveButton.text = "Update Feeling"
        }

        // Back button navigation
        view.findViewById<ImageView>(R.id.ic_back).setOnClickListener {
            findNavController().popBackStack()
        }

        // Save/Update button logic
        saveButton.setOnClickListener {
            saveOrUpdateFeeling(view)
        }

        return view
    }

    private fun saveOrUpdateFeeling(view: View) {
        val feelingName = view.findViewById<EditText>(R.id.edit_text_feeling).text.toString().trim()

        if (feelingName.isBlank()) {
            Toast.makeText(requireContext(), "Please enter a feeling name", Toast.LENGTH_SHORT).show()
            return
        }

        // Check feeling count for add operation
        if (args.feelingId == -1) {
            val feelingCount = runBlocking { mFeelingViewModel.getFeelingCount() }
            if (feelingCount >= 10) {
                Toast.makeText(requireContext(), "Cannot add more than 10 feelings", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Update SharedPreferences if editing and name changes
        if (args.feelingId != -1 && args.feelingName != feelingName) {
            val selectedNames = sharedPreferences.getStringSet("selected_feeling_names", emptySet())?.toMutableSet() ?: mutableSetOf()
            if (args.feelingName in selectedNames) {
                selectedNames.remove(args.feelingName)
                selectedNames.add(feelingName)
                sharedPreferences.edit().putStringSet("selected_feeling_names", selectedNames).apply()
            }
        }

        val feeling = Feeling(id = if (args.feelingId != -1) args.feelingId else 0, name = feelingName)
        if (args.feelingId != -1) {
            mFeelingViewModel.updateFeeling(feeling)
            Toast.makeText(requireContext(), "Feeling updated successfully!", Toast.LENGTH_SHORT).show()
        } else {
            mFeelingViewModel.addFeeling(feeling)
            Toast.makeText(requireContext(), "Feeling added successfully!", Toast.LENGTH_SHORT).show()
        }
        findNavController().popBackStack()
    }
}