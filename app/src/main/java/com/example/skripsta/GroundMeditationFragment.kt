package com.example.skripsta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.fragment.findNavController

class GroundMeditationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate layout fragment ground meditation
        val view =
            inflater.inflate(R.layout.fragment_ground_meditation, container, false)

        // Inisialisasi tombol kembali
        val goBack =
            view.findViewById<ImageView>(R.id.ic_go_back)

        // Aksi ketika tombol kembali ditekan
        goBack.setOnClickListener {

            // Navigasi kembali ke KegiatanFragment
            val action =
                GroundMeditationFragmentDirections
                    .actionGroundMeditationFragmentToKegiatanFragment()

            findNavController().navigate(action)
        }

        return view
    }
}
