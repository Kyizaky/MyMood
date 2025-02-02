package com.example.skripsta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.skripsta.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Menangani event ketika tanggal dipilih
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%02d/%02d/%d", month + 1, dayOfMonth, year) // Format MM/dd/yyyy

            // Navigasi ke HistoryFragment dengan mengirimkan tanggal yang dipilih
            val action = HomeFragmentDirections.actionHomeFragmentToRiwayatTanggalFragment(selectedDate)
            findNavController().navigate(action)
        }

        return binding.root
    }

}