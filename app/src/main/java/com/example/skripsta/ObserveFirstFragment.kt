package com.example.skripsta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.fragment.findNavController

class ObserveFirstFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate layout fragment observe pertama
        val view = inflater.inflate(R.layout.fragment_observe_first, container, false)

        // Menghubungkan ImageView tombol kembali dari layout
        val goBack = view.findViewById<ImageView>(R.id.ic_go_back)

        // Listener klik tombol kembali
        goBack.setOnClickListener {

            // Aksi navigasi dari ObserveFirstFragment ke KegiatanFragment
            val action =
                ObserveFirstFragmentDirections
                    .actionObserveFirstFragmentToKegiatanFragment()

            // Menjalankan navigasi menggunakan NavController
            findNavController().navigate(action)
        }

        // Mengembalikan view fragment
        return view
    }

}
