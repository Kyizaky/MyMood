package com.example.skripsta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController

class GroundMeditationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_ground_meditation, container, false)

        val goBack = view.findViewById<ImageView>(R.id.ic_go_back)

        goBack.setOnClickListener {
            val action = GroundMeditationFragmentDirections.actionGroundMeditationFragmentToKegiatanFragment()
            findNavController().navigate(action)
        }

        return view

    }




}