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
import com.example.skripsta.data.User
import com.example.skripsta.data.UserViewModel
import com.example.skripsta.databinding.FragmentDailyLoginBinding
import com.example.skripsta.utils.ClaimPrefsHelper
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DailyLoginFragment : Fragment() {

    private lateinit var binding: FragmentDailyLoginBinding
    private lateinit var userViewModel: UserViewModel
    private val dbDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val petDrawables = listOf("pet1", "pet2", "pet3")
    private val pointsToEvolveList = listOf(7, 14, 30)
    private var currentUserId: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDailyLoginBinding.inflate(inflater, container, false)
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        val sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        currentUserId = sharedPreferences.getInt("current_user_id", 1)

        userViewModel.recordLogin(currentUserId)

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_dailyLoginFragment_to_homeFragment)
        }

        userViewModel.readAllData.observe(viewLifecycleOwner) { userList ->
            val user = userList.find { it.id == currentUserId }
            user?.let {
                binding.tvStreakCount.text = it.points.toString()

                val currentIndex = it.currentPetIndex.coerceIn(0, petDrawables.size - 1)
                val pointsToEvolve = pointsToEvolveList[currentIndex]
                val isFinalEvolution = currentIndex >= petDrawables.size - 1
                val unlockedPets = it.unlockedPets.split(",").filter { it.isNotEmpty() }
                val maxUnlockedIndex = unlockedPets.map { pet -> petDrawables.indexOf(pet) }.maxOrNull() ?: 0
                val isActivePet = currentIndex == maxUnlockedIndex
                val nextPetIndex = currentIndex + 1
                val canEvolveToNext = nextPetIndex < petDrawables.size && petDrawables[nextPetIndex] !in unlockedPets

                // Periksa apakah poin cukup untuk evolusi berikutnya
                val previousPoints = if (maxUnlockedIndex > 0) pointsToEvolveList.take(maxUnlockedIndex).sum() else 0
                val relativePoints = (it.points - previousPoints).coerceAtLeast(0)


                // Update progress bar
                binding.petProgress.progress = it.points
                binding.petProgress.max = pointsToEvolve

                binding.petProgress.visibility = when {
                    !isActivePet -> View.GONE
                    isFinalEvolution -> View.GONE
                    relativePoints >= pointsToEvolve && canEvolveToNext -> View.GONE
                    else -> View.VISIBLE
                }

                binding.tvPoints.visibility = when {
                    !isActivePet -> View.GONE
                    isFinalEvolution -> View.GONE
                    relativePoints >= pointsToEvolve && canEvolveToNext -> View.GONE
                    else -> View.VISIBLE
                }

                // Tampilkan tombol evolve hanya jika poin cukup dan pet berikutnya belum di-unlock
                binding.btnEvolve.visibility = when {
                    !isFinalEvolution && it.points >= pointsToEvolve && canEvolveToNext -> View.VISIBLE
                    else -> View.GONE
                }

                // Update teks progress
                binding.tvPoints.text = when {
                    isFinalEvolution -> "Max evolution reached!"
                    it.points >= pointsToEvolve && canEvolveToNext -> "Go evolve"
                    else -> "${pointsToEvolve - it.points} more total days to unlock the next stage"
                }

                updatePetImage(it)
                updateChecklist(it)
            }
        }


        binding.btnClaimPoint.setOnClickListener {
            lifecycleScope.launch {
                val user = userViewModel.getUserById(currentUserId)
                val today = LocalDate.now().format(dbDateFormatter)
                if (userViewModel.canClaimToday(currentUserId) && user?.lastLoginDate == today && user?.lastMoodEntryDate == today) {
                    userViewModel.claimStreakPoints(currentUserId, 1)
                    ClaimPrefsHelper.saveClaimDateToday(requireContext())
                    Toast.makeText(
                        requireContext(),
                        "Claimed 1 point!",
                        Toast.LENGTH_SHORT
                    ).show()
                    user?.let {
                        binding.tvStreakCount.text = it.points.toString()
                        val currentIndex = it.currentPetIndex.coerceIn(0, petDrawables.size - 1)
                        val pointsToEvolve = pointsToEvolveList[currentIndex]
                        val isFinalEvolution = currentIndex >= petDrawables.size - 1
                        val unlockedPets = it.unlockedPets.split(",").filter { it.isNotEmpty() }
                        val nextPetIndex = currentIndex + 1
                        val canEvolveToNext = nextPetIndex < petDrawables.size && petDrawables[nextPetIndex] !in unlockedPets

                        // Periksa apakah poin cukup untuk evolusi berikutnya
                        val nextPointsToEvolve = if (nextPetIndex < pointsToEvolveList.size) pointsToEvolveList[nextPetIndex] else pointsToEvolve
                        val isPointFull = it.points >= pointsToEvolve && (isFinalEvolution || it.points >= nextPointsToEvolve)

                        binding.petProgress.progress = it.points
                        binding.petProgress.max = pointsToEvolve
                        binding.petProgress.visibility = if (isFinalEvolution || isPointFull) View.GONE else View.VISIBLE
                        binding.btnEvolve.visibility = if (!isFinalEvolution && it.points >= pointsToEvolve && canEvolveToNext) View.VISIBLE else View.GONE
                        binding.tvPoints.visibility = if (isFinalEvolution || isPointFull) View.GONE else View.VISIBLE
                        binding.tvPoints.text = when {
                            isFinalEvolution -> "Max evolution reached!"
                            it.points >= pointsToEvolve && canEvolveToNext -> "Go evolve"
                            else -> "${pointsToEvolve - it.points} more total days to unlock the next stage"
                        }
                        updateChecklist(it)
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Complete all mandatory tasks to claim points!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.btnEvolve.setOnClickListener {
            lifecycleScope.launch {
                val user = userViewModel.getUserById(currentUserId) ?: return@launch
                val currentIndex = user.currentPetIndex.coerceIn(0, petDrawables.size - 1)
                val nextPetIndex = currentIndex + 1
                if (nextPetIndex < petDrawables.size) {
                    val pointsToEvolve = pointsToEvolveList[currentIndex]
                    val nextPointsToEvolve = if (nextPetIndex < pointsToEvolveList.size) pointsToEvolveList[nextPetIndex] else pointsToEvolve
                    val unlockedPets = user.unlockedPets.split(",").filter { it.isNotEmpty() }
                    val canEvolveToNext = petDrawables[nextPetIndex] !in unlockedPets

                    if (user.points >= pointsToEvolve && canEvolveToNext) {
                        val newPet = petDrawables[nextPetIndex]
                        userViewModel.evolvePet(currentUserId, newPet)
                        Toast.makeText(requireContext(), "Pet evolved to ${newPet}!", Toast.LENGTH_SHORT).show()

                        val isFinalEvolution = nextPetIndex >= petDrawables.size - 1
                        binding.petProgress.visibility = if (isFinalEvolution || user.points >= nextPointsToEvolve) View.GONE else View.VISIBLE
                        binding.btnEvolve.visibility = if (!isFinalEvolution && user.points >= nextPointsToEvolve && canEvolveToNext) View.VISIBLE else View.GONE
                        binding.tvPoints.visibility = if (isFinalEvolution || user.points >= nextPointsToEvolve) View.GONE else View.VISIBLE
                        binding.tvPoints.text = when {
                            isFinalEvolution -> "Max evolution reached!"
                            user.points >= nextPointsToEvolve && canEvolveToNext -> "Go evolve"
                            else -> "${nextPointsToEvolve - user.points} more total days to unlock the next stage"
                        }
                        updatePetImage(user)
                        updateChecklist(user)
                    } else {
                        Toast.makeText(requireContext(), "Not enough points to evolve!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.nextPet.setOnClickListener {
            lifecycleScope.launch {
                val user = userViewModel.getUserById(currentUserId) ?: return@launch
                val nextIndex = user.currentPetIndex + 1
                if (nextIndex < petDrawables.size) {
                    userViewModel.updateCurrentPetIndex(currentUserId, nextIndex)
                }
            }
        }

        binding.prevPet.setOnClickListener {
            lifecycleScope.launch {
                val user = userViewModel.getUserById(currentUserId) ?: return@launch
                val prevIndex = user.currentPetIndex - 1
                if (prevIndex >= 0) {
                    userViewModel.updateCurrentPetIndex(currentUserId, prevIndex)
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
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
        lifecycleScope.launch {
            val canClaim = userViewModel.canClaimToday(currentUserId)
            binding.btnClaimPoint.isEnabled = canClaim
        }
    }


    private fun updateChecklist(user: User) {
        val today = LocalDate.now().format(dbDateFormatter)
        binding.check1.setImageResource(
            if (user.lastLoginDate == today) R.drawable.ic_checkbox else R.drawable.ic_nocheck
        )
        binding.check2.setImageResource(
            if (user.lastMoodEntryDate == today) R.drawable.ic_checkbox else R.drawable.ic_nocheck
        )
    }

    private fun updatePetImage(user: User) {
        val unlockedPets = user.unlockedPets.split(",").filter { it.isNotEmpty() }
        val currentIndex = user.currentPetIndex.coerceIn(0, petDrawables.size - 1)
        val petDrawableName = petDrawables[currentIndex]
        val drawableRes = if (petDrawableName in unlockedPets) {
            resources.getIdentifier(petDrawableName, "drawable", requireContext().packageName)
        } else {
            R.drawable.ic_ask
        }
        binding.petImage.setImageResource(drawableRes)
        binding.petName.text = if (petDrawableName in unlockedPets) "Ikky" else "Locked"
        binding.prevPet.visibility = if (currentIndex > 0) View.VISIBLE else View.GONE
        binding.nextPet.visibility = if (currentIndex < petDrawables.size - 1) View.VISIBLE else View.GONE
    }
}