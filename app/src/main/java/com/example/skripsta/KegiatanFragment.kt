package com.example.skripsta

import android.content.Intent
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

        cardMeditation.setOnClickListener {
            val intent = Intent(activity, MeditationActivity::class.java)
            startActivity(intent)
        }

        cardBreathe.setOnClickListener {
            val intent = Intent(activity, BreatheActivity::class.java)
            startActivity(intent)
        }

        cardGround.setOnClickListener {
            val action = KegiatanFragmentDirections.actionKegiatanFragmentToGroundMeditationFragment()
            findNavController().navigate(action)
        }


        return view
    }
}
