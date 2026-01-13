package com.example.skripsta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.skripsta.data.entity.MoodEntry
import com.example.skripsta.viewmodel.MoodEntryViewModel
import com.example.skripsta.databinding.FragmentIsiRiwayatBinding
import com.example.skripsta.utils.MoodUtils

class IsiRiwayatFragment : Fragment() {

    // Mengambil data MoodEntry yang dikirim melalui Navigation Component
    private val args by navArgs<IsiRiwayatFragmentArgs>()

    // ViewModel untuk mengelola data MoodEntry dari Room
    private lateinit var moodEntryViewModel: MoodEntryViewModel

    // ViewBinding untuk mengakses komponen UI
    private lateinit var binding: FragmentIsiRiwayatBinding

    // Menyimpan data mood yang sedang ditampilkan
    private var currentMoodEntry: MoodEntry? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inisialisasi ViewBinding
        binding = FragmentIsiRiwayatBinding.inflate(inflater, container, false)

        // Inisialisasi ViewModel menggunakan Activity scope
        moodEntryViewModel = ViewModelProvider(requireActivity()).get(MoodEntryViewModel::class.java)

        // Menyembunyikan Bottom Navigation saat halaman detail ditampilkan
        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.GONE

        // Mengambil data mood dari argumen navigasi
        currentMoodEntry = args.moodEntry

        // Menampilkan data mood ke UI
        updateUI(currentMoodEntry!!)

        // Observer untuk memantau perubahan data di database Room
        moodEntryViewModel.readAllData.observe(viewLifecycleOwner) { moodList ->
            // Mencari data mood yang sama berdasarkan ID
            val updatedMood = moodList.find { it.id == currentMoodEntry?.id }
            // Jika data berubah, UI diperbarui
            if (updatedMood != null && updatedMood != currentMoodEntry) {
                currentMoodEntry = updatedMood
                updateUI(updatedMood)
            }
        }

        // Aksi tombol hapus data
        binding.btnDel.setOnClickListener {
            deleteMoodEntry()
        }

        // Aksi tombol edit data
        binding.btnEdit.setOnClickListener {
            val action = IsiRiwayatFragmentDirections
                .actionIsiRiwayatFragmentToEditMoodFragment(currentMoodEntry!!)
            findNavController().navigate(action)
        }

        // Tombol kembali ke halaman sebelumnya
        binding.backIsisHistory.setOnClickListener {
            findNavController().popBackStack()
        }

        return binding.root
    }

    // Fungsi untuk menghapus data mood dengan konfirmasi
    private fun deleteMoodEntry() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            // Menghapus data mood dari database
            currentMoodEntry?.let { moodEntryViewModel.deleteMoodEntry(it) }
            Toast.makeText(requireContext(), "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete this data?")
        builder.setMessage("Are you sure?")
        builder.create().show()
    }

    // Fungsi untuk memperbarui tampilan UI berdasarkan data MoodEntry
    private fun updateUI(moodEntry: MoodEntry) {
        // Menampilkan teks mood
        binding.tvTitleP.text = MoodUtils.getMoodText(moodEntry.mood)
        // Menampilkan tanggal
        binding.txtDate.text = MoodUtils.formatTanggal(moodEntry.tanggal)
        // Menampilkan waktu
        binding.txtTime.text = moodEntry.jam
        // Menampilkan perasaan
        binding.tvFeeling.text = moodEntry.perasaan
        // Menampilkan isi jurnal
        binding.tvIsiJurnal.text = moodEntry.jurnal
        // Menampilkan judul jurnal
        binding.tvTitlej.text = moodEntry.judul
        // Menampilkan ikon aktivitas
        binding.imageView2.setImageResource(moodEntry.activityIcon)
        // Menampilkan ikon mood
        binding.ivMood.setImageResource(convertMoodToImage(moodEntry.mood))
        // Menampilkan aktivitas
        binding.tvAktivitasdata.text = moodEntry.activities
    }

    // Mengonversi nilai mood menjadi gambar yang sesuai
    private fun convertMoodToImage(mood: Int): Int {
        return when (mood) {
            1 -> R.drawable.para1
            2 -> R.drawable.para2
            3 -> R.drawable.para3
            4 -> R.drawable.para4
            5 -> R.drawable.para5
            else -> R.drawable.ic_breath
        }
    }
}
