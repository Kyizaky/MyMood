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
import com.example.skripsta.adapter.JournalAdapter
import com.example.skripsta.data.UserViewModel
import com.example.skripsta.databinding.FragmentHomeBinding
import com.example.skripsta.databinding.FragmentRiwayatTanggalBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class RiwayatTanggalFragment : Fragment() {

    private val args by navArgs<RiwayatTanggalFragmentArgs>()
    private lateinit var mUserViewModel: UserViewModel
    private lateinit var adapter: JournalAdapter
    private lateinit var binding: FragmentRiwayatTanggalBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRiwayatTanggalBinding.inflate(inflater, container, false)
        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.GONE

        binding.backIsiDate.setOnClickListener {
            findNavController().popBackStack()
        }

        // Konfigurasi RecyclerView
        binding.tvCal.text = formatDateToDayMonth(args.selectedDate)
        adapter = JournalAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Ambil data berdasarkan tanggal yang dipilih
        mUserViewModel.getJournalsByDate(args.selectedDate).observe(viewLifecycleOwner) { journalList ->
            if (journalList.isEmpty()) {
                binding.cvMood.visibility = View.GONE
                binding.tvCal.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.tvNoData.visibility = View.VISIBLE
            } else {
                binding.cvMood.visibility = View.VISIBLE
                binding.tvCal.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.VISIBLE
                binding.tvNoData.visibility = View.GONE
                adapter.submitList(journalList)
            }
        }

        return binding.root
    }

    // Function to format the date to "21 Mei"
    private fun formatDateToDayMonth(dateString: String): String {
        return try {
            // Define the input format based on your date string (adjust if needed)
            val inputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.getDefault())
            // Parse the input date string
            val date = LocalDate.parse(dateString, inputFormatter)
            // Define the output format (day and month name in Indonesian)
            val outputFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale("in", "ID"))
            // Format the date to "21 Mei"
            date.format(outputFormatter)
        } catch (e: Exception) {
            // Fallback in case the date string is invalid
            dateString
        }
    }
}
