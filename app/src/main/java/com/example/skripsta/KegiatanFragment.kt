package com.example.skripsta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController

class KegiatanFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate layout fragment_kegiatan
        val view = inflater.inflate(R.layout.fragment_kegiatan, container, false)

        // Menghubungkan CardView dengan ID pada layout
        val cardMeditation = view.findViewById<CardView>(R.id.meditation)
        val cardBreathe = view.findViewById<CardView>(R.id.breathe)
        val cardGround = view.findViewById<CardView>(R.id.medi_ground)
        val cardObserve = view.findViewById<CardView>(R.id.medi_observe)

        // Navigasi ke halaman Meditation
        cardMeditation.setOnClickListener {
            val action = KegiatanFragmentDirections.actionKegiatanFragmentToMeditationFragment()
            findNavController().navigate(action)
        }

        // Navigasi ke halaman Breathe
        cardBreathe.setOnClickListener {
            val action = KegiatanFragmentDirections.actionKegiatanFragmentToBreatheFragment()
            findNavController().navigate(action)
        }

        // Navigasi ke halaman Ground Meditation
        cardGround.setOnClickListener {
            val action = KegiatanFragmentDirections.actionKegiatanFragmentToGroundMeditationFragment()
            findNavController().navigate(action)
        }

        // Navigasi ke halaman Observe
        cardObserve.setOnClickListener {
            val action = KegiatanFragmentDirections.actionKegiatanFragmentToObserveFirstFragment()
            findNavController().navigate(action)
        }

        // Mengembalikan view fragment
        return view
    }
}
