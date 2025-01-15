package com.example.skripsta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.data.Item

class TambahFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_tambah, container, false)

        // Sembunyikan BottomNavigationView
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNavigationView)
        bottomNav.visibility = View.GONE

        val goBack: ImageView = view.findViewById(R.id.ic_back)
        goBack.setOnClickListener{

            parentFragmentManager.popBackStack()
        }

        // Inisialisasi RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)

        // Data untuk item grid
        val items = listOf(
            Item(R.drawable.ic_bekerja, "Family"),
            Item(R.drawable.ic_belajar, "Friends"),
            Item(R.drawable.ic_belanja, "Beloved"),
            Item(R.drawable.ic_makan, "Colleague"),
            Item(R.drawable.ic_olahraga, "Stranger"),
            Item(R.drawable.ic_renang, "Party"),
            Item(R.drawable.ic_riwayat, "Dating"),
            Item(R.drawable.ic_mood, "Traveling")
        )

        // Set Adapter dan LayoutManager
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 4) // 4 kolom
        recyclerView.adapter = ItemAdapter(items)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Tampilkan kembali BottomNavigationView
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNavigationView)
        bottomNav.visibility = View.VISIBLE
    }
}
