package com.example.skripsta

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.skripsta.databinding.ActivityMainBinding
import android.Manifest // Add this import
import android.content.Context
import android.content.Intent
import android.util.Log

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                android.widget.Toast.makeText(
                    this,
                    "Notification permission is required for reminders to work",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check and request to disable battery optimization
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(POWER_SERVICE) as android.os.PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                Log.d("MainActivity", "Requesting to disable battery optimization")
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = android.net.Uri.parse("package:$packageName")
                startActivity(intent)
            } else {
                Log.d("MainActivity", "Battery optimization already disabled")
            }
        }

        hideSystemUI()
        val navHost = supportFragmentManager.findFragmentById(R.id.navHostFragmentContainer) as NavHostFragment
        navController = navHost.navController

        binding.bottomNavigationView.setupWithNavController(navController)

        val visibleFragments = setOf(
            R.id.homeFragment,
            R.id.statFragment,
            R.id.pengaturanFragment,
            R.id.kegiatanFragment
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigationView.visibility =
                if (destination.id in visibleFragments) View.VISIBLE else View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        navController = findNavController(R.id.navHostFragmentContainer)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    @SuppressLint("ResourceType")
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )
    }
}