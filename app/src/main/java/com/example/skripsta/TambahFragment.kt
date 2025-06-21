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
import com.example.skripsta.data.User
import com.example.skripsta.data.UserViewModel
import com.example.skripsta.data.ActivityViewModel
import com.example.skripsta.data.FeelingViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TambahFragment : Fragment() {

    private lateinit var mUserViewModel: UserViewModel
    private lateinit var mFeelingViewModel: FeelingViewModel
    private lateinit var mActivityViewModel: ActivityViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private var selectedFeelingText: String? = null
    private var selectedActivityItem: Item? = null
    private var selectedMoodButton: ImageButton? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tambah, container, false)

        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        mFeelingViewModel = ViewModelProvider(this).get(FeelingViewModel::class.java)
        mActivityViewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)
        sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.GONE

        view.findViewById<ImageView>(R.id.ic_back).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<Button>(R.id.btn_save).setOnClickListener {
            insertDataToDatabase(view)
        }

        val btnCal: EditText = view.findViewById(R.id.btn_cal)
        val btnClock: EditText = view.findViewById(R.id.btn_clock)
        val calendar = Calendar.getInstance()

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        btnClock.setText(timeFormat.format(calendar.time))

        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        btnCal.setText(dateFormat.format(calendar.time))

        btnClock.setOnClickListener {
            val timePickerDialog = TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    val selectedTime = Calendar.getInstance()
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    selectedTime.set(Calendar.MINUTE, minute)
                    btnClock.setText(timeFormat.format(selectedTime.time))
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
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, dayOfMonth)
                    btnCal.setText(dateFormat.format(selectedDate.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
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
            R.id.mood4 to 4, R.id.mood5 to 5, R.id.mood6 to 6
        )
        moodButtons.forEach { (id, _) ->
            view.findViewById<ImageButton>(id).setOnClickListener { button ->
                updateMoodSelection(button as ImageButton, moodButtons.map { view.findViewById<ImageButton>(it.first) })
            }
        }
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
        }

        mActivityViewModel.allActivities.observe(viewLifecycleOwner, Observer { activities ->
            Log.d("TambahFragment", "Total activities in database: ${activities.size}")
            Log.d("TambahFragment", "Activities: $activities")
            var selectedNames = sharedPreferences.getStringSet("selected_activity_names", emptySet()) ?: emptySet()
            Log.d("TambahFragment", "Selected activity names from SharedPreferences: $selectedNames")

            if (selectedNames.isEmpty() && activities.isNotEmpty()) {
                Log.w("TambahFragment", "Selected activity names empty, using all activities")
                selectedNames = activities.map { it.name }.toSet()
                sharedPreferences.edit().putStringSet("selected_activity_names", selectedNames).apply()
                Log.d("TambahFragment", "Updated selected activity names: $selectedNames")
            }

            val displayedActivities = activities.filter { it.name in selectedNames }.map {
                Item(it.iconRes, it.selectedIconRes, it.name)
            }
            Log.d("TambahFragment", "Displayed activities: $displayedActivities")
            recyclerView.adapter = ActivityAdapter(displayedActivities) { selectedItem ->
                selectedActivityItem = selectedItem
                Log.d("TambahFragment", "Selected Activity: ${selectedItem.getDisplayName()}, Drawable ID: ${selectedItem.drawableId}")
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
            Log.d("TambahFragment", "Total feelings in database: ${feelings.size}")
            Log.d("TambahFragment", "Feelings: $feelings")
            var selectedNames = sharedPreferences.getStringSet("selected_feeling_names", emptySet()) ?: emptySet()
            Log.d("TambahFragment", "Selected feeling names from SharedPreferences: $selectedNames")

            if (selectedNames.isEmpty() && feelings.isNotEmpty()) {
                Log.w("TambahFragment", "Selected feeling names empty, using all feelings")
                selectedNames = feelings.map { it.name }.toSet()
                sharedPreferences.edit().putStringSet("selected_feeling_names", selectedNames).apply()
                Log.d("TambahFragment", "Updated selected feeling names: $selectedNames")
            }

            val displayedFeelings = feelings.filter { it.name in selectedNames }.map { it.name }
            Log.d("TambahFragment", "Displayed feelings: $displayedFeelings")
            recyclerView.adapter = FeelingAdapter(displayedFeelings) { selectedFeeling ->
                selectedFeelingText = selectedFeeling
                Log.d("TambahFragment", "Selected Feeling: $selectedFeeling")
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

        if (moodType == null || selectedFeeling == null || selectedActivity == null || selectedDate.isBlank() || selectedTime.isBlank()) {
            Toast.makeText(requireContext(), "Lengkapi semua data sebelum menyimpan!", Toast.LENGTH_SHORT).show()
            return
        }

        val user = User(
            id = 0,
            mood = moodType,
            activities = selectedActivity.getDisplayName(),
            activityIcon = selectedActivity.drawableId,
            perasaan = selectedFeeling,
            judul = titleJournal,
            jurnal = journalContent,
            tanggal = selectedDate,
            jam = selectedTime
        )

        Log.d("TambahFragment", "Saving User - Activity: ${user.activities}, Icon: ${user.activityIcon}")
        mUserViewModel.addUser(user)
        val action = TambahFragmentDirections.actionTambahFragmentToValidationFragment(moodType)
        findNavController().navigate(action)
    }

    private fun getSelectedMoodType(view: View): Int? {
        val moodButtons = listOf(
            R.id.mood1 to 1, R.id.mood2 to 2, R.id.mood3 to 3,
            R.id.mood4 to 4,
            R.id.mood5 to 5,
            R.id.mood6 to 6
        )
        return moodButtons.firstOrNull { view.findViewById<ImageButton>(it.first)?.isSelected == true }?.second
    }

    private fun updateMoodSelection(button: ImageButton, allButtons: List<ImageButton>): Boolean {
        val moodDrawables = mapOf(
            R.id.mood1 to Pair(R.drawable.mood1_nocolor, R.drawable.mood1),
            R.id.mood2 to Pair(R.drawable.mood2_nocolor, R.drawable.mood2),
            R.id.mood3 to Pair(R.drawable.mood3_nocolor, R.drawable.mood3),
            R.id.mood4 to Pair(R.drawable.mood4_nocolor, R.drawable.mood4),
            R.id.mood5 to Pair(R.drawable.mood5_nocolor, R.drawable.mood5),
            R.id.mood6 to Pair(R.drawable.mood6_nocolor, R.drawable.mood6)
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
