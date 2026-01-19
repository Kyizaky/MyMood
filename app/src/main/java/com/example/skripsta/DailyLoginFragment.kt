package com.example.skripsta

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.skripsta.data.entity.User
import com.example.skripsta.viewmodel.UserViewModel
import com.example.skripsta.databinding.FragmentDailyLoginBinding
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DailyLoginFragment : Fragment() {

    private lateinit var binding: FragmentDailyLoginBinding
    private lateinit var userViewModel: UserViewModel
    private val dbDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val petDrawables = listOf("pet1", "pet2", "pet3")
    private val pointsToEvolveList = listOf(1, 14, 30)
    private var currentUserId: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inisialisasi binding
        binding = FragmentDailyLoginBinding.inflate(inflater, container, false)

        // Inisialisasi ViewModel
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        // Ambil user aktif dari SharedPreferences
        val sharedPreferences =
            requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        currentUserId = sharedPreferences.getInt("current_user_id", 1)

        // Catat login hari ini ke database
        userViewModel.recordLogin(currentUserId)

        // Tombol kembali ke fragment sebelumnya
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Observasi perubahan data user
        userViewModel.readAllData.observe(viewLifecycleOwner) { userList ->
            val user = userList.find { it.id == currentUserId }
            user?.let {

                // Menampilkan total poin (streak)
                binding.tvStreakCount.text = it.points.toString()

                // Menentukan index pet saat ini
                val currentIndex = it.currentPetIndex.coerceIn(0, petDrawables.size - 1)

                // Poin yang dibutuhkan untuk evolusi saat ini
                val pointsToEvolve = pointsToEvolveList[currentIndex]

                // Mengecek apakah pet sudah evolusi terakhir
                val isFinalEvolution = currentIndex >= petDrawables.size - 1

                // List pet yang sudah terbuka
                val unlockedPets = it.unlockedPets.split(",").filter { it.isNotEmpty() }

                // Index pet tertinggi yang sudah terbuka
                val maxUnlockedIndex =
                    unlockedPets.map { pet -> petDrawables.indexOf(pet) }.maxOrNull() ?: 0

                // Mengecek apakah pet yang sedang ditampilkan adalah pet aktif
                val isActivePet = currentIndex == maxUnlockedIndex

                // Index pet selanjutnya
                val nextPetIndex = currentIndex + 1

                // Mengecek apakah bisa evolve ke pet berikutnya
                val canEvolveToNext =
                    nextPetIndex < petDrawables.size &&
                            petDrawables[nextPetIndex] !in unlockedPets

                // Menghitung poin sebelumnya
                val previousPoints =
                    if (maxUnlockedIndex > 0)
                        pointsToEvolveList.take(maxUnlockedIndex).sum()
                    else 0

                // Menghitung poin relatif untuk progress
                val relativePoints = (it.points - previousPoints).coerceAtLeast(0)

                // Update progress bar
                binding.petProgress.progress = it.points
                binding.petProgress.max = pointsToEvolve

                // Mengatur visibilitas progress bar
                binding.petProgress.visibility = when {
                    !isActivePet -> View.GONE
                    isFinalEvolution -> View.GONE
                    relativePoints >= pointsToEvolve && canEvolveToNext -> View.GONE
                    else -> View.VISIBLE
                }

                // Mengatur visibilitas teks poin
                binding.tvPoints.visibility = when {
                    !isActivePet -> View.GONE
                    isFinalEvolution -> View.GONE
                    relativePoints >= pointsToEvolve && canEvolveToNext -> View.GONE
                    else -> View.VISIBLE
                }

                // Menampilkan tombol evolve jika memenuhi syarat
                binding.btnEvolve.visibility = when {
                    !isFinalEvolution && it.points >= pointsToEvolve && canEvolveToNext -> View.VISIBLE
                    else -> View.GONE
                }

                // Menampilkan teks status evolusi
                binding.tvPoints.text = when {
                    isFinalEvolution -> "Max evolution reached!"
                    it.points >= pointsToEvolve && canEvolveToNext -> "Go evolve"
                    else -> "${pointsToEvolve - it.points} more total days to unlock the next stage"
                }

                // Update gambar pet
                updatePetImage(it)

                // Update checklist tugas harian
                updateChecklist(it)
            }
        }

        // Tombol klaim poin harian
        binding.btnClaimPoint.setOnClickListener {
            lifecycleScope.launch {

                // Ambil data user
                val user = userViewModel.getUserById(currentUserId)
                val today = LocalDate.now().format(dbDateFormatter)

                // Mengecek apakah klaim hari ini valid
                if (userViewModel.canClaimToday(currentUserId)
                    && user?.lastLoginDate == today
                    && user?.lastMoodEntryDate == today
                ) {
                    // Tambah 1 poin streak
                    userViewModel.claimStreakPoints(currentUserId, 1)

                    // Tampilkan notifikasi
                    Toast.makeText(requireContext(), "Claimed 1 point!", Toast.LENGTH_SHORT).show()

                    // Nonaktifkan tombol klaim
                    binding.btnClaimPoint.isEnabled = false
                    binding.btnClaimPoint.setBackgroundResource(R.drawable.bg_btn_disabled)

                    // Update checklist setelah klaim
                    user?.let {
                        updateChecklist(it)
                    }
                } else {
                    // Notifikasi jika tugas belum lengkap
                    Toast.makeText(
                        requireContext(),
                        "Complete all mandatory tasks to claim points!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Nonaktifkan tombol klaim
                    binding.btnClaimPoint.isEnabled = false
                    binding.btnClaimPoint.setBackgroundResource(R.drawable.bg_btn_disabled)
                }
            }
        }

        // Tombol evolusi pet
        binding.btnEvolve.setOnClickListener {
            lifecycleScope.launch {

                // Ambil user
                val user = userViewModel.getUserById(currentUserId) ?: return@launch
                val currentIndex = user.currentPetIndex.coerceIn(0, petDrawables.size - 1)
                val nextPetIndex = currentIndex + 1

                if (nextPetIndex < petDrawables.size) {
                    val pointsToEvolve = pointsToEvolveList[currentIndex]
                    val unlockedPets = user.unlockedPets.split(",").filter { it.isNotEmpty() }

                    // Mengecek apakah pet berikutnya belum terbuka
                    val canEvolveToNext = petDrawables[nextPetIndex] !in unlockedPets

                    if (user.points >= pointsToEvolve && canEvolveToNext) {
                        val newPet = petDrawables[nextPetIndex]

                        // Update evolusi pet
                        userViewModel.evolvePet(currentUserId, newPet)

                        // Notifikasi evolusi
                        Toast.makeText(
                            requireContext(),
                            "Pet evolved to $newPet!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Update tampilan pet
                        updatePetImage(user)
                        updateChecklist(user)
                    } else {
                        // Notifikasi jika poin tidak cukup
                        Toast.makeText(
                            requireContext(),
                            "Not enough points to evolve!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        // Navigasi ke pet berikutnya
        binding.nextPet.setOnClickListener {
            lifecycleScope.launch {
                val user = userViewModel.getUserById(currentUserId) ?: return@launch
                val nextIndex = user.currentPetIndex + 1
                if (nextIndex < petDrawables.size) {
                    userViewModel.updateCurrentPetIndex(currentUserId, nextIndex)
                }
            }
        }

        // Navigasi ke pet sebelumnya
        binding.prevPet.setOnClickListener {
            lifecycleScope.launch {
                val user = userViewModel.getUserById(currentUserId) ?: return@launch
                val prevIndex = user.currentPetIndex - 1
                if (prevIndex >= 0) {
                    userViewModel.updateCurrentPetIndex(currentUserId, prevIndex)
                }
            }
        }

        // Mengatur padding sesuai status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val statusBarHeight =
                insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            binding.root.setPadding(
                binding.root.paddingLeft,
                statusBarHeight,
                binding.root.paddingRight,
                binding.root.paddingBottom
            )
            insets
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // Mengecek status klaim saat fragment tampil
        lifecycleScope.launch {
            val user = userViewModel.getUserById(currentUserId)
            val today = LocalDate.now().format(dbDateFormatter)

            val canClaim = userViewModel.canClaimToday(currentUserId)
            val isTaskComplete =
                user?.lastLoginDate == today && user?.lastMoodEntryDate == today

            val isEnabled = canClaim && isTaskComplete
            binding.btnClaimPoint.isEnabled = isEnabled

            // Mengubah background tombol klaim
            binding.btnClaimPoint.setBackgroundResource(
                if (isEnabled) R.drawable.bg_btn else R.drawable.bg_btn_disabled
            )
        }
    }

    // Update checklist tugas harian
    private fun updateChecklist(user: User) {
        val today = LocalDate.now().format(dbDateFormatter)
        binding.check1.setImageResource(
            if (user.lastLoginDate == today)
                R.drawable.ic_checkbox
            else
                R.drawable.ic_nocheck
        )
        binding.check2.setImageResource(
            if (user.lastMoodEntryDate == today)
                R.drawable.ic_checkbox
            else
                R.drawable.ic_nocheck
        )
    }

    // Update gambar dan nama pet
    private fun updatePetImage(user: User) {
        val unlockedPets = user.unlockedPets.split(",").filter { it.isNotEmpty() }
        val currentIndex = user.currentPetIndex.coerceIn(0, petDrawables.size - 1)
        val petDrawableName = petDrawables[currentIndex]

        // Menentukan drawable pet
        val drawableRes =
            if (petDrawableName in unlockedPets)
                resources.getIdentifier(
                    petDrawableName,
                    "drawable",
                    requireContext().packageName
                )
            else
                R.drawable.ic_ask

        // Set gambar pet
        binding.petImage.setImageResource(drawableRes)

        // Set nama pet
        binding.petName.text =
            if (petDrawableName in unlockedPets) "Ikky" else "Locked"

        // Atur navigasi pet
        binding.prevPet.visibility =
            if (currentIndex > 0) View.VISIBLE else View.GONE
        binding.nextPet.visibility =
            if (currentIndex < petDrawables.size - 1) View.VISIBLE else View.GONE
    }
}
