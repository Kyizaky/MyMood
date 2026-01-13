package com.example.skripsta

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skripsta.adapter.ActivitySelectionAdapter
import com.example.skripsta.data.entity.Activity
import com.example.skripsta.viewmodel.ActivityViewModel
import com.example.skripsta.databinding.FragmentSelectActivityBinding

class SelectActivityFragment : Fragment() {

    // Binding untuk mengakses view pada layout fragment
    private var _binding: FragmentSelectActivityBinding? = null
    private val binding get() = _binding!!

    // Adapter untuk RecyclerView pemilihan aktivitas
    private lateinit var adapter: ActivitySelectionAdapter

    // ViewModel untuk mengambil data aktivitas dari database
    private lateinit var activityViewModel: ActivityViewModel

    // SharedPreferences untuk menyimpan aktivitas yang dipilih pengguna
    private lateinit var sharedPreferences: SharedPreferences

    // List aktivitas yang akan ditampilkan
    private val activities = mutableListOf<Activity>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inisialisasi view binding
        _binding = FragmentSelectActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi SharedPreferences
        sharedPreferences =
            requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        // Inisialisasi ViewModel
        activityViewModel =
            ViewModelProvider(this).get(ActivityViewModel::class.java)

        // Observasi data aktivitas dari database
        activityViewModel.allActivities.observe(viewLifecycleOwner) { activitiesList ->
            activities.clear()
            activities.addAll(activitiesList)
            setupRecyclerView()
        }

        // Tombol apply untuk menyimpan pilihan aktivitas
        binding.applyButton.setOnClickListener {

            val selectedNames = adapter.getSelectedNames()

            // Validasi jumlah aktivitas yang dipilih harus 5
            if (selectedNames.size != 5) {
                Toast.makeText(
                    context,
                    "Please select exactly 5 activities",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Simpan aktivitas yang dipilih
            saveSelectedActivities(selectedNames)

            // Kembali ke fragment sebelumnya
            findNavController().popBackStack()
        }

        // Floating Action Button untuk menambah aktivitas baru
        binding.fabActivity.setOnClickListener {
            val action =
                SelectActivityFragmentDirections
                    .actionSelectActivityFragmentToActivityFragment()
            findNavController().navigate(action)
        }

        // Tombol batal untuk kembali tanpa menyimpan perubahan
        binding.cancelButton.setOnClickListener {
            findNavController().popBackStack()
        }

    }

    // Menyiapkan RecyclerView beserta adapter
    private fun setupRecyclerView() {

        // Mengambil aktivitas yang sebelumnya sudah dipilih
        val selectedNames =
            sharedPreferences.getStringSet(
                "selected_activity_names",
                emptySet()
            ) ?: emptySet()

        // Inisialisasi adapter dengan data aktivitas dan data terpilih
        adapter =
            ActivitySelectionAdapter(
                activities,
                selectedNames
            ) { selectedNames ->
                saveSelectedActivities(selectedNames)
            }

        // Setup RecyclerView
        binding.recyclerViewSelection.layoutManager =
            LinearLayoutManager(context)
        binding.recyclerViewSelection.adapter = adapter
    }

    // Menyimpan nama aktivitas yang dipilih ke SharedPreferences
    private fun saveSelectedActivities(selectedNames: List<String>) {
        sharedPreferences.edit()
            .putStringSet(
                "selected_activity_names",
                selectedNames.toSet()
            )
            .apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Menghindari memory leak dengan menghapus binding
        _binding = null
    }
}
