package com.example.skripsta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlin.getValue

class ValidationFragment : Fragment() {

    private val args: ValidationFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_validation, container, false)

        val moodType = args.moodType
        val imagemood = view.findViewById<ImageView>(R.id.imageViewMood)
        val imageline1 = view.findViewById<ImageView>(R.id.line1)
        val imageline2 = view.findViewById<ImageView>(R.id.line2)
        val imagebigline = view.findViewById<ImageView>(R.id.bigline)
        val iv_word = view.findViewById<ImageView>(R.id.iv_word)
        val btn_back = view.findViewById<ImageButton>(R.id.ic_back)

        btn_back.setOnClickListener {
            val action = ValidationFragmentDirections.actionValidationFragmentToHomeFragment()
            findNavController().navigate(action)
        }
        when (moodType) {
            1 -> {
                imagemood.setImageResource(R.drawable.valid5)
                imageline1.setImageResource(R.drawable.line5)
                imageline2.setImageResource(R.drawable.line5)
                imagebigline.setImageResource(R.drawable.bigline5)
                iv_word.setImageResource(R.drawable.word5)
            }
            2 -> {
                imagemood.setImageResource(R.drawable.valid4)
                imageline1.setImageResource(R.drawable.line4)
                imageline2.setImageResource(R.drawable.line4)
                imagebigline.setImageResource(R.drawable.bigline4)
                iv_word.setImageResource(R.drawable.word4)
            }
            3 -> {
                imagemood.setImageResource(R.drawable.valid2)
                imageline1.setImageResource(R.drawable.line2)
                imageline2.setImageResource(R.drawable.line2)
                imagebigline.setImageResource(R.drawable.bigline2)
                iv_word.setImageResource(R.drawable.word2)
            }
            4 -> {
                imagemood.setImageResource(R.drawable.valid3)
                imageline1.setImageResource(R.drawable.line3)
                imageline2.setImageResource(R.drawable.line3)
                imagebigline.setImageResource(R.drawable.bigline3)
                iv_word.setImageResource(R.drawable.word3)
            }
            5 -> {
                imagemood.setImageResource(R.drawable.valid1)
                imageline1.setImageResource(R.drawable.line1)
                imageline2.setImageResource(R.drawable.line1)
                imagebigline.setImageResource(R.drawable.bigline1)
                iv_word.setImageResource(R.drawable.word1)
            }
            6 -> {
                imagemood.setImageResource(R.drawable.valid6)
                imageline1.setImageResource(R.drawable.line6)
                imageline2.setImageResource(R.drawable.line6)
                imagebigline.setImageResource(R.drawable.bigline6)
                iv_word.setImageResource(R.drawable.word6)
            }
            else -> imagemood.setImageResource(R.drawable.ic_mood)
        }

        return view
    }
}