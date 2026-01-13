package com.example.skripsta

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class BreatheFragment : Fragment() {

    // Text untuk instruksi fase napas (Inhale, Hold, Exhale)
    private lateinit var instructionText: TextView

    // Text di tengah lingkaran untuk hitung mundur
    private lateinit var circleText: TextView

    // View lingkaran yang dianimasikan
    private lateinit var circleView: View

    // Icon tombol kembali
    private lateinit var icBack: ImageView

    // Icon bantuan / panduan
    private lateinit var howTo: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout fragment breathe
        return inflater.inflate(R.layout.fragment_breathe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi view dari layout
        icBack = view.findViewById(R.id.goBack)
        instructionText = view.findViewById(R.id.instructionText)
        circleView = view.findViewById(R.id.circleView)
        circleText = view.findViewById(R.id.circleText)
        howTo = view.findViewById(R.id.howTo)

        // Navigasi kembali ke halaman sebelumnya
        icBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Menampilkan dialog panduan pernapasan
        howTo.setOnClickListener {
            showCustomDialog()
        }

        // Memulai siklus pernapasan saat lingkaran ditekan
        circleView.setOnClickListener {
            icBack.visibility = View.INVISIBLE
            howTo.visibility = View.INVISIBLE
            startBreathingCycle()
        }
    }

    // Menampilkan dialog panduan teknik pernapasan 4-7-8
    private fun showCustomDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Breathing Guide")
        builder.setMessage("""
        The 4-7-8 breathing exercise helps you relax and feel calm. Follow these simple steps:

            1. Inhale (4 seconds):
               - Breathe in through your nose for 4 seconds.
               - Watch the circle grow.

            2. Hold (7 seconds):
               - Hold your breath for 7 seconds.
               - The circle stays still.

            3. Exhale (8 seconds):
               - Breathe out slowly through your mouth for 8 seconds.
               - The circle shrinks.
            """)
        builder.setPositiveButton("Got it") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    // Memulai satu siklus pernapasan lengkap (Inhale → Hold → Exhale)
    private fun startBreathingCycle() {

        // Menonaktifkan klik selama animasi berjalan
        circleView.isEnabled = false

        // Menampilkan teks countdown
        circleText.visibility = View.VISIBLE

        // Animasi fase Inhale
        animateCircle(1f, 3.5f, 4000, "Inhale", 4) {

            // Animasi fase Hold
            animateCircle(3.5f, 3.5f, 7000, "Hold", 7) {

                // Animasi fase Exhale
                animateCircle(3.5f, 1f, 8000, "Exhale", 8) {

                    // Menampilkan pesan selesai
                    instructionText.visibility = View.VISIBLE
                    instructionText.text = "Done! Tap the circle to start again."

                    // Mengatur ulang teks lingkaran
                    circleText.textSize = 18.toFloat()
                    circleText.text = "Click me!!"

                    // Menampilkan kembali tombol navigasi
                    icBack.visibility = View.VISIBLE
                    howTo.visibility = View.VISIBLE

                    // Mengaktifkan kembali klik lingkaran
                    circleView.isEnabled = true
                }
            }
        }
    }

    // Fungsi animasi lingkaran untuk setiap fase pernapasan
    private fun animateCircle(
        startScale: Float,
        endScale: Float,
        duration: Long,
        phase: String,
        totalSeconds: Int,
        onEnd: () -> Unit
    ) {
        // Animator untuk perubahan skala lingkaran
        val animator = ValueAnimator.ofFloat(startScale, endScale)
        animator.duration = duration
        animator.interpolator = LinearInterpolator()

        // Update animasi setiap frame
        animator.addUpdateListener { animation ->
            val scale = animation.animatedValue as Float

            // Mengatur skala lingkaran
            circleView.scaleX = scale
            circleView.scaleY = scale

            // Menghitung sisa waktu
            val elapsedTime = animation.animatedFraction * duration
            val remainingSeconds =
                (totalSeconds - (elapsedTime / 1000)).toInt().coerceAtLeast(0)

            // Menampilkan hitung mundur
            circleText.textSize = 30.toFloat()
            circleText.text = remainingSeconds.toString()

            // Menampilkan instruksi fase saat ini
            instructionText.text = phase
        }

        // Callback ketika animasi selesai
        animator.doOnEnd { onEnd() }

        // Memulai animasi
        animator.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Tidak ada resource khusus yang perlu dibersihkan
    }
}
