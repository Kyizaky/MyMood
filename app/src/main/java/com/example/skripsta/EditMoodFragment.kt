package com.example.skripsta

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavArgs
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.adapter.FeelingAdapter
import com.example.skripsta.data.Item
import com.example.skripsta.data.User
import com.example.skripsta.data.UserViewModel
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

    private lateinit var binding: FragmentEditMoodBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.GONE

        binding = FragmentEditMoodBinding.inflate(inflater, container, false)

        binding.btnCal.setText(args.currentUser.tanggal)
        binding.btnClock.setText(args.currentUser.jam)
        binding.etJudul.setText(args.currentUser.judul)
        binding.etJurnal.setText(args.currentUser.jurnal)

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
        val feelings = listOf("Happy", "Sad", "Excited", "Angry", "Relaxed", "Anxious")
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

    private fun updateDataToDatabase(view: View) {
        val journalContent = binding.etJurnal.text.toString()
        val titleJournal = binding.etJudul.text.toString()
        val moodType = getSelectedMoodType(view)
        val selectedFeeling = selectedFeelingText
        val selectedActivities = getSelectedActivity(view) // Dapatkan semua aktivitas yang dipilih
        val selectedDate = view.findViewById<EditText>(R.id.btn_cal)?.text.toString()
        val selectedTime = view.findViewById<EditText>(R.id.btn_clock)?.text.toString()

        if (journalContent.isBlank() || titleJournal.isBlank() || moodType == null || selectedFeeling == null || selectedActivities == null || selectedDate.isBlank() || selectedTime.isBlank()) {
            Toast.makeText(requireContext(), "Lengkapi semua data sebelum menyimpan!", Toast.LENGTH_SHORT).show()
            return
        } else {
            val updatedUser  = User(
                id = args.currentUser.id,
                mood = moodType.toInt(),
                activities = selectedActivities.getDisplayName(), // Simpan nama aktivitas
                activityIcon = selectedActivities.drawableId,
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