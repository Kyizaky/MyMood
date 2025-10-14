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
        val view = inflater.inflate(R.layout.fragment_kegiatan, container, false)

        val cardMeditation = view.findViewById<CardView>(R.id.meditation)
        val cardBreathe = view.findViewById<CardView>(R.id.breathe)
        val cardGround = view.findViewById<CardView>(R.id.medi_ground)
        val cardObserve = view.findViewById<CardView>(R.id.medi_observe)

        cardMeditation.setOnClickListener {
            val action = KegiatanFragmentDirections.actionKegiatanFragmentToMeditationFragment()
            findNavController().navigate(action)
        }

        cardBreathe.setOnClickListener {
            val action = KegiatanFragmentDirections.actionKegiatanFragmentToBreatheFragment()
            findNavController().navigate(action)
        }

        cardGround.setOnClickListener {
            val action = KegiatanFragmentDirections.actionKegiatanFragmentToGroundMeditationFragment()
            findNavController().navigate(action)
        }
        
        cardObserve.setOnClickListener { 
            val action = KegiatanFragmentDirections.actionKegiatanFragmentToObserveFirstFragment()
            findNavController().navigate(action)
        }
        return view
    }
}
