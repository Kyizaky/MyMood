package com.example.skripsta

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skripsta.adapter.JournalAdapter
import com.example.skripsta.adapter.RiwayatAdapter
import com.example.skripsta.data.MoodEntryViewModel
import com.example.skripsta.databinding.FragmentRiwayatTanggalBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class RiwayatTanggalFragment : Fragment() {

    private val args by navArgs<RiwayatTanggalFragmentArgs>()
    private lateinit var mMoodEntryViewModel: MoodEntryViewModel
    private lateinit var adapter: JournalAdapter
    private lateinit var binding: FragmentRiwayatTanggalBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRiwayatTanggalBinding.inflate(inflater, container, false)
        mMoodEntryViewModel = ViewModelProvider(this).get(MoodEntryViewModel::class.java)

        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.GONE

        binding.icBack.setOnClickListener {
            val action = RiwayatTanggalFragmentDirections.actionRiwayatTanggalFragmentToHomeFragment()
            findNavController().navigate(action)
        }

        // Konfigurasi RecyclerView
        binding.tvCal.text = formatDateToDayMonth(args.selectedDate)
        adapter = JournalAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        mMoodEntryViewModel.getJournalsByDate( args.selectedDate).observe(viewLifecycleOwner) { moodEntryList ->
            if (moodEntryList.isEmpty()) {
                binding.cvMood.visibility = View.GONE
                binding.tvCal.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.tvNoData.visibility = View.VISIBLE
            } else {
                binding.cvMood.visibility = View.VISIBLE
                binding.tvCal.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.VISIBLE
                binding.tvNoData.visibility = View.GONE
                adapter.submitList(moodEntryList)
            }
        }

        return binding.root
    }

    // Function to format the date to "21 Mei"
    private fun formatDateToDayMonth(dateString: String): String {
        return try {
            val inputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH)
            val date = LocalDate.parse(dateString, inputFormatter)
            val outputFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale.ENGLISH)
            date.format(outputFormatter)
        } catch (e: Exception) {
            dateString
        }
    }
}