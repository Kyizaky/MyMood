package com.example.skripsta

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.adapter.ActivityAdapter
import com.example.skripsta.adapter.FeelingAdapter
import com.example.skripsta.adapter.getDisplayName
import com.example.skripsta.data.Item
import com.example.skripsta.data.ActivityViewModel
import com.example.skripsta.data.FeelingViewModel
import com.example.skripsta.data.MoodEntry
import com.example.skripsta.data.MoodEntryViewModel
import com.example.skripsta.data.UserViewModel
import com.example.skripsta.utils.MoodUtils
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class TambahFragment : Fragment() {

    private lateinit var mMoodEntryViewModel: MoodEntryViewModel
    private lateinit var mUserViewModel: UserViewModel
    private lateinit var mFeelingViewModel: FeelingViewModel
    private lateinit var mActivityViewModel: ActivityViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private var selectedFeelingText: String? = null
    private var selectedActivityItem: Item? = null
    private var selectedMoodButton: ImageButton? = null
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tambah, container, false)

        mMoodEntryViewModel = ViewModelProvider(this).get(MoodEntryViewModel::class.java)
        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        mFeelingViewModel = ViewModelProvider(this).get(FeelingViewModel::class.java)
        mActivityViewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)
        sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val currentUserId = sharedPreferences.getInt("current_user_id", 1)
        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.GONE

        view.findViewById<ImageView>(R.id.ic_back).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<Button>(R.id.btn_save).setOnClickListener {
            insertDataToDatabase(view)
            mUserViewModel.recordMoodEntry(currentUserId)
        }

        val btnCal: EditText = view.findViewById(R.id.btn_cal)
        val btnClock: EditText = view.findViewById(R.id.btn_clock)
        val calendar = Calendar.getInstance()
        val today = LocalDate.now()
        val now = LocalTime.now()

        btnCal.setText(MoodUtils.formatCal(today.format(dateFormatter)))
        btnClock.setText(now.format(timeFormatter))

        btnClock.setOnClickListener {
            val timePickerDialog = TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    val selectedTime = LocalTime.of(hourOfDay, minute)
                    btnClock.setText(selectedTime.format(timeFormatter))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePickerDialog.show()
        }

        btnCal.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                    btnCal.setText(MoodUtils.formatCal(selectedDate.format(dateFormatter)))
                },
                today.year,
                today.monthValue - 1,
                today.dayOfMonth
            )
            datePickerDialog.show()
        }

        setupMoodButtons(view)
        setupRecyclerView(view)
        setupFeelingRecyclerView(view)

        view.findViewById<ImageView>(R.id.edit_activities_button).setOnClickListener {
            findNavController().navigate(R.id.action_tambahFragment_to_selectActivityFragment)
        }

        view.findViewById<ImageView>(R.id.edit_feelings_button).setOnClickListener {
            findNavController().navigate(R.id.action_tambahFragment_to_selectFeelingFragment)
        }

        return view
    }

    private fun setupMoodButtons(view: View) {
        val moodButtons = listOf(
            R.id.mood1 to 1, R.id.mood2 to 2, R.id.mood3 to 3,
            R.id.mood4 to 4, R.id.mood5 to 5
        )
        moodButtons.forEach { (id, _) ->
            view.findViewById<ImageButton>(id).setOnClickListener { button ->
                updateMoodSelection(button as ImageButton, moodButtons.map { view.findViewById<ImageButton>(it.first) })
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
            var selectedNames = sharedPreferences.getStringSet("selected_activity_names", emptySet()) ?: emptySet()

            if (selectedNames.isEmpty() && activities.isNotEmpty()) {
                selectedNames = activities.map { it.name }.toSet()
                sharedPreferences.edit().putStringSet("selected_activity_names", selectedNames).apply()
            }

            val displayedActivities = activities.filter { it.name in selectedNames }.map {
                Item(it.selectedIconRes, it.iconRes, it.name)
            }
            recyclerView.adapter = ActivityAdapter(displayedActivities) { selectedItem ->
                selectedActivityItem = selectedItem
            }
        })
    }

    private fun setupFeelingRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_feelings)
        recyclerView.layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }

        mFeelingViewModel.allFeelings.observe(viewLifecycleOwner, Observer { feelings ->
            var selectedNames = sharedPreferences.getStringSet("selected_feeling_names", emptySet()) ?: emptySet()

            if (selectedNames.isEmpty() && feelings.isNotEmpty()) {
                selectedNames = feelings.map { it.name }.toSet()
                sharedPreferences.edit().putStringSet("selected_feeling_names", selectedNames).apply()
            }

            val displayedFeelings = feelings.filter { it.name in selectedNames }.map { it.name }
            recyclerView.adapter = FeelingAdapter(displayedFeelings) { selectedFeeling ->
                selectedFeelingText = selectedFeeling
            }
        })
    }

    private fun insertDataToDatabase(view: View) {
        val journalContent = view.findViewById<EditText>(R.id.title_jurnal_save)?.text.toString().ifBlank { "No story today" }
        val titleJournal = view.findViewById<EditText>(R.id.isi_jurnal_save)?.text.toString().ifBlank { "Today" }
        val moodType = getSelectedMoodType(view)
        val selectedFeeling = selectedFeelingText
        val selectedActivity = selectedActivityItem
        val selectedDate = view.findViewById<EditText>(R.id.btn_cal)?.text.toString()
        val selectedTime = view.findViewById<EditText>(R.id.btn_clock)?.text.toString()

        val storedDate = try {
            val date = LocalDate.parse(selectedDate, DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH))
            date.format(dateFormatter)
        } catch (e: Exception) {
            selectedDate // Fallback to input if conversion fails
        }

        if (moodType == null || selectedFeeling == null || selectedActivity == null || selectedDate.isBlank() || selectedTime.isBlank()) {
            Toast.makeText(requireContext(), "Complete all data before saving!", Toast.LENGTH_SHORT).show()
            return
        }

        val moodEntry = MoodEntry(
            id = 0,
            mood = moodType,
            activities = selectedActivity.getDisplayName(),
            activityIcon = selectedActivity.selectedDrawableId,
            perasaan = selectedFeeling,
            judul = titleJournal,
            jurnal = journalContent,
            tanggal = storedDate,
            jam = selectedTime
        )

        mMoodEntryViewModel.addMoodEntry(moodEntry)
        val action = TambahFragmentDirections.actionTambahFragmentToValidationFragment(moodType)
        findNavController().navigate(action)
    }

    private fun getSelectedMoodType(view: View): Int? {
        val moodButtons = listOf(
            R.id.mood1 to 1, R.id.mood2 to 2, R.id.mood3 to 3,
            R.id.mood4 to 4,
            R.id.mood5 to 5
        )
        return moodButtons.firstOrNull { view.findViewById<ImageButton>(it.first)?.isSelected == true }?.second
    }

    private fun updateMoodSelection(button: ImageButton, allButtons: List<ImageButton>): Boolean {
        val moodDrawables = mapOf(
            R.id.mood1 to Pair(R.drawable.para1_nocolor, R.drawable.para1),
            R.id.mood2 to Pair(R.drawable.para2_nocolor, R.drawable.para2),
            R.id.mood3 to Pair(R.drawable.para3_nocolor, R.drawable.para3),
            R.id.mood4 to Pair(R.drawable.para4_nocolor, R.drawable.para4),
            R.id.mood5 to Pair(R.drawable.para5_nocolor, R.drawable.para5)
        )

        if (selectedMoodButton == button) {
            button.isSelected = false
            moodDrawables[button.id]?.first?.let { button.setImageResource(it) }
            selectedMoodButton = null
            return false
        }

        allButtons.forEach {
            it.isSelected = false
            moodDrawables[it.id]?.first?.let { drawable -> it.setImageResource(drawable) }
        }

        button.isSelected = true
        moodDrawables[button.id]?.second?.let { button.setImageResource(it) }
        selectedMoodButton = button
        return true
    }
}