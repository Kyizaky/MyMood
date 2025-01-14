package com.example.skripsta

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.skripsta.databinding.ActivityMeditationBinding
import me.tankery.lib.circularseekbar.CircularSeekBar

class MeditationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMeditationBinding
    private lateinit var mediaPlayer: MediaPlayer
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeditationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        binding.goBack.setOnClickListener {
            resetAudio()
            onBackPressed()
        }

        binding.howTo.setOnClickListener {
            showCustomDialog()
        }

        // Initialize MediaPlayer with a sample audio file
        mediaPlayer = MediaPlayer.create(this, R.raw.music)

        // Set up CircularSeekBar listener
        binding.seekBar.setOnSeekBarChangeListener(object : CircularSeekBar.OnCircularSeekBarChangeListener {
            override fun onProgressChanged(
                circularSeekBar: CircularSeekBar?,
                progress: Float,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    val duration = mediaPlayer.duration
                    val newPosition = (progress / 100 * duration).toInt()
                    mediaPlayer.seekTo(newPosition)
                }
            }

            override fun onStartTrackingTouch(seekBar: CircularSeekBar?) {
                // Optional: Handle when user starts interacting with the seek bar
            }

            override fun onStopTrackingTouch(seekBar: CircularSeekBar?) {
                // Optional: Handle when user stops interacting with the seek bar
            }
        })

        // Set up play/pause button listener
        binding.playPauseButton.setOnClickListener {
            if (isPlaying) {
                pauseAudio()
            } else {
                playAudio()
            }
        }
        // Set up reset button listener
        binding.resetButton.setOnClickListener {
            resetAudio()
        }

        // Update seek bar progress while audio is playing
        mediaPlayer.setOnPreparedListener {
            updateSeekBar()
        }

        mediaPlayer.setOnCompletionListener {
            resetAudio()
        }
    }

    private fun showCustomDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Androidly Alert")
        builder.setMessage("We have a message")

        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            
        }
        builder.show()
    }

    private fun playAudio() {
        mediaPlayer.start()
        isPlaying = true
        binding.playPauseButton.setImageResource(R.drawable.ic_pause) // Ganti ikon menjadi pause
        updateSeekBar()
    }

    private fun pauseAudio() {
        mediaPlayer.pause()
        isPlaying = false
        binding.playPauseButton.setImageResource(R.drawable.ic_play) // Ganti ikon menjadi play
    }

    private fun resetAudio() {
        mediaPlayer.seekTo(0)
        binding.seekBar.progress = 0f
        pauseAudio()
    }

    private fun updateSeekBar() {
        if (mediaPlayer.isPlaying) {
            val progress = (mediaPlayer.currentPosition / mediaPlayer.duration.toFloat()) * 100
            binding.seekBar.progress = progress

            binding.currentTimeText.text = formatTime(mediaPlayer.currentPosition)
            binding.seekBar.postDelayed({ updateSeekBar() }, 1000 / 600)
        }
    }
    override fun onBackPressed() {
        if (mediaPlayer.isPlaying) {
            // Pause lagu sebelum kembali ke activity sebelumnya
            mediaPlayer.reset()
            isPlaying = false
        }
        super.onBackPressed()  // Navigasi kembali ke activity sebelumnya
    }

    private fun formatTime(milliseconds: Int): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / 1000) / 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }


}