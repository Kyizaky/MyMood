package com.example.skripsta

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.skripsta.databinding.FragmentMeditationBinding
import me.tankery.lib.circularseekbar.CircularSeekBar

class MeditationFragment : Fragment() {
    private var _binding: FragmentMeditationBinding? = null
    private val binding get() = _binding!!
    private lateinit var mediaPlayer: MediaPlayer
    private var isPlaying = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeditationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fullscreen immersive mode
        requireActivity().window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        // Tombol kembali
        binding.goBack.setOnClickListener {
            resetAudio()
            requireActivity().onBackPressed()
        }

        // Tombol petunjuk
        binding.howTo.setOnClickListener {
            showCustomDialog()
        }

        // Initialize MediaPlayer
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.music)

        // Listener seekbar melingkar
        binding.seekBar.setOnSeekBarChangeListener(object :
            CircularSeekBar.OnCircularSeekBarChangeListener {
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

            override fun onStartTrackingTouch(seekBar: CircularSeekBar?) {}
            override fun onStopTrackingTouch(seekBar: CircularSeekBar?) {}
        })

        // Tombol play/pause
        binding.playPauseButton.setOnClickListener {
            if (isPlaying) pauseAudio() else playAudio()
        }

        // Tombol reset
        binding.resetButton.setOnClickListener {
            resetAudio()
        }

        // Update seekbar selama audio berjalan
        mediaPlayer.setOnPreparedListener {
            updateSeekBar()
        }

        // Reset setelah selesai
        mediaPlayer.setOnCompletionListener {
            resetAudio()
        }
    }

    private fun showCustomDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Meditation Guide")
        builder.setMessage("""
           Mindfulness meditation helps you stay calm and focused. Follow these simple steps for a quick session.

            Preparation:
            - Find a quiet, comfortable spot.
            - Sit or lie down, keeping your back relaxed.
            
            Meditation Steps:
            1. Listen to Calming Music:
               - Play soft, instrumental music sounds.
               - Listen for 1-2 minutes to relax.

            2. Focus on Your Breath:
               - Breathe naturally and notice your breath.
               - If your mind wanders, gently refocus on breathing.

            3. End the Session:
               - After end song, take a deep breath.
               - Slowly move your fingers and toes to finish.
        """.trimIndent())
        builder.setPositiveButton("Got it") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun formatTime(milliseconds: Int): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / 1000) / 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun playAudio() {
        mediaPlayer.start()
        isPlaying = true
        binding.playPauseButton.setImageResource(R.drawable.ic_pause)
        updateSeekBar()
    }

    private fun pauseAudio() {
        mediaPlayer.pause()
        isPlaying = false
        binding.playPauseButton.setImageResource(R.drawable.ic_play)
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (this@MeditationFragment::mediaPlayer.isInitialized &&
                tryCheckPlaying()) {
                val progress = (mediaPlayer.currentPosition / mediaPlayer.duration.toFloat()) * 100
                binding.seekBar.progress = progress
                binding.currentTimeText.text = formatTime(mediaPlayer.currentPosition)
                binding.seekBar.postDelayed(this, 100)
            }
        }
    }

    // Fungsi untuk cek aman tanpa crash
    private fun tryCheckPlaying(): Boolean {
        return try {
            mediaPlayer.isPlaying
        } catch (e: IllegalStateException) {
            false
        }
    }

    private fun updateSeekBar() {
        binding.seekBar.removeCallbacks(updateRunnable)
        binding.seekBar.post(updateRunnable)
    }

    private fun resetAudio() {
        try {
            mediaPlayer.seekTo(0)
            binding.seekBar.progress = 0f
            pauseAudio()
            binding.seekBar.removeCallbacks(updateRunnable)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.seekBar.removeCallbacks(updateRunnable) // Hentikan update loop
        if (this::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        _binding = null
    }

}
