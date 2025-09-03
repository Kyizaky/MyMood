package com.example.skripsta

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.skripsta.data.Activity
import com.example.skripsta.data.ActivityViewModel
import com.example.skripsta.data.Icon
import com.example.skripsta.data.IconViewModel
import kotlinx.coroutines.runBlocking

class FragmentAddActivity : Fragment() {

    private lateinit var mActivityViewModel: ActivityViewModel
    private lateinit var mIconViewModel: IconViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private val args: FragmentAddActivityArgs by navArgs()
    private var selectedIcon: Icon? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_activity, container, false)

        mActivityViewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)
        mIconViewModel = ViewModelProvider(this).get(IconViewModel::class.java)
        sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        // Update title for edit mode
        val titleTextView = view.findViewById<TextView>(R.id.tv_add_activity_title)
        val editTextActivity = view.findViewById<EditText>(R.id.edit_text_activity)
        val spinnerIcon = view.findViewById<Spinner>(R.id.spinner_icon)
        val saveButton = view.findViewById<Button>(R.id.btn_save_activity)

        if (args.activityId != -1) {
            titleTextView.text = "Edit Activity"
            editTextActivity.setText(args.activityName)
            saveButton.text = "Update Activity"
        } else {
            titleTextView.text = "Add New Activity"
            saveButton.text = "Save Activity"
        }

        // Setup icon spinner with custom adapter
        mIconViewModel.allIcons.observe(viewLifecycleOwner) { icons ->
            Log.d("FragmentAddActivity", "Icons loaded: ${icons?.map { it.colorRes } ?: "null"}")
            if (icons.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "No icons available", Toast.LENGTH_SHORT).show()
                return@observe
            }
            val adapter = IconAdapter(requireContext(), icons)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerIcon.adapter = adapter
            if (args.activityId != -1) {
                // Find the icon matching the passed selectedIconRes (color) and iconRes (no-color)
                val position = icons.indexOfFirst { it.colorRes == args.selectedIconRes && it.noColorRes == args.iconRes }
                if (position != -1) {
                    spinnerIcon.setSelection(position)
                    selectedIcon = icons[position]
                } else {
                    spinnerIcon.setSelection(0)
                    selectedIcon = icons[0]
                    Log.w("FragmentAddActivity", "No matching icon found for edit, defaulting to first icon")
                }
            } else {
                spinnerIcon.setSelection(0)
                selectedIcon = icons[0]
            }
            spinnerIcon.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                    selectedIcon = icons[position]
                    Log.d("FragmentAddActivity", "Selected icon: ${selectedIcon?.colorRes}")
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
                    selectedIcon = icons[0]
                    Log.d("FragmentAddActivity", "No icon selected, defaulting to: ${selectedIcon?.colorRes}")
                }
            })
        }

        // Back button navigation
        view.findViewById<ImageView>(R.id.ic_back).setOnClickListener {
            findNavController().popBackStack()
        }

        // Save/Update button logic
        saveButton.setOnClickListener {
            saveOrUpdateActivity(view)
        }

        return view
    }

    private fun saveOrUpdateActivity(view: View) {
        val activityName = view.findViewById<EditText>(R.id.edit_text_activity).text.toString().trim()

        if (activityName.isBlank()) {
            Toast.makeText(requireContext(), "Please enter an activity name", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedIcon == null) {
            Toast.makeText(requireContext(), "Please select an icon", Toast.LENGTH_SHORT).show()
            return
        }

        if (args.activityId == -1) {
            val activityCount = runBlocking { mActivityViewModel.getActivityCount() }
            if (activityCount >= 10) {
                Toast.makeText(requireContext(), "Cannot add more than 10 activities", Toast.LENGTH_SHORT).show()
                return
            }
        }

        if (args.activityId != -1 && args.activityName != activityName) {
            val selectedNames = sharedPreferences.getStringSet("selected_activity_names", emptySet())?.toMutableSet() ?: mutableSetOf()
            if (args.activityName in selectedNames) {
                selectedNames.remove(args.activityName)
                selectedNames.add(activityName)
                sharedPreferences.edit().putStringSet("selected_activity_names", selectedNames).apply()
            }
        }

        val activity = Activity(
            id = if (args.activityId != -1) args.activityId else 0,
            name = activityName,
            iconRes = selectedIcon!!.colorRes,
            selectedIconRes = selectedIcon!!.noColorRes
        )
        if (args.activityId != -1) {
            mActivityViewModel.updateActivity(activity)
            Toast.makeText(requireContext(), "Activity updated successfully!", Toast.LENGTH_SHORT).show()
        } else {
            mActivityViewModel.addActivity(activity)
            Toast.makeText(requireContext(), "Activity added successfully!", Toast.LENGTH_SHORT).show()
        }
        findNavController().popBackStack()
    }

    private class IconAdapter(context: Context, private val icons: List<Icon>) : ArrayAdapter<Icon>(context, android.R.layout.simple_spinner_item, icons) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent) as TextView
            val icon = icons[position]
            view.text = ""
            view.setCompoundDrawablesWithIntrinsicBounds(icon.colorRes, 0, 0, 0)
            view.compoundDrawablePadding = 8
            return view
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getDropDownView(position, convertView, parent) as TextView
            val icon = icons[position]
            view.text = "Icon ${position + 1}"
            view.setCompoundDrawablesWithIntrinsicBounds(icon.colorRes, 0, 0, 0)
            view.compoundDrawablePadding = 8
            return view
        }
    }
}