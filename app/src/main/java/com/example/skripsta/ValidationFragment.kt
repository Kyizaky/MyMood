package com.example.skripsta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.fragment.navArgs
import kotlin.getValue

class ValidationFragment : Fragment() {

    private val args: ValidationFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_validation, container, false)

        val moodType = args.moodType // dapetin data dari TambahFragment
        val imageView = view.findViewById<ImageView>(R.id.imageViewMood)

        when (moodType) {
            1 -> imageView.setImageResource(R.drawable.activity1)
            2 -> imageView.setImageResource(R.drawable.activity2)
            3 -> imageView.setImageResource(R.drawable.activity3)
            4 -> imageView.setImageResource(R.drawable.activity4)
            5 -> imageView.setImageResource(R.drawable.activity5)
            6 -> imageView.setImageResource(R.drawable.activity6)
            else -> imageView.setImageResource(R.drawable.ic_mood)
        }

        return view
    }
}