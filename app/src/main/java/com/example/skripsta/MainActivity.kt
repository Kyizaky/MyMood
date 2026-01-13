package com.example.skripsta

import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.skripsta.viewmodel.ActivityViewModel
import com.example.skripsta.viewmodel.FeelingViewModel
import com.example.skripsta.viewmodel.IconViewModel
import com.example.skripsta.viewmodel.PinLockViewModel
import com.example.skripsta.viewmodel.UserViewModel
import com.example.skripsta.databinding.ActivityMainBinding
import com.example.skripsta.data.entity.Activity
import com.example.skripsta.data.entity.Feeling
import com.example.skripsta.data.entity.Icon
import com.example.skripsta.data.entity.User
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userViewModel: UserViewModel
    private lateinit var feelingViewModel: FeelingViewModel
    private lateinit var activityViewModel: ActivityViewModel
    private lateinit var iconViewModel: IconViewModel
    private val pinLockViewModel = PinLockViewModel()

    // Variabel untuk menangani double back press
    private var backPressedTime = 0L
    private var backToast: Toast? = null

    // Launcher untuk request permission notifikasi
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
        // Menyembunyikan ActionBar
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        feelingViewModel = ViewModelProvider(this).get(FeelingViewModel::class.java)
        activityViewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        iconViewModel = ViewModelProvider(this).get(IconViewModel::class.java)

        // Mengecek apakah userId sudah tersedia
        val userId = sharedPreferences.getInt("current_user_id", -1)
        if (userId == -1) {
            sharedPreferences.edit().putInt("current_user_id", 1).apply()
            Log.d("MainActivity", "Set default userId to 1")
        }

        // Warna teks BottomNavigation
        val navHost = supportFragmentManager.findFragmentById(R.id.navHostFragmentContainer) as NavHostFragment
        navController = navHost.navController
        binding.bottomNavigationView.setupWithNavController(navController)

        val textColorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(
                ContextCompat.getColor(this, R.color.vista),
                ContextCompat.getColor(this, R.color.lightGray)
            )
        )
        binding.bottomNavigationView.itemTextColor = textColorStateList

        // Warna ikon BottomNavigation
        val iconColorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(
                ContextCompat.getColor(this, R.color.vista),
                ContextCompat.getColor(this, R.color.lightGray)
            )
        )
        binding.bottomNavigationView.itemIconTintList = iconColorStateList

        val visibleFragments = setOf(
            R.id.homeFragment,
            R.id.statFragment,
            R.id.pengaturanFragment,
            R.id.kegiatanFragment
        )

        // Navigasi ke halaman PIN jika PIN sudah diatur
        if (pinLockViewModel.hasPin(this)) {
            navController.navigate(
                R.id.pinLockFragment,
                Bundle().apply { putString("mode", "login") }
            )
        }

        // Menampilkan atau menyembunyikan BottomNavigation
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigationView.visibility =
                if (destination.id in visibleFragments) View.VISIBLE else View.GONE
        }

        // Inisialisasi data awal aplikasi
        lifecycleScope.launch {
            initializeFeelingData()
            initializeIconData()
            initializeActivityData()
            checkDailyLogin()
        }

        // Request permission notifikasi untuk Android 13+
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

    // Mengatur perilaku tombol back
    override fun onBackPressed() {
        val currentDestination = navController.currentDestination?.id

        val mainFragments = setOf(
            R.id.homeFragment,
            R.id.statFragment,
            R.id.kegiatanFragment,
            R.id.tambahFragment,
            R.id.pengaturanFragment
        )

        when {
            // Jika berada di tab utama selain Home
            currentDestination in mainFragments && currentDestination != R.id.homeFragment -> {
                navController.popBackStack(R.id.homeFragment, false)
                binding.bottomNavigationView.selectedItemId = R.id.homeFragment

            }

            // Jika berada di HomeFragment
            currentDestination == R.id.homeFragment -> {
                val currentTime = System.currentTimeMillis()
                if (currentTime - backPressedTime < 2000) {
                    backToast?.cancel()
                    super.onBackPressed() // keluar aplikasi
                } else {
                    backToast = Toast.makeText(
                        this,
                        "Tekan sekali lagi untuk keluar",
                        Toast.LENGTH_SHORT
                    )
                    backToast?.show()
                    backPressedTime = currentTime
                }
            }

            // Kalau di halaman lain (bukan bagian bottom nav), biarkan back biasa
            else -> super.onBackPressed()
        }
    }

    // Navigasi ke atas
    override fun onSupportNavigateUp(): Boolean {
        navController = findNavController(R.id.navHostFragmentContainer)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    // Mengecek login harian user
    private fun checkDailyLogin() {
        val userId = sharedPreferences.getInt("current_user_id", 1)
        lifecycleScope.launch {
            val user = userViewModel.getUserById(userId)
            if (user == null) {
                val newUser = User(
                    id = userId,
                    points = 0,
                    lastClaimDate = null,
                    lastLoginDate = null,
                    lastMoodEntryDate = null,
                    unlockedPets = "pet1",
                    currentPetIndex = 0
                )
                userViewModel.addUser(newUser)
                Log.d("MainActivity", "Created new user with ID: $userId")
            } else {
                Log.d("MainActivity", "Current streak for userId: $userId is ${user.points}")
            }
        }
    }

    // Inisialisasi data Feeling default
    private fun initializeFeelingData() {
        feelingViewModel.allFeelings.observe(this) { feelings ->
            if (feelings.isNullOrEmpty()) {
                val initialFeelings = listOf(
                    Feeling(name = "Angry"),
                    Feeling(name = "Disgust"),
                    Feeling(name = "Scary"),
                    Feeling(name = "Sad"),
                    Feeling(name = "Happy")
                )
                feelingViewModel.addAllFeelings(initialFeelings)
                Log.d("MainActivity", "Inserted initial feelings: ${initialFeelings.map { it.name }}")

                val defaultSelectedNames = initialFeelings.take(5).map { it.name }.toSet()
                sharedPreferences.edit()
                    .putStringSet("selected_feeling_names", defaultSelectedNames)
                    .apply()
            } else {
                Log.d("MainActivity", "Feelings already initialized: ${feelings.map { it.name }}")
            }
        }
    }

    // Inisialisasi data Icon default
    private fun initializeIconData() {
        iconViewModel.allIcons.observe(this) { icons ->
            if (icons.isNullOrEmpty()) {
                val initialIcons = listOf(
                    Icon(
                        colorRes = R.drawable.activity1,
                        noColorRes = R.drawable.activity1_nocolor
                    ),
                    Icon(
                        colorRes = R.drawable.activity2,
                        noColorRes = R.drawable.activity2_nocolor
                    ),
                    Icon(
                        colorRes = R.drawable.activity3,
                        noColorRes = R.drawable.activity3_nocolor
                    ),
                    Icon(
                        colorRes = R.drawable.activity4,
                        noColorRes = R.drawable.activity4_nocolor
                    ),
                    Icon(colorRes = R.drawable.activity5, noColorRes = R.drawable.activity5_nocolor)
                )
                iconViewModel.addAllIcons(initialIcons)
                Log.d("MainActivity", "Inserted initial icons: ${initialIcons.map { it.colorRes }}")
            } else {
                Log.d("MainActivity", "Icons already initialized: ${icons.map { it.colorRes }}")
            }
        }
    }

    // Inisialisasi data Activity default
    private fun initializeActivityData() {
        activityViewModel.allActivities.observe(this) { activities ->
            if (activities.isNullOrEmpty()) {
                val initialActivities = listOf(
                    Activity(
                        name = "Study",
                        iconRes = R.drawable.activity1,
                        selectedIconRes = R.drawable.activity1_nocolor
                    ),
                    Activity(
                        name = "Shop",
                        iconRes = R.drawable.activity2,
                        selectedIconRes = R.drawable.activity2_nocolor
                    ),
                    Activity(
                        name = "Work",
                        iconRes = R.drawable.activity3,
                        selectedIconRes = R.drawable.activity3_nocolor
                    ),
                    Activity(
                        name = "Vacation",
                        iconRes = R.drawable.activity4,
                        selectedIconRes = R.drawable.activity4_nocolor
                    ),
                    Activity(
                        name = "Eat",
                        iconRes = R.drawable.activity5,
                        selectedIconRes = R.drawable.activity5_nocolor
                    ),
                    Activity(
                        name = "Gym",
                        iconRes = R.drawable.activity6,
                        selectedIconRes = R.drawable.activity6_nocolor
                    ),
                    Activity(
                        name = "Swim",
                        iconRes = R.drawable.activity7,
                        selectedIconRes = R.drawable.activity7_nocolor
                    )
                )
                activityViewModel.addAllActivities(initialActivities)
                Log.d("MainActivity", "Inserted initial activities: ${initialActivities.map { it.name }}")

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