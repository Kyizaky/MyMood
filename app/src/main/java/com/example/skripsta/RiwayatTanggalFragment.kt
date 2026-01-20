package com.example.skripsta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skripsta.adapter.MoodHistoryAdapter
import com.example.skripsta.viewmodel.MoodEntryViewModel
import com.example.skripsta.databinding.FragmentRiwayatTanggalBinding
import com.example.skripsta.utils.MoodUtils

class RiwayatTanggalFragment : Fragment() {

    private val args by navArgs<RiwayatTanggalFragmentArgs>()
    private lateinit var mMoodEntryViewModel: MoodEntryViewModel
    private lateinit var adapter: MoodHistoryAdapter
    private lateinit var binding: FragmentRiwayatTanggalBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRiwayatTanggalBinding.inflate(inflater, container, false)
        mMoodEntryViewModel = ViewModelProvider(this).get(MoodEntryViewModel::class.java)

        // Sembunyikan bottom navigation
        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.GONE

        // Tombol back ke Home
        binding.icBack.setOnClickListener {
            findNavController().popBackStack(
                R.id.homeFragment,
                false
            )
        }

        // Format tanggal di title
        binding.tvCal.text = MoodUtils.formatTanggal(args.selectedDate)

        // Inisialisasi adapter baru dengan fungsi klik
        adapter = MoodHistoryAdapter { moodEntry ->
            val action = RiwayatTanggalFragmentDirections
                .actionRiwayatTanggalFragmentToIsiRiwayatFragment(moodEntry)
            findNavController().navigate(action)
        }

        // Set RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Observasi data dari ViewModel
        mMoodEntryViewModel.getJournalsByDate(args.selectedDate).observe(viewLifecycleOwner) { moodEntryList ->
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
}
