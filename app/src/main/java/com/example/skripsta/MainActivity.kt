package com.example.skripsta

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.skripsta.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideSystemUI()

        // Set the default fragment when the app starts
        replaceFragment(HomeFragment())

        // Set listener for bottom navigation item selection
        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    replaceFragment(HomeFragment())
                    true // Return true to indicate the item is selected
                }
                R.id.riwayat -> {
                    replaceFragment(RiwayatFragment())
                    true // Return true to indicate the item is selected
                }
                R.id.tambah -> {
                    replaceFragment(TambahFragment())
                    setBottomNavIndicator(false)
                    false // Return true to indicate the item is selected
                }
                R.id.kegiatan -> {
                    replaceFragment(KegiatanFragment())
                    true // Return true to indicate the item is selected
                }
                R.id.pengaturan -> {
                    replaceFragment(PengaturanFragment())
                    true // Return true to indicate the item is selected
                }
                else -> false // Return false if no valid item is selected
            }
        }
    }

    private fun setBottomNavIndicator(isActive: Boolean) {
        if (isActive) {
            binding.bottomNavigationView.itemActiveIndicatorColor = ContextCompat.getColorStateList(this, R.color.vista)
        } else {
            binding.bottomNavigationView.itemActiveIndicatorColor = ContextCompat.getColorStateList(this, R.color.transparent)
        }
    }
    @SuppressLint("ResourceType")

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
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
}
