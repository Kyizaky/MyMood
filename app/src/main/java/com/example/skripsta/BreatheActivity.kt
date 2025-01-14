package com.example.skripsta

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd

class BreatheActivity : AppCompatActivity() {

    private lateinit var instructionText: TextView
    private lateinit var circleText: TextView
    private lateinit var circleView: View
    private lateinit var icBack: ImageView
    private lateinit var howTo: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_breathe)

        hideSystemUI()

        icBack = findViewById(R.id.goBack)
        instructionText = findViewById(R.id.instructionText)
        circleView = findViewById(R.id.circleView)
        circleText = findViewById(R.id.circleText)
        circleText.visibility = View.INVISIBLE
        howTo = findViewById(R.id.howTo)

        icBack.setOnClickListener{
            onBackPressed()
        }

        howTo.setOnClickListener{
            showCustomDialog()
        }

        circleView.setOnClickListener {
            icBack.visibility = View.INVISIBLE
            howTo.visibility = View.INVISIBLE
            startBreathingCycle()
        }
    }

    private fun hideSystemUI() {
        // Sembunyikan navbar dan status bar
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )
    }

    private fun showCustomDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Androidly Alert")
        builder.setMessage("We have a message")

        builder.setPositiveButton(android.R.string.yes) { dialog, which ->

        }
        builder.show()
    }

    private fun startBreathingCycle() {
        circleView.isEnabled = false // Disable interaction during the cycle

        // Tarik napas (4 detik)
        circleText.visibility = View.VISIBLE
        instructionText.visibility = View.INVISIBLE
        circleText.text = "Inhale"
        animateCircle(1f, 9.5f, 4000) {
            // Tahan napas (7 detik)
            circleText.text = "Hold"
            animateCircle(9.5f, 9.5f, 7000) {
                // Hembuskan napas (8 detik)
                circleText.text = "Exhale"
                animateCircle(9.5f, 1f, 8000) {
                    // Selesai
                    instructionText.visibility = View.VISIBLE
                    instructionText.text = "Selesai! Klik lingkaran untuk memulai lagi."
                    circleText.visibility = View.INVISIBLE
                    icBack.visibility = View.VISIBLE
                    howTo.visibility = View.VISIBLE
                    circleView.isEnabled = true
                }
            }
        }
    }

    private fun animateCircle(startScale: Float, endScale: Float, duration: Long, onEnd: () -> Unit) {
        val animator = ValueAnimator.ofFloat(startScale, endScale)
        animator.duration = duration
        animator.interpolator = LinearInterpolator()

        animator.addUpdateListener { animation ->
            val scale = animation.animatedValue as Float
            circleView.scaleX = scale
            circleView.scaleY = scale
        }

        animator.doOnEnd { onEnd() }
        animator.start()
    }

    override fun onBackPressed() {

        super.onBackPressed()  // Navigasi kembali ke activity sebelumnya
    }
}