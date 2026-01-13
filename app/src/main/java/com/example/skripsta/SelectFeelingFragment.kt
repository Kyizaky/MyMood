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
import com.example.skripsta.adapter.FeelingSelectionAdapter
import com.example.skripsta.data.entity.Feeling
import com.example.skripsta.viewmodel.FeelingViewModel
import com.example.skripsta.databinding.FragmentSelectFeelingBinding

class SelectFeelingFragment : Fragment() {

    // Binding untuk mengakses view pada layout fragment
    private var _binding: FragmentSelectFeelingBinding? = null
    private val binding get() = _binding!!

    // Adapter untuk RecyclerView pemilihan perasaan
    private lateinit var adapter: FeelingSelectionAdapter

    // ViewModel untuk mengelola data perasaan dari database
    private lateinit var feelingViewModel: FeelingViewModel

    // SharedPreferences untuk menyimpan perasaan yang dipilih pengguna
    private lateinit var sharedPreferences: SharedPreferences

    // List perasaan yang akan ditampilkan
    private val feelings = mutableListOf<Feeling>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inisialisasi view binding
        _binding = FragmentSelectFeelingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi SharedPreferences
        sharedPreferences =
            requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        // Inisialisasi ViewModel
        feelingViewModel =
            ViewModelProvider(this).get(FeelingViewModel::class.java)

        // Mengamati data perasaan dari database
        feelingViewModel.allFeelings.observe(viewLifecycleOwner) { feelingsList ->
            feelings.clear()
            feelings.addAll(feelingsList)
            setupRecyclerView()
        }

        // Tombol apply untuk menyimpan perasaan yang dipilih
        binding.applyButton.setOnClickListener {

            val selectedNames = adapter.getSelectedNames()

            // Validasi jumlah perasaan yang dipilih harus 5
            if (selectedNames.size != 5) {
                Toast.makeText(
                    context,
                    "Please select exactly 5 feelings",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Simpan perasaan yang dipilih
            saveSelectedFeelings(selectedNames)

            // Kembali ke fragment sebelumnya
            findNavController().popBackStack()
        }

        // Floating Action Button untuk menambah perasaan baru
        binding.fabFeeling.setOnClickListener {
            val action =
                SelectFeelingFragmentDirections
                    .actionSelectFeelingFragmentToFeelingFragment()
            findNavController().navigate(action)
        }

        // Tombol batal untuk kembali tanpa menyimpan perubahan
        binding.cancelButton.setOnClickListener {
            findNavController().popBackStack()
        }

    }

    // Menyiapkan RecyclerView beserta adapter
    private fun setupRecyclerView() {

        // Mengambil perasaan yang sebelumnya sudah dipilih
        val selectedNames =
            sharedPreferences.getStringSet(
                "selected_feeling_names",
                emptySet()
            ) ?: emptySet()

        // Inisialisasi adapter dengan data perasaan dan data terpilih
        adapter =
            FeelingSelectionAdapter(
                feelings,
                selectedNames
            ) { selectedNames ->
                saveSelectedFeelings(selectedNames)
            }

        // Setup RecyclerView
        binding.recyclerViewSelection.layoutManager =
            LinearLayoutManager(context)
        binding.recyclerViewSelection.adapter = adapter
    }

    // Menyimpan nama perasaan yang dipilih ke SharedPreferences
    private fun saveSelectedFeelings(selectedNames: List<String>) {
        sharedPreferences.edit()
            .putStringSet(
                "selected_feeling_names",
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
