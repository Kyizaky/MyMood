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
import android.widget.Button
import android.widget.EditText
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
import com.example.skripsta.data.entity.MoodEntry
import com.example.skripsta.viewmodel.MoodEntryViewModel
import com.example.skripsta.viewmodel.ActivityViewModel
import com.example.skripsta.viewmodel.FeelingViewModel
import com.example.skripsta.databinding.FragmentEditMoodBinding
import com.example.skripsta.utils.MoodUtils
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class EditMoodFragment : Fragment() {

    // Mengambil argumen mood entry dari fragment sebelumnya
    private val args by navArgs<EditMoodFragmentArgs>()

    // ViewModel untuk mood, aktivitas, dan perasaan
    private lateinit var mMoodEntryViewModel: MoodEntryViewModel
    private lateinit var mActivityViewModel: ActivityViewModel
    private lateinit var mFeelingViewModel: FeelingViewModel

    // SharedPreferences untuk menyimpan pilihan user
    private lateinit var sharedPreferences: SharedPreferences

    // ViewBinding fragment
    private lateinit var binding: FragmentEditMoodBinding

    // Variabel penyimpan pilihan user
    private var selectedFeelingText: String? = null
    private var selectedActivityItem: Item? = null
    private var selectedMoodButton: ImageButton? = null

    // Formatter tanggal dan waktu
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inisialisasi ViewModel
        mMoodEntryViewModel = ViewModelProvider(this).get(MoodEntryViewModel::class.java)
        mActivityViewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)
        mFeelingViewModel = ViewModelProvider(this).get(FeelingViewModel::class.java)

        // Inisialisasi SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        // Menyembunyikan bottom navigation
        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.GONE

        // Inisialisasi binding
        binding = FragmentEditMoodBinding.inflate(inflater, container, false)

        // Mengambil data mood entry lama
        val moodEntry = args.moodEntry

        // Menampilkan data awal ke UI
        binding.btnCal.setText(MoodUtils.formatCal(moodEntry.tanggal))
        binding.btnClock.setText(moodEntry.jam)
        binding.tvJurnaling.setText(moodEntry.jurnal)
        binding.etJornal.setText(moodEntry.judul)

        // Menyimpan data awal ke variabel
        selectedFeelingText = moodEntry.perasaan
        selectedActivityItem = Item(
            drawableId = moodEntry.activityIcon,
            selectedDrawableId = moodEntry.activityIcon,
            text = moodEntry.activities,
            isSelected = true
        )

        val calendar = Calendar.getInstance()

        // Setup tombol mood dengan mood awal
        setupMoodButtons(binding.root, moodEntry.mood)

        // Time picker
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

        // Date picker
        binding.btnCal.setOnClickListener {

            val todayCalendar = Calendar.getInstance()

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, dayOfMonth)

                    binding.btnCal.setText(
                        MoodUtils.formatCal(
                            dateFormat.format(selectedDate.time)
                        )
                    )

                    updateSaveButtonState(binding.root)
                },
                todayCalendar.get(Calendar.YEAR),
                todayCalendar.get(Calendar.MONTH),
                todayCalendar.get(Calendar.DAY_OF_MONTH)
            )

            // 🚫 Batasi agar tidak bisa memilih tanggal masa depan
            datePickerDialog.datePicker.maxDate = todayCalendar.timeInMillis

            datePickerDialog.show()
        }


        // Tombol update mood
        binding.btnUpdate.setOnClickListener {
            updateDataToDatabase(binding.root)
        }

        // Navigasi ke edit aktivitas
        binding.editActivitiesButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_editMoodFragment_to_selectActivityFragment
            )
        }

        // Navigasi ke edit perasaan
        binding.editFeelingsButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_editMoodFragment_to_selectFeelingFragment
            )
        }

        // Tombol kembali
        binding.icBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Setup recycler view aktivitas dan perasaan
        setupRecyclerView(binding.root)
        setupFeelingRecyclerView(binding.root)

        // Update status tombol simpan
        updateSaveButtonState(binding.root)

        return binding.root
    }

    // Setup tombol mood dan status awalnya
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

            // Menentukan mood awal
            if (moodValue == initialMood) {
                button.isSelected = true
                selectedMoodButton = button
                moodDrawables[id]?.second?.let { button.setImageResource(it) }
            } else {
                button.isSelected = false
                moodDrawables[id]?.first?.let { button.setImageResource(it) }
            }

            // Listener klik mood
            button.setOnClickListener {
                updateMoodSelection(
                    it as ImageButton,
                    moodButtons.map { view.findViewById<ImageButton>(it.first) }
                )
                updateSaveButtonState(view)
            }
        }
    }

    // Setup RecyclerView aktivitas
    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_activities)

        recyclerView.layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
        }

        mActivityViewModel.allActivities.observe(viewLifecycleOwner, Observer { activities ->
            val selectedNames =
                sharedPreferences.getStringSet("selected_activity_names", emptySet())
                    ?: emptySet()

            val displayedActivities = activities
                .filter { it.name in selectedNames }
                .map {
                    Item(
                        drawableId = it.selectedIconRes,
                        selectedDrawableId = it.iconRes,
                        text = it.name,
                        isSelected = it.name == args.moodEntry.activities
                    )
                }

            recyclerView.adapter =
                ActivityAdapter(displayedActivities, args.moodEntry.activities) { selectedItem ->
                    selectedActivityItem = selectedItem
                    updateSaveButtonState(view)
                }
        })
    }

    // Setup RecyclerView perasaan
    private fun setupFeelingRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_feelings)

        recyclerView.layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
        }

        mFeelingViewModel.allFeelings.observe(viewLifecycleOwner, Observer { feelings ->
            val selectedNames =
                sharedPreferences.getStringSet("selected_feeling_names", emptySet())
                    ?: emptySet()

            val displayedFeelings =
                feelings.filter { it.name in selectedNames }.map { it.name }

            recyclerView.adapter =
                FeelingAdapter(displayedFeelings, args.moodEntry.perasaan) { selectedFeeling ->
                    selectedFeelingText = selectedFeeling
                    updateSaveButtonState(view)
                }
        })
    }

    // Mendapatkan mood yang dipilih
    private fun getSelectedMoodType(view: View): Int? {
        val moodButtons = listOf(
            R.id.mood1 to 1, R.id.mood2 to 2, R.id.mood3 to 3,
            R.id.mood4 to 4, R.id.mood5 to 5
        )
        return moodButtons.firstOrNull {
            view.findViewById<ImageButton>(it.first).isSelected
        }?.second
    }

    // Mengatur perubahan seleksi mood
    private fun updateMoodSelection(button: ImageButton, allButtons: List<ImageButton>) {
        val moodDrawables = mapOf(
            R.id.mood1 to Pair(R.drawable.para1_nocolor, R.drawable.para1),
            R.id.mood2 to Pair(R.drawable.para2_nocolor, R.drawable.para2),
            R.id.mood3 to Pair(R.drawable.para3_nocolor, R.drawable.para3),
            R.id.mood4 to Pair(R.drawable.para4_nocolor, R.drawable.para4),
            R.id.mood5 to Pair(R.drawable.para5_nocolor, R.drawable.para5)
        )

        // Jika mood yang sama ditekan ulang
        if (selectedMoodButton == button) {
            button.isSelected = false
            moodDrawables[button.id]?.first?.let { button.setImageResource(it) }
            selectedMoodButton = null
            return
        }

        // Reset semua mood
        allButtons.forEach {
            it.isSelected = false
            moodDrawables[it.id]?.first?.let { drawable -> it.setImageResource(drawable) }
        }

        // Set mood terpilih
        button.isSelected = true
        moodDrawables[button.id]?.second?.let { button.setImageResource(it) }
        selectedMoodButton = button
    }

    // Update data mood ke database
    private fun updateDataToDatabase(view: View) {
        val journalContent =
            binding.tvJurnaling.text.toString().ifBlank { "No story today" }
        val titleJournal =
            binding.etJornal.text.toString().ifBlank { "Today" }

        val moodType = getSelectedMoodType(view)
        val selectedFeeling = selectedFeelingText
        val selectedActivity = selectedActivityItem
        val selectedDate = binding.btnCal.text.toString()
        val selectedTime = binding.btnClock.text.toString()

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
            Toast.makeText(
                requireContext(),
                "Lengkapi semua data sebelum menyimpan!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Membuat objek mood baru
        val updatedMood = MoodEntry(
            id = args.moodEntry.id,
            mood = moodType,
            user_Id = args.moodEntry.user_Id,
            activities = selectedActivity.getDisplayName(),
            activityIcon =
                if (selectedActivity.isSelected)
                    selectedActivity.selectedDrawableId
                else
                    selectedActivity.drawableId,
            perasaan = selectedFeeling,
            judul = titleJournal,
            jurnal = journalContent,
            tanggal = storedDate,
            jam = selectedTime
        )

        // Update ke database
        mMoodEntryViewModel.updateMoodEntry(updatedMood)

        // Notifikasi berhasil
        Toast.makeText(requireContext(), "Berhasil", Toast.LENGTH_LONG).show()

        // Kembali ke fragment sebelumnya
        findNavController().popBackStack()
    }

    // Mengatur status tombol simpan
    private fun updateSaveButtonState(view: View) {
        val moodType = getSelectedMoodType(view)
        val selectedFeeling = selectedFeelingText
        val selectedActivity = selectedActivityItem
        val selectedDate =
            view.findViewById<EditText>(R.id.btn_cal)?.text.toString()
        val selectedTime =
            view.findViewById<EditText>(R.id.btn_clock)?.text.toString()

        val saveButton = view.findViewById<Button>(R.id.btnUpdate)

        val isComplete =
            moodType != null &&
                    selectedFeeling != null &&
                    selectedActivity != null &&
                    selectedDate.isNotBlank() &&
                    selectedTime.isNotBlank()

        // Mengatur tampilan tombol simpan
        if (isComplete) {
            saveButton.setBackgroundResource(R.drawable.bg_btn)
            saveButton.isEnabled = true
        } else {
            saveButton.setBackgroundResource(R.drawable.bg_btn_disabled)
            saveButton.isEnabled = false
        }
    }
}
