package com.example.skripsta

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.skripsta.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                    val intent = Intent(this, TambahActivity::class.java)
                    startActivity(intent)
                    true // Return true to indicate the item is selected
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

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}
