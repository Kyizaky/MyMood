package com.example.skripsta

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.skripsta.data.*
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
                Toast.makeText(
                    this,
                    "Notification permission is required for reminders to work",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        feelingViewModel = ViewModelProvider(this).get(FeelingViewModel::class.java)
        activityViewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)


        // Cek dan minta izin battery optimization
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }

        // Setup navigasi
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

        // Inisialisasi data berdasarkan isi database
        initializeFeelingData()
        initializeActivityData()

        // Minta izin notifikasi untuk Android 13+
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

    private fun initializeFeelingData() {
        feelingViewModel.allFeelings.observe(this) { feelings ->
            if (feelings.isNullOrEmpty()) {
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
                Log.d("MainActivity", "Inserted initial feelings")

                val defaultSelectedNames = initialFeelings.take(5).map { it.name }.toSet()
                sharedPreferences.edit()
                    .putStringSet("selected_feeling_names", defaultSelectedNames)
                    .apply()
            } else {
                Log.d("MainActivity", "Feelings already initialized: ${feelings.map { it.name }}")
            }
        }
    }

    private fun initializeActivityData() {
        activityViewModel.allActivities.observe(this) { activities ->
            if (activities.isNullOrEmpty()) {
                val initialActivities = listOf(
                    Activity(name = "Study", iconRes = R.drawable.activity1, selectedIconRes = R.drawable.activity1_nocolor),
                    Activity(name = "Shop", iconRes = R.drawable.activity2, selectedIconRes = R.drawable.activity2_nocolor),
                    Activity(name = "Work", iconRes = R.drawable.activity3, selectedIconRes = R.drawable.activity3_nocolor),
                    Activity(name = "Vacation", iconRes = R.drawable.activity4, selectedIconRes = R.drawable.activity4_nocolor),
                    Activity(name = "Eat", iconRes = R.drawable.activity5, selectedIconRes = R.drawable.activity5_nocolor),
                    Activity(name = "Gym", iconRes = R.drawable.activity6, selectedIconRes = R.drawable.activity6_nocolor),
                    Activity(name = "Swim", iconRes = R.drawable.activity7, selectedIconRes = R.drawable.activity7_nocolor)
                )
                activityViewModel.addAllActivities(initialActivities)
                Log.d("MainActivity", "Inserted initial activities")

                val defaultSelectedNames = initialActivities.take(5).map { it.name }.toSet()
                sharedPreferences.edit()
                    .putStringSet("selected_activity_names", defaultSelectedNames)
                    .apply()
            } else {
                Log.d("MainActivity", "Activities already initialized: ${activities.map { it.name }}")
            }
        }
    }
}
