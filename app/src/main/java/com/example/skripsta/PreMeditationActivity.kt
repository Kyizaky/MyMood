package com.example.skripsta

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.skripsta.databinding.ActivityPreMeditationBinding

class PreMeditationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreMeditationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreMeditationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil data fragment yang dikirim dari Activity sebelumnya
        val fragmentType = intent.getStringExtra("fragment")

        // Tentukan fragment mana yang akan ditampilkan
        if (fragmentType == "A") {
            loadFragment(KeteranganMeditationFragment())
        } else if (fragmentType == "B") {
            loadFragment(KeteranganBreatheFragment())
        }

        binding.startActivity.setOnClickListener {
            if (fragmentType == "A") {
                // Navigate to MeditasiActivity
                startActivity(Intent(this, MeditationActivity::class.java))
            } else if (fragmentType == "B") {
                // Navigate to BreatheActivity
                startActivity(Intent(this, BreatheActivity::class.java))
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }
}