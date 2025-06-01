package com.example.skripsta

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.skripsta.data.Activity
import com.example.skripsta.data.ActivityViewModel
import com.example.skripsta.data.Feeling
import com.example.skripsta.data.FeelingViewModel
import com.example.skripsta.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var feelingViewModel: FeelingViewModel
    private lateinit var activityViewModel: ActivityViewModel

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

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // Initialize ViewModels
        feelingViewModel = ViewModelProvider(this).get(FeelingViewModel::class.java)
        activityViewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)

        // Check and request to disable battery optimization
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
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

        // Initialize feeling and activity data if not already done
        initializeFeelingData()
        initializeActivityData()

        // Request notification permission on first launch
        val isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true)
        if (isFirstLaunch && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                sharedPreferences.edit().putBoolean("isFirstLaunch", false).apply()
            }
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

    private fun initializeFeelingData() {
        val isDataInitialized = sharedPreferences.getBoolean("isFeelingDataInitialized", false)
        if (!isDataInitialized) {
            val initialFeelings = listOf(
                Feeling(name = "Angry"),
                Feeling(name = "Disgust"),
                Feeling(name = "Scary"),
                Feeling(name = "Sad"),
                Feeling(name = "Happy"),
                Feeling(name = "Neutral"),
                Feeling(name = "Excited"),
                Feeling(name = "Anxious"),
                Feeling(name = "Calm"),
                Feeling(name = "Surprised")
            )
            feelingViewModel.addAllFeelings(initialFeelings)

            // Save default selected feeling IDs (Angry, Disgust, Scary, Sad, Happy)
            val defaultSelectedIds = initialFeelings.take(5).map { it.name }.toSet()
            sharedPreferences.edit()
                .putStringSet("selected_feeling_names", defaultSelectedIds)
                .putBoolean("isFeelingDataInitialized", true)
                .apply()
        }
    }

    private fun initializeActivityData() {
        val isActivityDataInitialized = sharedPreferences.getBoolean("isActivityDataInitialized", false)
        if (!isActivityDataInitialized) {
            val initialActivities = listOf(
                Activity(name = "Study", iconRes = R.drawable.activity1),
                Activity(name = "Shop", iconRes = R.drawable.activity2),
                Activity(name = "Work", iconRes = R.drawable.activity3),
                Activity(name = "Vacation", iconRes = R.drawable.activity4),
                Activity(name = "Eat", iconRes = R.drawable.activity5),
                Activity(name = "Gym", iconRes = R.drawable.activity6),
                Activity(name = "Sleep", iconRes = R.drawable.activity1),
                Activity(name = "Travel", iconRes = R.drawable.activity2),
                Activity(name = "Read", iconRes = R.drawable.activity3),
                Activity(name = "Game", iconRes = R.drawable.activity4)
            )
            activityViewModel.addAllActivities(initialActivities)

            // Save default selected activity IDs (Study, Shop, Work, Vacation, Eat)
            val defaultSelectedIds = initialActivities.take(5).map { it.name }.toSet()
            sharedPreferences.edit()
                .putStringSet("selected_activity_names", defaultSelectedIds)
                .putBoolean("isActivityDataInitialized", true)
                .apply()
        }
    }
}