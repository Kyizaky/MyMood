package com.example.skripsta

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.adapter.ActivityAdapter
import com.example.skripsta.adapter.FeelingAdapter
import com.example.skripsta.adapter.getDisplayName
import com.example.skripsta.data.Item
import com.example.skripsta.data.MoodEntry
import com.example.skripsta.data.MoodEntryViewModel
import com.example.skripsta.data.ActivityViewModel
import com.example.skripsta.data.FeelingViewModel
import com.example.skripsta.databinding.FragmentEditMoodBinding
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditMoodFragment : Fragment() {

    private val args by navArgs<EditMoodFragmentArgs>()
    private lateinit var mMoodEntryViewModel: MoodEntryViewModel
    private lateinit var mActivityViewModel: ActivityViewModel
    private lateinit var mFeelingViewModel: FeelingViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: FragmentEditMoodBinding
    private var selectedFeelingText: String? = null
    private var selectedActivityItem: Item? = null
    private var selectedMoodButton: ImageButton? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mMoodEntryViewModel = ViewModelProvider(this).get(MoodEntryViewModel::class.java)
        mActivityViewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)
        mFeelingViewModel = ViewModelProvider(this).get(FeelingViewModel::class.java)
        sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.GONE

        binding = FragmentEditMoodBinding.inflate(inflater, container, false)

        val moodEntry = args.moodEntry
        binding.btnCal.setText(moodEntry.tanggal)
        binding.btnClock.setText(moodEntry.jam)
        binding.tvJurnaling.setText(moodEntry.jurnal)
        binding.etJornal.setText(moodEntry.judul)
        selectedFeelingText = moodEntry.perasaan
        selectedActivityItem = Item(
            drawableId = moodEntry.activityIcon,
            selectedDrawableId = moodEntry.activityIcon, // Use same icon for consistency, will be updated by adapter
            text = moodEntry.activities,
            isSelected = true
        )

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH)
        val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)

        // Pre-select mood button
        setupMoodButtons(binding.root, moodEntry.mood)

        // Event untuk menampilkan TimePickerDialog
        binding.btnClock.setOnClickListener {
            val timePickerDialog = TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    val selectedTime = Calendar.getInstance()
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    selectedTime.set(Calendar.MINUTE, minute)
                    binding.btnClock.setText(timeFormat.format(selectedTime.time))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePickerDialog.show()
        }

        // Event untuk menampilkan DatePickerDialog
        binding.btnCal.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, dayOfMonth)
                    binding.btnCal.setText(dateFormat.format(selectedDate.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        binding.btnUpdate.setOnClickListener {
            updateDataToDatabase(binding.root)
        }

        binding.editActivitiesButton.setOnClickListener {
            findNavController().navigate(R.id.action_editMoodFragment_to_selectActivityFragment)
        }

        binding.editFeelingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_editMoodFragment_to_selectFeelingFragment)
        }

        setupRecyclerView(binding.root)
        setupFeelingRecyclerView(binding.root)

        return binding.root
    }

    private fun setupMoodButtons(view: View, initialMood: Int?) {
        val moodButtons = listOf(
            R.id.mood1 to 1, R.id.mood2 to 2, R.id.mood3 to 3,
            R.id.mood4 to 4, R.id.mood5 to 5
        )
        val moodDrawables = mapOf(
            R.id.mood1 to Pair(R.drawable.para1_nocolor, R.drawable.para1),
            R.id.mood2 to Pair(R.drawable.para2_nocolor, R.drawable.para2),
            R.id.mood3 to Pair(R.drawable.para3_nocolor, R.drawable.para3),
            R.id.mood4 to Pair(R.drawable.para4_nocolor, R.drawable.para4),
            R.id.mood5 to Pair(R.drawable.para5_nocolor, R.drawable.para5)
        )

        moodButtons.forEach { (id, moodValue) ->
            val button = view.findViewById<ImageButton>(id)
            if (moodValue == initialMood) {
                button.isSelected = true
                selectedMoodButton = button
                val selectedDrawable = moodDrawables[id]?.second
                if (selectedDrawable != null) button.setImageResource(selectedDrawable)
            } else {
                button.isSelected = false
                val defaultDrawable = moodDrawables[id]?.first
                if (defaultDrawable != null) button.setImageResource(defaultDrawable)
            }
            button.setOnClickListener {
                updateMoodSelection(it as ImageButton, moodButtons.map { view.findViewById<ImageButton>(it.first) })
            }
        }
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_activities)
        recyclerView.layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
        }

        mActivityViewModel.allActivities.observe(viewLifecycleOwner, Observer { activities ->
            Log.d("EditMoodFragment", "Observed activities: ${activities.map { it.name }}")
            val selectedNames = sharedPreferences.getStringSet("selected_activity_names", emptySet()) ?: emptySet()
            val displayedActivities = activities.filter { it.name in selectedNames }.map {
                Item(
                    drawableId = it.iconRes,
                    selectedDrawableId = it.selectedIconRes,
                    text = it.name,
                    isSelected = it.name == args.moodEntry.activities
                )
            }
            Log.d("EditMoodFragment", "Displayed activities: ${displayedActivities.map { it.text }}")
            recyclerView.adapter = ActivityAdapter(displayedActivities, args.moodEntry.activities) { selectedItem ->
                selectedActivityItem = selectedItem
            }
        })
    }

    private fun setupFeelingRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_feelings)
        recyclerView.layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
        }

        mFeelingViewModel.allFeelings.observe(viewLifecycleOwner, Observer { feelings ->
            val selectedNames = sharedPreferences.getStringSet("selected_feeling_names", emptySet()) ?: emptySet()
            val displayedFeelings = feelings.filter { it.name in selectedNames }.map { it.name }
            Log.d("EditMoodFragment", "Displayed feelings: $displayedFeelings, Initial feeling: ${args.moodEntry.perasaan}")
            recyclerView.adapter = FeelingAdapter(displayedFeelings, args.moodEntry.perasaan) { selectedFeeling ->
                selectedFeelingText = selectedFeeling
            }
        })
    }

    private fun getSelectedMoodType(view: View): Int? {
        val moodButtons = listOf(
            R.id.mood1 to 1, R.id.mood2 to 2, R.id.mood3 to 3,
            R.id.mood4 to 4, R.id.mood5 to 5
        )
        return moodButtons.firstOrNull { view.findViewById<ImageButton>(it.first).isSelected }?.second
    }

    private fun updateMoodSelection(button: ImageButton, allButtons: List<ImageButton>) {
        val moodDrawables = mapOf(
            R.id.mood1 to Pair(R.drawable.para1_nocolor, R.drawable.para1),
            R.id.mood2 to Pair(R.drawable.para2_nocolor, R.drawable.para2),
            R.id.mood3 to Pair(R.drawable.para3_nocolor, R.drawable.para3),
            R.id.mood4 to Pair(R.drawable.para4_nocolor, R.drawable.para4),
            R.id.mood5 to Pair(R.drawable.para5_nocolor, R.drawable.para5)
        )

        if (selectedMoodButton == button) {
            button.isSelected = false
            val defaultDrawable = moodDrawables[button.id]?.first
            if (defaultDrawable != null) button.setImageResource(defaultDrawable)
            selectedMoodButton = null
            return
        }

        allButtons.forEach {
            it.isSelected = false
            val defaultDrawable = moodDrawables[it.id]?.first
            if (defaultDrawable != null) it.setImageResource(defaultDrawable)
        }

        button.isSelected = true
        val selectedDrawable = moodDrawables[button.id]?.second
        if (selectedDrawable != null) button.setImageResource(selectedDrawable)
        selectedMoodButton = button
    }

    private fun updateDataToDatabase(view: View) {
        val journalContent = binding.tvJurnaling.text.toString().ifBlank { "No story today" }
        val titleJournal = binding.etJornal.text.toString().ifBlank { "Today" }
        val moodType = getSelectedMoodType(view)
        val selectedFeeling = selectedFeelingText
        val selectedActivity = selectedActivityItem
        val selectedDate = binding.btnCal.text.toString()
        val selectedTime = binding.btnClock.text.toString()

        if (moodType == null || selectedFeeling == null || selectedActivity == null || selectedDate.isBlank() || selectedTime.isBlank()) {
            Toast.makeText(requireContext(), "Lengkapi semua data sebelum menyimpan!", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedMood = MoodEntry(
            id = args.moodEntry.id,
            mood = moodType,
            activities = selectedActivity.getDisplayName(),
            activityIcon = if (selectedActivity.isSelected) selectedActivity.selectedDrawableId else selectedActivity.drawableId,
            perasaan = selectedFeeling,
            judul = titleJournal,
            jurnal = journalContent,
            tanggal = selectedDate,
            jam = selectedTime
        )

        mMoodEntryViewModel.updateMoodEntry(updatedMood)
        Toast.makeText(requireContext(), "Berhasil", Toast.LENGTH_LONG).show()
        findNavController().popBackStack()
    }
}