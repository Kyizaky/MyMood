package com.example.skripsta

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlin.random.Random

class ValidationFragment : Fragment() {

    // Mengambil argumen navigasi (moodType) dari fragment sebelumnya
    private val args: ValidationFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate layout fragment_validation
        val view = inflater.inflate(R.layout.fragment_validation, container, false)

        // Mengambil nilai moodType dari argument
        val moodType = args.moodType

        // Inisialisasi komponen UI
        val imagemood = view.findViewById<ImageView>(R.id.imageViewMood)
        val imageline1 = view.findViewById<ImageView>(R.id.line1)
        val imageline2 = view.findViewById<ImageView>(R.id.line2)
        val imagebigline = view.findViewById<ImageView>(R.id.bigline)
        val ivWord = view.findViewById<TextView>(R.id.iv_word)
        val btnBack = view.findViewById<ImageButton>(R.id.ic_back)
        val tvPrompt = view.findViewById<TextView>(R.id.tvMoodPrompt)

        // Aksi tombol back → kembali ke HomeFragment
        btnBack.setOnClickListener {
            val navController = findNavController()

            // Menghapus ValidationFragment dari back stack
            val navOptions = androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.validationFragment, true)
                .build()

            navController.navigate(
                ValidationFragmentDirections.actionValidationFragmentToHomeFragment(),
                navOptions
            )
        }

        // Menyesuaikan gambar berdasarkan moodType
        when (moodType) {
            1 -> {
                imagemood.setImageResource(R.drawable.valid5)
                imageline1.setImageResource(R.drawable.line5)
                imageline2.setImageResource(R.drawable.line5)
                imagebigline.setImageResource(R.drawable.bigline5)
            }
            2 -> {
                imagemood.setImageResource(R.drawable.valid4)
                imageline1.setImageResource(R.drawable.line4)
                imageline2.setImageResource(R.drawable.line4)
                imagebigline.setImageResource(R.drawable.bigline4)
            }
            3 -> {
                imagemood.setImageResource(R.drawable.valid2)
                imageline1.setImageResource(R.drawable.line2)
                imageline2.setImageResource(R.drawable.line2)
                imagebigline.setImageResource(R.drawable.bigline2)
            }
            4 -> {
                imagemood.setImageResource(R.drawable.valid3)
                imageline1.setImageResource(R.drawable.line3)
                imageline2.setImageResource(R.drawable.line3)
                imagebigline.setImageResource(R.drawable.bigline3)
            }
            5 -> {
                imagemood.setImageResource(R.drawable.valid1)
                imageline1.setImageResource(R.drawable.line1)
                imageline2.setImageResource(R.drawable.line1)
                imagebigline.setImageResource(R.drawable.bigline1)
            }
            else -> imagemood.setImageResource(R.drawable.ic_medi)
        }

        // Daftar kalimat validasi berdasarkan mood
        val wordList = when (moodType) {
            1 -> listOf(
                "It's okay to have bad days.",
                "Take a deep breath, today will be better.",
                "Be gentle with yourself."
            )
            2 -> listOf(
                "Hang in there, things will turn around.",
                "You’ve handled worse before.",
                "A small step forward still counts."
            )
            3 -> listOf(
                "You’re doing fine.",
                "Keep going, steady and calm.",
                "Every day is progress, even the quiet ones."
            )
            4 -> listOf(
                "That’s great! Keep the energy up.",
                "You’re doing awesome today.",
                "Enjoy the little wins!"
            )
            5 -> listOf(
                "Fantastic! You’re on top of the world!",
                "Keep shining and spread that joy!",
                "What a wonderful mood to be in!"
            )
            else -> listOf(
                "How are you feeling today?",
                "Stay mindful.",
                "Take a moment to breathe."
            )
        }

        // Menampilkan satu kalimat secara acak
        ivWord.text = wordList.random()

        // Teks dengan bagian yang bisa diklik
        val fullText = "Feeling off? Click here!!"
        val spannable = SpannableString(fullText)

        // Bagian teks yang dijadikan link
        val clickText = "Click here"
        val start = fullText.indexOf(clickText)
        val end = start + clickText.length

        // Memberi warna biru pada teks link
        spannable.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(requireContext(), R.color.blue)
            ),
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Aksi ketika teks diklik
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val action =
                    ValidationFragmentDirections.actionValidationFragmentToKegiatanFragment()
                findNavController().navigate(action)
            }
        }

        // Menempelkan clickable span ke teks
        spannable.setSpan(
            clickableSpan,
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Menampilkan teks dan mengaktifkan klik
        tvPrompt.text = spannable
        tvPrompt.movementMethod = LinkMovementMethod.getInstance()

        return view
    }
}
