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
import com.example.skripsta.viewmodel.ActivityViewModel
import com.example.skripsta.viewmodel.FeelingViewModel
import com.example.skripsta.data.entity.MoodEntry
import com.example.skripsta.viewmodel.MoodEntryViewModel
import com.example.skripsta.viewmodel.UserViewModel
import com.example.skripsta.utils.MoodUtils
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class TambahFragment : Fragment() {

    // ViewModel untuk mengelola data mood, user, perasaan, dan aktivitas
    private lateinit var mMoodEntryViewModel: MoodEntryViewModel
    private lateinit var mUserViewModel: UserViewModel
    private lateinit var mFeelingViewModel: FeelingViewModel
    private lateinit var mActivityViewModel: ActivityViewModel

    // SharedPreferences untuk menyimpan pengaturan lokal
    private lateinit var sharedPreferences: SharedPreferences

    // Variabel penyimpanan pilihan user
    private var selectedFeelingText: String? = null
    private var currentUserId: Int = -1
    private var selectedActivityItem: Item? = null
    private var selectedMoodButton: ImageButton? = null

    // Formatter tanggal dan waktu
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate layout fragment
        val view = inflater.inflate(R.layout.fragment_tambah, container, false)

        // Inisialisasi ViewModel
        mMoodEntryViewModel = ViewModelProvider(this).get(MoodEntryViewModel::class.java)
        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        mFeelingViewModel = ViewModelProvider(this).get(FeelingViewModel::class.java)
        mActivityViewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)

        // Inisialisasi SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        // Mengambil ID user yang sedang login
        val currentUserId = sharedPreferences.getInt("current_user_id", 1)

        // Menyembunyikan bottom navigation
        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.GONE

        // Tombol kembali
        view.findViewById<ImageView>(R.id.ic_back).setOnClickListener {
            findNavController().popBackStack()
        }

        // Tombol simpan data mood
        view.findViewById<Button>(R.id.btn_save).setOnClickListener {
            insertDataToDatabase(view)
            mUserViewModel.recordMoodEntry(currentUserId)
        }

        // Inisialisasi input tanggal dan waktu
        val btnCal: EditText = view.findViewById(R.id.btn_cal)
        val btnClock: EditText = view.findViewById(R.id.btn_clock)

        val calendar = Calendar.getInstance()
        val today = LocalDate.now()
        val now = LocalTime.now()

        // Set nilai default tanggal dan waktu
        btnCal.setText(MoodUtils.formatCal(today.format(dateFormatter)))
        btnClock.setText(now.format(timeFormatter))

        // Time picker
        btnClock.setOnClickListener {
            val timePickerDialog = TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    val selectedTime = LocalTime.of(hourOfDay, minute)
                    btnClock.setText(selectedTime.format(timeFormatter))
                    updateSaveButtonState(view)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePickerDialog.show()
        }

        // Date picker
        btnCal.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                    btnCal.setText(MoodUtils.formatCal(selectedDate.format(dateFormatter)))
                    updateSaveButtonState(view)
                },
                today.year,
                today.monthValue - 1,
                today.dayOfMonth
            )
            datePickerDialog.show()
        }

        // Setup komponen UI
        setupMoodButtons(view)
        setupRecyclerView(view)
        setupFeelingRecyclerView(view)
        updateSaveButtonState(view)

        // Navigasi ke pengaturan aktivitas
        view.findViewById<ImageView>(R.id.edit_activities_button).setOnClickListener {
            findNavController().navigate(R.id.action_tambahFragment_to_selectActivityFragment)
        }

        // Navigasi ke pengaturan perasaan
        view.findViewById<ImageView>(R.id.edit_feelings_button).setOnClickListener {
            findNavController().navigate(R.id.action_tambahFragment_to_selectFeelingFragment)
        }

        return view
    }

    // Mengatur tombol pilihan mood
    private fun setupMoodButtons(view: View) {
        val moodButtons = listOf(
            R.id.mood1 to 1, R.id.mood2 to 2, R.id.mood3 to 3,
            R.id.mood4 to 4, R.id.mood5 to 5
        )

        moodButtons.forEach { (id, _) ->
            view.findViewById<ImageButton>(id).setOnClickListener { button ->
                updateMoodSelection(
                    button as ImageButton,
                    moodButtons.map { view.findViewById<ImageButton>(it.first) }
                )
                updateSaveButtonState(view)
            }
        }
    }

    // RecyclerView aktivitas dengan Flexbox
    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_activities)

        recyclerView.layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
        }

        mActivityViewModel.allActivities.observe(viewLifecycleOwner, Observer { activities ->

            // Ambil aktivitas terpilih dari SharedPreferences
            var selectedNames = sharedPreferences.getStringSet(
                "selected_activity_names",
                emptySet()
            ) ?: emptySet()

            // Jika belum ada, tampilkan semua
            if (selectedNames.isEmpty() && activities.isNotEmpty()) {
                selectedNames = activities.map { it.name }.toSet()
                sharedPreferences.edit().putStringSet("selected_activity_names", selectedNames).apply()
            }

            // Filter dan tampilkan aktivitas
            val displayedActivities = activities.filter { it.name in selectedNames }.map {
                Item(it.selectedIconRes, it.iconRes, it.name)
            }

            recyclerView.adapter = ActivityAdapter(displayedActivities) { selectedItem ->
                selectedActivityItem = selectedItem
                updateSaveButtonState(view)
            }
        })
    }

    // RecyclerView perasaan
    private fun setupFeelingRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_feelings)

        recyclerView.layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }

        mFeelingViewModel.allFeelings.observe(viewLifecycleOwner, Observer { feelings ->

            // Ambil perasaan terpilih
            var selectedNames = sharedPreferences.getStringSet(
                "selected_feeling_names",
                emptySet()
            ) ?: emptySet()

            // Default tampilkan semua
            if (selectedNames.isEmpty() && feelings.isNotEmpty()) {
                selectedNames = feelings.map { it.name }.toSet()
                sharedPreferences.edit().putStringSet("selected_feeling_names", selectedNames).apply()
            }

            val displayedFeelings = feelings.filter { it.name in selectedNames }.map { it.name }

            recyclerView.adapter = FeelingAdapter(displayedFeelings) { selectedFeeling ->
                selectedFeelingText = selectedFeeling
                updateSaveButtonState(view)
            }
        })
    }

    // Menyimpan data mood ke database
    private fun insertDataToDatabase(view: View) {

        val journalContent =
            view.findViewById<EditText>(R.id.title_jurnal_save)?.text.toString()
                .ifBlank { "No story today" }

        val titleJournal =
            view.findViewById<EditText>(R.id.isi_jurnal_save)?.text.toString()
                .ifBlank { "Today" }

        val moodType = getSelectedMoodType(view)
        val selectedFeeling = selectedFeelingText
        val selectedActivity = selectedActivityItem
        val selectedDate = view.findViewById<EditText>(R.id.btn_cal)?.text.toString()
        val selectedTime = view.findViewById<EditText>(R.id.btn_clock)?.text.toString()

        // Konversi format tanggal
        val storedDate = try {
            val date = LocalDate.parse(
                selectedDate,
                DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
            )
            date.format(dateFormatter)
        } catch (e: Exception) {
            selectedDate
        }

        // Validasi input
        if (moodType == null || selectedFeeling == null || selectedActivity == null ||
            selectedDate.isBlank() || selectedTime.isBlank()
        ) {
            Toast.makeText(requireContext(), "Complete all data before saving!", Toast.LENGTH_SHORT).show()
            return
        }

        // Membuat objek MoodEntry
        val moodEntry = MoodEntry(
            id = 0,
            mood = moodType,
            user_Id = currentUserId,
            activities = selectedActivity.getDisplayName(),
            activityIcon = selectedActivity.selectedDrawableId,
            perasaan = selectedFeeling,
            judul = titleJournal,
            jurnal = journalContent,
            tanggal = storedDate,
            jam = selectedTime
        )

        // Simpan ke database
        mMoodEntryViewModel.addMoodEntry(moodEntry)

        // Navigasi ke halaman validasi
        val action = TambahFragmentDirections
            .actionTambahFragmentToValidationFragment(moodType)

        findNavController().navigate(action)
    }

    // Mengambil nilai mood yang dipilih
    private fun getSelectedMoodType(view: View): Int? {
        val moodButtons = listOf(
            R.id.mood1 to 1, R.id.mood2 to 2, R.id.mood3 to 3,
            R.id.mood4 to 4, R.id.mood5 to 5
        )
        return moodButtons.firstOrNull {
            view.findViewById<ImageButton>(it.first)?.isSelected == true
        }?.second
    }

    // Mengatur tampilan tombol mood
    private fun updateMoodSelection(
        button: ImageButton,
        allButtons: List<ImageButton>
    ): Boolean {

        val moodDrawables = mapOf(
            R.id.mood1 to Pair(R.drawable.para1_nocolor, R.drawable.para1),
            R.id.mood2 to Pair(R.drawable.para2_nocolor, R.drawable.para2),
            R.id.mood3 to Pair(R.drawable.para3_nocolor, R.drawable.para3),
            R.id.mood4 to Pair(R.drawable.para4_nocolor, R.drawable.para4),
            R.id.mood5 to Pair(R.drawable.para5_nocolor, R.drawable.para5)
        )

        // Jika tombol yang sama ditekan ulang
        if (selectedMoodButton == button) {
            button.isSelected = false
            moodDrawables[button.id]?.first?.let { button.setImageResource(it) }
            selectedMoodButton = null
            return false
        }

        // Reset semua tombol
        allButtons.forEach {
            it.isSelected = false
            moodDrawables[it.id]?.first?.let { drawable ->
                it.setImageResource(drawable)
            }
        }

        // Set tombol terpilih
        button.isSelected = true
        moodDrawables[button.id]?.second?.let { button.setImageResource(it) }
        selectedMoodButton = button

        return true
    }

    // Mengatur status tombol simpan
    private fun updateSaveButtonState(view: View) {

        val moodType = getSelectedMoodType(view)
        val selectedFeeling = selectedFeelingText
        val selectedActivity = selectedActivityItem
        val selectedDate = view.findViewById<EditText>(R.id.btn_cal)?.text.toString()
        val selectedTime = view.findViewById<EditText>(R.id.btn_clock)?.text.toString()

        val saveButton = view.findViewById<Button>(R.id.btn_save)

        val isComplete = moodType != null &&
                selectedFeeling != null &&
                selectedActivity != null &&
                selectedDate.isNotBlank() &&
                selectedTime.isNotBlank()

        if (isComplete) {
            saveButton.setBackgroundResource(R.drawable.bg_btn)
            saveButton.isEnabled = true
        } else {
            saveButton.setBackgroundResource(R.drawable.bg_btn_disabled)
            saveButton.isEnabled = false
        }
    }
}
