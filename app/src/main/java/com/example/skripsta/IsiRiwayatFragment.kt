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
import com.example.skripsta.data.MoodEntry
import com.example.skripsta.data.MoodEntryViewModel
import com.example.skripsta.databinding.FragmentIsiRiwayatBinding
import com.example.skripsta.utils.MoodUtils

class IsiRiwayatFragment : Fragment() {

    private val args by navArgs<IsiRiwayatFragmentArgs>()
    private lateinit var moodEntryViewModel: MoodEntryViewModel
    private lateinit var binding: FragmentIsiRiwayatBinding
    private var currentMoodEntry: MoodEntry? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIsiRiwayatBinding.inflate(inflater, container, false)

        moodEntryViewModel = ViewModelProvider(requireActivity()).get(MoodEntryViewModel::class.java)

        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.GONE

        currentMoodEntry = args.moodEntry
        updateUI(currentMoodEntry!!)

        // 🔹 Observer untuk memantau perubahan data di Room
        moodEntryViewModel.readAllData.observe(viewLifecycleOwner) { moodList ->
            val updatedMood = moodList.find { it.id == currentMoodEntry?.id }
            if (updatedMood != null && updatedMood != currentMoodEntry) {
                currentMoodEntry = updatedMood
                updateUI(updatedMood)
            }
        }

        binding.btnDel.setOnClickListener {
            deleteMoodEntry()
        }

        binding.btnEdit.setOnClickListener {
            val action = IsiRiwayatFragmentDirections
                .actionIsiRiwayatFragmentToEditMoodFragment(currentMoodEntry!!)
            findNavController().navigate(action)
        }

        binding.backIsisHistory.setOnClickListener {
            findNavController().popBackStack()
        }

        return binding.root
    }

    private fun deleteMoodEntry() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            currentMoodEntry?.let { moodEntryViewModel.deleteMoodEntry(it) }
            Toast.makeText(requireContext(), "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete this data?")
        builder.setMessage("Are you sure?")
        builder.create().show()
    }

    // 🔹 Fungsi untuk memperbarui UI dengan data terbaru
    private fun updateUI(moodEntry: MoodEntry) {
        binding.tvTitleP.text = MoodUtils.getMoodText(moodEntry.mood)
        binding.txtDate.text = MoodUtils.formatTanggal(moodEntry.tanggal)
        binding.txtTime.text = moodEntry.jam
        binding.tvFeeling.text = moodEntry.perasaan
        binding.tvIsiJurnal.text = moodEntry.jurnal
        binding.tvTitlej.text = moodEntry.judul
        binding.imageView2.setImageResource(moodEntry.activityIcon)
        binding.ivMood.setImageResource(convertMoodToImage(moodEntry.mood))
        binding.tvAktivitasdata.text = moodEntry.activities
    }

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
