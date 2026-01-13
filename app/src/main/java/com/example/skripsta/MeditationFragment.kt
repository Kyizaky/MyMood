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

    // Binding untuk mengakses view pada fragment
    private var _binding: FragmentMeditationBinding? = null
    private val binding get() = _binding!!

    // MediaPlayer untuk memutar audio meditasi
    private lateinit var mediaPlayer: MediaPlayer

    // Status apakah audio sedang diputar
    private var isPlaying = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inisialisasi ViewBinding
        _binding = FragmentMeditationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mengaktifkan mode layar penuh (immersive mode)
        requireActivity().window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        // Tombol kembali untuk menghentikan audio dan kembali ke halaman sebelumnya
        binding.goBack.setOnClickListener {
            resetAudio()
            requireActivity().onBackPressed()
        }

        // Tombol petunjuk meditasi
        binding.howTo.setOnClickListener {
            showCustomDialog()
        }

        // Inisialisasi MediaPlayer dengan audio meditasi
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.music)

        // Listener untuk circular seek bar
        binding.seekBar.setOnSeekBarChangeListener(object :
            CircularSeekBar.OnCircularSeekBarChangeListener {

            // Mengatur posisi audio saat seekbar digeser
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

        // Tombol play dan pause audio
        binding.playPauseButton.setOnClickListener {
            if (isPlaying) pauseAudio() else playAudio()
        }

        // Tombol reset audio
        binding.resetButton.setOnClickListener {
            resetAudio()
        }

        // Update seekbar setelah audio siap diputar
        mediaPlayer.setOnPreparedListener {
            updateSeekBar()
        }

        // Reset audio ketika selesai diputar
        mediaPlayer.setOnCompletionListener {
            resetAudio()
        }
    }

    // Menampilkan dialog petunjuk meditasi
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

    // Mengubah waktu audio dari milidetik ke format menit:detik
    private fun formatTime(milliseconds: Int): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / 1000) / 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    // Memutar audio meditasi
    private fun playAudio() {
        mediaPlayer.start()
        isPlaying = true
        binding.playPauseButton.setImageResource(R.drawable.ic_pause)
        updateSeekBar()
    }

    // Menjeda audio meditasi
    private fun pauseAudio() {
        mediaPlayer.pause()
        isPlaying = false
        binding.playPauseButton.setImageResource(R.drawable.ic_play)
    }

    // Runnable untuk memperbarui seekbar dan waktu audio secara berkala
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (this@MeditationFragment::mediaPlayer.isInitialized &&
                tryCheckPlaying()) {

                val progress =
                    (mediaPlayer.currentPosition / mediaPlayer.duration.toFloat()) * 100
                binding.seekBar.progress = progress
                binding.currentTimeText.text = formatTime(mediaPlayer.currentPosition)
                binding.seekBar.postDelayed(this, 100)
            }
        }
    }

    // Fungsi untuk mengecek status audio tanpa menyebabkan crash
    private fun tryCheckPlaying(): Boolean {
        return try {
            mediaPlayer.isPlaying
        } catch (e: IllegalStateException) {
            false
        }
    }

    // Memulai pembaruan seekbar
    private fun updateSeekBar() {
        binding.seekBar.removeCallbacks(updateRunnable)
        binding.seekBar.post(updateRunnable)
    }

    // Mengatur ulang audio ke posisi awal
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

        // Menghentikan update seekbar
        binding.seekBar.removeCallbacks(updateRunnable)

        // Melepaskan MediaPlayer untuk mencegah memory leak
        if (this::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }

        // Membersihkan binding
        _binding = null
    }
}
