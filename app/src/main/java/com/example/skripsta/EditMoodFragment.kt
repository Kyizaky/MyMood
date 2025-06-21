package com.example.skripsta

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.adapter.ActivityAdapter
import com.example.skripsta.adapter.FeelingAdapter
import com.example.skripsta.adapter.getDisplayName
import com.example.skripsta.data.Item
import com.example.skripsta.data.User
import com.example.skripsta.data.UserViewModel
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
    private lateinit var mUserViewModel: UserViewModel
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
    ): View? {
        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        mActivityViewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)
        mFeelingViewModel = ViewModelProvider(this).get(FeelingViewModel::class.java)
        sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.GONE

        binding = FragmentEditMoodBinding.inflate(inflater, container, false)

        binding.btnCal.setText(args.currentUser.tanggal)
        binding.btnClock.setText(args.currentUser.jam)
        binding.tvJurnaling.setText(args.currentUser.judul)
        binding.etJornal.setText(args.currentUser.jurnal)

        // Format tanggal
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

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

        // Tambahkan click listener untuk ikon pensil untuk activities
        binding.editActivitiesButton.setOnClickListener {
            findNavController().navigate(R.id.action_editMoodFragment_to_selectActivityFragment)
        }

        // Tambahkan click listener untuk ikon pensil untuk feelings
        binding.editFeelingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_editMoodFragment_to_selectFeelingFragment)
        }

        setupMoodButtons(binding.root)
        setupRecyclerView(binding.root)
        setupFeelingRecyclerView(binding.root)

        return binding.root
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
            Log.d("EditMoodFragment", "Observed activities: $activities")
            val selectedNames = sharedPreferences.getStringSet("selected_activity_names", emptySet()) ?: emptySet()
            val displayedActivities = activities.filter { it.name in selectedNames }.map {
                Item(
                    drawableId = it.iconRes,
                    selectedDrawableId = it.selectedIconRes,
                    text = it.name,
                )
            }
            Log.d("EditMoodFragment", "Displayed activities: $displayedActivities")
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
            justifyContent = JustifyContent.FLEX_START
        }

        mFeelingViewModel.allFeelings.observe(viewLifecycleOwner, Observer { feelings ->
            val selectedNames = sharedPreferences.getStringSet("selected_feeling_names", emptySet()) ?: emptySet()
            val displayedFeelings = feelings.filter { it.name in selectedNames }.map { it.name }
            recyclerView.adapter = FeelingAdapter(displayedFeelings) { selectedFeeling ->
                selectedFeelingText = selectedFeeling
            }
        })
    }

    private fun getSelectedMoodType(view: View): Int? {
        val moodButtons = listOf(
            R.id.mood1 to 1, R.id.mood2 to 2, R.id.mood3 to 3,
            R.id.mood4 to 4, R.id.mood5 to 5, R.id.mood6 to 6
        )
        return moodButtons.firstOrNull { view.findViewById<ImageButton>(it.first).isSelected }?.second
    }

    private fun updateMoodSelection(button: ImageButton, allButtons: List<ImageButton>) {
        val moodDrawables = mapOf(
            R.id.mood1 to Pair(R.drawable.mood1_nocolor, R.drawable.mood1),
            R.id.mood2 to Pair(R.drawable.mood2_nocolor, R.drawable.mood2),
            R.id.mood3 to Pair(R.drawable.mood3_nocolor, R.drawable.mood3),
            R.id.mood4 to Pair(R.drawable.mood4_nocolor, R.drawable.mood4),
            R.id.mood5 to Pair(R.drawable.mood5_nocolor, R.drawable.mood5),
            R.id.mood6 to Pair(R.drawable.mood6_nocolor, R.drawable.mood6)
        )

        // Jika tombol yang sama ditekan lagi, deselect
        if (selectedMoodButton == button) {
            button.isSelected = false
            val defaultDrawable = moodDrawables[button.id]?.first
            if (defaultDrawable != null) button.setImageResource(defaultDrawable)
            selectedMoodButton = null
            return
        }

        // Reset semua tombol
        allButtons.forEach {
            it.isSelected = false
            val defaultDrawable = moodDrawables[it.id]?.first
            if (defaultDrawable != null) it.setImageResource(defaultDrawable)
        }

        // Set tombol baru sebagai aktif
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
        } else {
            val updatedUser = User(
                id = args.currentUser.id,
                mood = moodType.toInt(),
                activities = selectedActivity.getDisplayName(),
                activityIcon = selectedActivity.drawableId,
                perasaan = selectedFeeling,
                judul = titleJournal,
                jurnal = journalContent,
                tanggal = selectedDate,
                jam = selectedTime
            )

            mUserViewModel.updateUser(updatedUser)
            Toast.makeText(requireContext(), "Berhasil", Toast.LENGTH_LONG).show()
            val action = EditMoodFragmentDirections.actionEditMoodFragmentToIsiRiwayatFragment(updatedUser)
            findNavController().navigate(action)
        }
    }
}