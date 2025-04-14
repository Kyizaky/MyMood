package com.example.skripsta

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.adapter.FeelingAdapter
import com.example.skripsta.data.Item
import com.example.skripsta.data.User
import com.example.skripsta.data.UserViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TambahFragment : Fragment() {

    private lateinit var mUserViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tambah, container, false)

        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.GONE

        view.findViewById<ImageView>(R.id.ic_back).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        view.findViewById<Button>(R.id.btn_save).setOnClickListener {
            insertDataToDatabase(view)
        }

        val btnCal: EditText = view.findViewById(R.id.btn_cal)
        val btnClock: EditText = view.findViewById(R.id.btn_clock)
        val calendar = Calendar.getInstance()

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        btnClock.setText(timeFormat.format(calendar.time))

        // Format tanggal
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        btnCal.setText(dateFormat.format(calendar.time))

        // Event untuk menampilkan TimePickerDialog
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

        // Event untuk menampilkan DatePickerDialog
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
        val items = listOf(
            Item(R.drawable.activity1, "Study"),
            Item(R.drawable.activity2, "Shop"),
            Item(R.drawable.activity3, "Work"),
            Item(R.drawable.activity4, "Vacation"),
            Item(R.drawable.activity5, "Eat"),
            Item(R.drawable.activity6, "Gym")
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START // Agar item ditata lebih rapi
        }

        view.findViewById<RecyclerView>(R.id.recycler_view).apply {
            adapter = ItemAdapter(items) { selectedItem ->
                items.forEach { it.isSelected = false }
                selectedItem.isSelected = true
                adapter?.notifyDataSetChanged()
            }
        }
    }

    private var selectedFeelingText: String? = null

    private fun setupFeelingRecyclerView(view: View) {
        val feelings = listOf("Angry", "Disgust", "Scary", "Sad", "Happy", "Neutral")
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_feelings)

        recyclerView.layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START // Agar item ditata lebih rapi
        }
        recyclerView.adapter = FeelingAdapter(feelings) { selectedFeeling ->
            selectedFeelingText = selectedFeeling
        }
    }

    private fun insertDataToDatabase(view: View) {
        val journalContent = view.findViewById<EditText>(R.id.etJornal)?.text.toString().ifBlank { "No story today" }
        val titleJournal = view.findViewById<EditText>(R.id.tvJurnaling)?.text.toString().ifBlank { "Today" }
        val moodType = getSelectedMoodType(view)
        val selectedFeeling = selectedFeelingText
        val selectedActivity = getSelectedActivity(view) // Ambil objek Item yang dipilih
        val selectedDate = view.findViewById<EditText>(R.id.btn_cal)?.text.toString()
        val selectedTime = view.findViewById<EditText>(R.id.btn_clock)?.text.toString()

        if (moodType == null || selectedFeeling == null || selectedActivity == null || selectedDate.isBlank() || selectedTime.isBlank()) {
            Toast.makeText(requireContext(), "Lengkapi semua data sebelum menyimpan!", Toast.LENGTH_SHORT).show()
            return
        } else {
            val user = User(
                id = 0,
                mood = moodType.toInt(),
                activities = selectedActivity.getDisplayName(), // Simpan nama aktivitas
                activityIcon = selectedActivity.drawableId, // Simpan ID drawable gambar aktivitas
                perasaan = selectedFeeling,
                judul = titleJournal,
                jurnal = journalContent,
                tanggal = selectedDate,
                jam = selectedTime
            )

            mUserViewModel.addUser(user)
            Toast.makeText(requireContext(), "Berhasil", Toast.LENGTH_LONG).show()
            val action = TambahFragmentDirections.actionTambahFragmentToValidationFragment(moodType)
            findNavController().navigate(action)

        }
    }


    private fun getSelectedMoodType(view: View): Int? {
        val moodButtons = listOf(
            R.id.mood1 to 1, R.id.mood2 to 2, R.id.mood3 to 3,
            R.id.mood4 to 4, R.id.mood5 to 5, R.id.mood6 to 6
        )
        return moodButtons.firstOrNull { view.findViewById<ImageButton>(it.first).isSelected }?.second
    }

    private fun updateMoodSelection(button: ImageButton, allButtons: List<ImageButton>) {
        allButtons.forEach {
            it.isSelected = false
            it.setBackgroundColor(resources.getColor(R.color.white))
        }
        button.isSelected = true
        button.setBackgroundColor(resources.getColor(R.color.vista))
    }

    private fun getSelectedActivity(view: View): Item? {
        val adapter = view.findViewById<RecyclerView>(R.id.recycler_view).adapter as? ItemAdapter
        return adapter?.items?.firstOrNull { it.isSelected }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.VISIBLE
    }
}
