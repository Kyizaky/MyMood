package com.example.skripsta

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.adapter.FeelingListAdapter
import com.example.skripsta.viewmodel.FeelingViewModel
import kotlinx.coroutines.runBlocking

class FeelingFragment : Fragment() {

    // ViewModel untuk mengelola data feeling
    private lateinit var mFeelingViewModel: FeelingViewModel

    // SharedPreferences untuk menyimpan feeling yang sedang dipilih user
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate layout fragment
        val view = inflater.inflate(R.layout.fragment_feeling, container, false)

        // Inisialisasi ViewModel
        mFeelingViewModel = ViewModelProvider(this).get(FeelingViewModel::class.java)

        // Inisialisasi SharedPreferences
        sharedPreferences =
            requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        // Setup RecyclerView untuk menampilkan daftar feeling
        val recyclerView =
            view.findViewById<RecyclerView>(R.id.recycler_view_feelings)

        // Inisialisasi adapter dengan aksi edit dan delete
        val adapter = FeelingListAdapter(

            // Aksi ketika tombol edit diklik
            onEditClick = { feeling ->
                val action =
                    FeelingFragmentDirections
                        .actionFeelingFragmentToFragmentAddFeeling(
                            feelingId = feeling.id,
                            feelingName = feeling.name
                        )
                findNavController().navigate(action)
            },

            // Aksi ketika tombol delete diklik
            onDeleteClick = { feeling ->

                // Ambil daftar feeling yang sedang dipilih user
                val selectedNames =
                    sharedPreferences.getStringSet(
                        "selected_feeling_names",
                        emptySet()
                    ) ?: emptySet()

                // Ambil jumlah total feeling dari database
                val feelingCount =
                    runBlocking { mFeelingViewModel.getFeelingCount() }

                // Validasi sebelum menghapus feeling
                when {
                    // Minimal harus ada 5 feeling
                    feelingCount <= 5 -> {
                        Toast.makeText(
                            requireContext(),
                            "Cannot delete feeling, minimum 5 feelings required",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    // Feeling tidak boleh dihapus jika sedang dipilih
                    feeling.name in selectedNames -> {
                        Toast.makeText(
                            requireContext(),
                            "Cannot delete feeling '${feeling.name}' as it is currently selected",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    // Jika lolos validasi, hapus feeling
                    else -> {
                        mFeelingViewModel.deleteFeeling(feeling)
                        Toast.makeText(
                            requireContext(),
                            "Feeling '${feeling.name}' deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )

        // Set adapter dan layout manager RecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        // Observe data feeling dari ViewModel
        mFeelingViewModel.allFeelings.observe(viewLifecycleOwner) { feelings ->
            adapter.submitList(feelings)
        }

        // Tombol kembali ke halaman sebelumnya
        view.findViewById<ImageView>(R.id.ic_back).setOnClickListener {
            findNavController().popBackStack()
        }

        // Tombol untuk menambah feeling baru
        view.findViewById<Button>(R.id.btn_add_feeling).setOnClickListener {
            findNavController()
                .navigate(R.id.action_feelingFragment_to_fragmentAddFeeling)
        }

        return view
    }
}
