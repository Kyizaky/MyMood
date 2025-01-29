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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.data.Item
import com.example.skripsta.data.User
import com.example.skripsta.data.UserViewModel
import com.example.skripsta.adapter.ItemAdapter
import com.example.skripsta.adapter.getDisplayName
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
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
            Item(R.drawable.ic_bekerja, "Family"),
            Item(R.drawable.ic_belajar, "Friends"),
            Item(R.drawable.ic_belanja, "Beloved"),
            Item(R.drawable.ic_makan, "Colleague"),
            Item(R.drawable.ic_olahraga, "Stranger"),
            Item(R.drawable.ic_renang, "Party"),
            Item(R.drawable.ic_riwayat, "Dating"),
            Item(R.drawable.ic_mood, "Traveling")
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START // Agar item ditata lebih rapi
        }

        val adapter = ItemAdapter(items) { selectedItems ->
            // Anda bisa menangani item yang dipilih di sini
            val selectedNames = selectedItems.joinToString(", ") { it.text }
            Toast.makeText(requireContext(), "Selected: $selectedNames", Toast.LENGTH_SHORT).show()
        }

        recyclerView.adapter = adapter
    }


    private fun insertDataToDatabase(view: View) {
        val journalContent = view.findViewById<EditText>(R.id.etJornal)?.text.toString()
        val moodType = getSelectedMoodType(view)
        val selectedFeeling = getSelectedChipText(view)
        val selectedActivities = getSelectedActivities(view) // Dapatkan semua aktivitas yang dipilih
        val selectedDate = view.findViewById<EditText>(R.id.btn_cal)?.text.toString()
        val selectedTime = view.findViewById<EditText>(R.id.btn_clock)?.text.toString()

        if (journalContent.isBlank() || moodType == null || selectedFeeling == null || selectedActivities.isEmpty() || selectedDate.isBlank() || selectedTime.isBlank()) {
            Toast.makeText(requireContext(), "Lengkapi semua data sebelum menyimpan!", Toast.LENGTH_SHORT).show()
            return
        } else {
            val user = User(
                id = 0,
                mood = moodType.toInt(),
                activities = selectedActivities, // Simpan aktivitas sebagai List<String>
                perasaan = selectedFeeling,
                jurnal = journalContent,
                tanggal = selectedDate,
                jam = selectedTime
            )

            mUserViewModel.addUser(user)
            Toast.makeText(requireContext(), "Berhasil", Toast.LENGTH_LONG).show()
            parentFragmentManager.popBackStack()
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

    private fun getSelectedChipText(view: View): String? {
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroup)
        val selectedChips = mutableListOf<String>()

        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            if (chip.isChecked) {
                selectedChips.add(chip.text.toString()) // Tambahkan teks chip yang dipilih
            }
        }

        return if (selectedChips.isNotEmpty()) selectedChips.joinToString(", ") else null
    }

    private fun getSelectedActivities(view: View): List<String> {
        val adapter = view.findViewById<RecyclerView>(R.id.recycler_view).adapter as? ItemAdapter
        return adapter?.items?.filter { it.isSelected }?.map { it.text } ?: emptyList()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.VISIBLE
    }
}
