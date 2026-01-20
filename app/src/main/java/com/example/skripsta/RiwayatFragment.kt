package com.example.skripsta

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skripsta.adapter.MoodHistoryAdapter
import com.example.skripsta.data.entity.MoodEntry
import com.example.skripsta.viewmodel.MoodEntryViewModel
import com.example.skripsta.databinding.FragmentRiwayatBinding
import com.example.skripsta.utils.MoodUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class RiwayatFragment : Fragment() {

    // ViewBinding untuk fragment riwayat
    private lateinit var binding: FragmentRiwayatBinding

    // ViewModel untuk mengelola data mood
    private lateinit var moodEntryViewModel: MoodEntryViewModel

    // Adapter untuk RecyclerView riwayat mood
    private lateinit var moodHistoryAdapter: MoodHistoryAdapter

    // Daftar nama bulan
    private val monthsList = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    // Daftar tahun yang tersedia
    private val yearsList = (2025..2030).map { it.toString() }

    // Menyimpan tanggal yang sedang dipilih
    private var selectedFullDate: LocalDate? = null

    // Menyimpan bulan terakhir yang dicek
    private var lastCheckedMonth: String? = null

    // Handler untuk pengecekan perubahan tanggal secara berkala
    private val handler = Handler(Looper.getMainLooper())

    // Runnable untuk mengecek pergantian bulan setiap 1 menit
    private val checkDateRunnable = object : Runnable {
        override fun run() {
            updateButtonsIfNeeded()
            handler.postDelayed(this, 60_000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inisialisasi binding
        binding = FragmentRiwayatBinding.inflate(inflater, container, false)

        // Inisialisasi ViewModel
        moodEntryViewModel =
            ViewModelProvider(this).get(MoodEntryViewModel::class.java)

        // Set tanggal awal ke hari ini
        selectedFullDate = LocalDate.now()

        // Menampilkan tanggal dalam format yang sudah disesuaikan
        binding.dateButton.text =
            MoodUtils.formatTanggal(selectedFullDate!!.toString())

        // Setup tombol date picker
        setupDatePickerButton()

        // Setup RecyclerView
        setupRecyclerView()

        // Observasi perubahan data mood
        moodEntryViewModel.readAllData.observe(viewLifecycleOwner) { entries ->
            updateRecyclerView(entries)
        }

        // Navigasi kembali ke HomeFragment
        binding.backHistory.setOnClickListener {
            findNavController().popBackStack(
                R.id.homeFragment,
                false
            )
        }



        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // Mulai pengecekan tanggal saat fragment aktif
        handler.post(checkDateRunnable)
    }

    override fun onPause() {
        super.onPause()
        // Hentikan pengecekan saat fragment tidak aktif
        handler.removeCallbacks(checkDateRunnable)
    }

    // Setup klik pada tombol pemilih tanggal
    private fun setupDatePickerButton() {
        binding.dateButton.setOnClickListener {
            showDatePickerDialog()
        }
    }

    // Menampilkan BottomSheet Date Picker
    private fun showDatePickerDialog() {

        val dialog = BottomSheetDialog(requireContext())
        val view =
            layoutInflater.inflate(R.layout.bottom_sheet_picker3, null)
        dialog.setContentView(view)

        // Inisialisasi NumberPicker
        val datePicker = view.findViewById<NumberPicker>(R.id.date_picker)
        val monthPicker = view.findViewById<NumberPicker>(R.id.month_picker)
        val yearPicker = view.findViewById<NumberPicker>(R.id.year_picker)
        val btnCancel = view.findViewById<ImageButton>(R.id.btn_cancel)
        val btnConfirm = view.findViewById<Button>(R.id.btn_confirm)

        // Ambil tanggal awal
        val initialDate = selectedFullDate ?: LocalDate.now()
        val initialDay = initialDate.dayOfMonth
        val initialMonthIndex = initialDate.monthValue - 1
        val initialYearIndex =
            yearsList.indexOf(initialDate.year.toString()).coerceAtLeast(0)

        // Setup year picker
        yearPicker.minValue = 0
        yearPicker.maxValue = yearsList.size - 1
        yearPicker.displayedValues = yearsList.toTypedArray()
        yearPicker.value = initialYearIndex

        // Setup month picker
        monthPicker.minValue = 0
        monthPicker.maxValue = monthsList.size - 1
        monthPicker.displayedValues = monthsList.toTypedArray()
        monthPicker.value = initialMonthIndex

        // Fungsi untuk menentukan jumlah hari dalam bulan
        fun getMaxDay(month: Int, year: Int): Int {
            return when (month + 1) {
                1, 3, 5, 7, 8, 10, 12 -> 31
                4, 6, 9, 11 -> 30
                2 ->
                    if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
                        29 else 28
                else -> 30
            }
        }

        // Memperbarui date picker sesuai bulan dan tahun
        fun updateDatePicker() {
            val selectedYear =
                yearsList[yearPicker.value].toInt()
            val selectedMonth = monthPicker.value
            val maxDay =
                getMaxDay(selectedMonth, selectedYear)

            val dayList =
                (1..maxDay).map { it.toString() }.toTypedArray()

            datePicker.minValue = 0
            datePicker.maxValue = dayList.size - 1
            datePicker.displayedValues = dayList
            datePicker.value =
                (initialDay - 1).coerceAtMost(maxDay - 1)
        }

        // Inisialisasi date picker
        updateDatePicker()

        // Listener perubahan bulan dan tahun
        monthPicker.setOnValueChangedListener { _, _, _ ->
            updateDatePicker()
        }
        yearPicker.setOnValueChangedListener { _, _, _ ->
            updateDatePicker()
        }

        // Tombol batal
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Tombol konfirmasi
        btnConfirm.setOnClickListener {

            val day = datePicker.value + 1
            val month = monthPicker.value + 1
            val year =
                yearsList[yearPicker.value].toInt()

            // Set tanggal yang dipilih
            selectedFullDate =
                LocalDate.of(year, month, day)

            // Update tampilan tanggal
            binding.dateButton.text =
                MoodUtils.formatTanggal(
                    selectedFullDate!!.toString()
                )

            // Update data RecyclerView
            moodEntryViewModel.readAllData.value?.let {
                updateRecyclerView(it)
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    // Mengecek pergantian bulan otomatis
    private fun updateButtonsIfNeeded() {

        val now = LocalDate.now()
        val currentMonthYear =
            now.format(
                DateTimeFormatter.ofPattern(
                    "MMM yyyy",
                    Locale.ENGLISH
                )
            )

        if (currentMonthYear != lastCheckedMonth) {

            // Reset ke tanggal hari ini
            selectedFullDate = now
            binding.dateButton.text =
                MoodUtils.formatTanggal(
                    selectedFullDate!!.toString()
                )

            // Update data
            moodEntryViewModel.readAllData.value?.let {
                updateRecyclerView(it)
            }

            lastCheckedMonth = currentMonthYear
        }
    }

    // Setup RecyclerView riwayat
    private fun setupRecyclerView() {

        moodHistoryAdapter =
            MoodHistoryAdapter { moodEntry ->

                // Navigasi ke detail riwayat
                val action =
                    RiwayatFragmentDirections
                        .actionRiwayatFragmentToIsiRiwayatFragment(
                            moodEntry
                        )
                findNavController().navigate(action)
            }

        binding.historyRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(context)
            adapter = moodHistoryAdapter
        }
    }

    // Memfilter dan menampilkan data sesuai tanggal
    private fun updateRecyclerView(
        moodEntries: List<MoodEntry>
    ) {

        val selectedDate = selectedFullDate ?: return

        val inputFormatter =
            DateTimeFormatter.ofPattern(
                "yyyy-MM-dd",
                Locale.ENGLISH
            )

        // Filter data berdasarkan tanggal
        val filteredEntries =
            moodEntries.filter { entry ->
                try {
                    val entryDate =
                        LocalDate.parse(
                            entry.tanggal,
                            inputFormatter
                        )
                    entryDate == selectedDate
                } catch (e: Exception) {
                    false
                }
            }.sortedByDescending { it.jam }

        // Update adapter
        moodHistoryAdapter.submitList(filteredEntries)

        // Atur tampilan jika data kosong
        if (filteredEntries.isEmpty()) {
            binding.historyRecyclerView.visibility =
                View.GONE
            binding.tvNoData.visibility =
                View.VISIBLE
        } else {
            binding.historyRecyclerView.visibility =
                View.VISIBLE
            binding.tvNoData.visibility =
                View.GONE
        }
    }

}
