package com.example.skripsta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skripsta.adapter.JournalAdapter
import com.example.skripsta.data.UserViewModel
import com.example.skripsta.databinding.FragmentHomeBinding
import com.example.skripsta.databinding.FragmentRiwayatTanggalBinding

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

        // Konfigurasi RecyclerView
        adapter = JournalAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Ambil data berdasarkan tanggal yang dipilih
        mUserViewModel.getJournalsByDate(args.selectedDate).observe(viewLifecycleOwner) { journalList ->
            if (journalList.isEmpty()) {
                binding.recyclerView.visibility = View.GONE
                binding.tvNoData.visibility = View.VISIBLE
            } else {
                binding.recyclerView.visibility = View.VISIBLE
                binding.tvNoData.visibility = View.GONE
                adapter.submitList(journalList)
            }
        }

        return binding.root
    }
}
