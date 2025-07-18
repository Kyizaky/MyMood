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
    private val petDrawables = listOf("img_3", "img_4", "img_5")
    private val pointsToEvolve = 800
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
                binding.tvStreakCount.text = it.streakCount.toString()
                binding.petProgress.progress = it.points
                binding.petProgress.max = pointsToEvolve
                binding.tvPoints.text = "${pointsToEvolve - it.points} poin untuk membuka tampilan berikutnya"
                binding.petProgress.visibility = if (it.points >= pointsToEvolve) View.GONE else View.VISIBLE
                binding.btnEvolve.visibility = if (it.points >= pointsToEvolve) View.VISIBLE else View.GONE
                updatePetImage(it)
                updateChecklist(it)
            }
        }

        binding.btnClaimPoint.setOnClickListener {
            lifecycleScope.launch {
                if (userViewModel.canClaimStreakPoints(currentUserId)) {
                    userViewModel.claimStreakPoints(currentUserId, 5)
                    val user = userViewModel.getUserById(currentUserId)
                    val pointsClaimed = if (user?.streakCount ?: 0 >= 2) 10 else 5
                    ClaimPrefsHelper.saveClaimDateToday(requireContext())
                    Toast.makeText(
                        requireContext(),
                        "Claimed $pointsClaimed points! Streak: ${user?.streakCount}",
                        Toast.LENGTH_SHORT
                    ).show()
                    user?.let {
                        binding.tvStreakCount.text = it.streakCount.toString()
                        binding.petProgress.progress = it.points
                        binding.tvPoints.text = "${pointsToEvolve - it.points} poin untuk membuka tampilan berikutnya"
                        binding.petProgress.visibility = if (it.points >= pointsToEvolve) View.GONE else View.VISIBLE
                        binding.btnEvolve.visibility = if (it.points >= pointsToEvolve) View.VISIBLE else View.GONE
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
                val nextPetIndex = user.currentPetIndex + 1
                if (nextPetIndex < petDrawables.size) {
                    val newPet = petDrawables[nextPetIndex]
                    userViewModel.evolvePet(currentUserId, newPet)
                    userViewModel.updateUser(user.copy(points = 0))
                    Toast.makeText(requireContext(), "Pet evolved to ${newPet}!", Toast.LENGTH_SHORT).show()
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
            binding.btnClaimPoint.isEnabled = userViewModel.canClaimStreakPoints(currentUserId)
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
        binding.check3.setImageResource(
            if (user.streakCount >= 2) R.drawable.ic_checkbox else R.drawable.ic_nocheck
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
        binding.petName.text = if (petDrawableName in unlockedPets) "Ikky ${currentIndex + 1}" else "Locked"
        binding.prevPet.visibility = if (currentIndex > 0) View.VISIBLE else View.GONE
        binding.nextPet.visibility = if (currentIndex < petDrawables.size - 1) View.VISIBLE else View.GONE
    }
}