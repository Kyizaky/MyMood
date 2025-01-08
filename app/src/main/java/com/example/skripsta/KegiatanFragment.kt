package com.example.skripsta

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView

class KegiatanFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_kegiatan, container, false)

        // Find CardViews by ID
        val cardMeditation = view.findViewById<CardView>(R.id.meditation)
        val cardBreathe = view.findViewById<CardView>(R.id.breathe)

        // Set OnClickListener for CardView 1
        cardMeditation.setOnClickListener {
            val intent = Intent(activity, PreMeditationActivity::class.java)
            intent.putExtra("fragment", "A")
            startActivity(intent)
        }

        // Set OnClickListener for CardView 2
        cardBreathe.setOnClickListener {
            val intent = Intent(activity, PreMeditationActivity::class.java)
            intent.putExtra("fragment", "B")
            startActivity(intent)
        }

        return view
    }
}
