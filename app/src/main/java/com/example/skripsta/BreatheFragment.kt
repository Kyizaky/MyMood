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

    private lateinit var instructionText: TextView
    private lateinit var circleText: TextView
    private lateinit var circleView: View
    private lateinit var icBack: ImageView
    private lateinit var howTo: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_breathe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        icBack = view.findViewById(R.id.goBack)
        instructionText = view.findViewById(R.id.instructionText)
        circleView = view.findViewById(R.id.circleView)
        circleText = view.findViewById(R.id.circleText)
        howTo = view.findViewById(R.id.howTo)

        icBack.setOnClickListener {
            findNavController().navigateUp()
        }

        howTo.setOnClickListener {
            showCustomDialog()
        }

        circleView.setOnClickListener {
            icBack.visibility = View.INVISIBLE
            howTo.visibility = View.INVISIBLE
            startBreathingCycle()
        }
    }

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

    private fun startBreathingCycle() {
        circleView.isEnabled = false

        circleText.visibility = View.VISIBLE
        animateCircle(1f, 3.5f, 4000, "Inhale", 4) {
            animateCircle(3.5f, 3.5f, 7000, "Hold", 7) {
                animateCircle(3.5f, 1f, 8000, "Exhale", 8) {
                    instructionText.visibility = View.VISIBLE
                    instructionText.text = "Done! Tap the circle to start again."
                    circleText.textSize = 18.toFloat()
                    circleText.text = "Click me!!"
                    icBack.visibility = View.VISIBLE
                    howTo.visibility = View.VISIBLE
                    circleView.isEnabled = true
                }
            }
        }
    }

    private fun animateCircle(
        startScale: Float,
        endScale: Float,
        duration: Long,
        phase: String,
        totalSeconds: Int,
        onEnd: () -> Unit
    ) {
        val animator = ValueAnimator.ofFloat(startScale, endScale)
        animator.duration = duration
        animator.interpolator = LinearInterpolator()

        animator.addUpdateListener { animation ->
            val scale = animation.animatedValue as Float
            circleView.scaleX = scale
            circleView.scaleY = scale

            val elapsedTime = animation.animatedFraction * duration
            val remainingSeconds = (totalSeconds - (elapsedTime / 1000)).toInt().coerceAtLeast(0)
            circleText.textSize = 30.toFloat()
            circleText.text = remainingSeconds.toString()
            instructionText.text = phase
        }

        animator.doOnEnd { onEnd() }
        animator.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}