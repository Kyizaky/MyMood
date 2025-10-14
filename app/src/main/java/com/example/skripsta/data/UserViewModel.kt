package com.example.skripsta.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class UserViewModel(application: Application) : AndroidViewModel(application) {

    val readAllData: LiveData<List<User>>
    private val repository: UserRepository
    private val moodEntryRepository: MoodEntryRepository

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        val moodEntryDao = AppDatabase.getDatabase(application).moodEntryDao()
        repository = UserRepository(userDao)
        moodEntryRepository = MoodEntryRepository(moodEntryDao)
        readAllData = repository.readAllData
    }

    fun addUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addUser(user)
            Log.d("UserViewModel", "Added user: $user")
        }
    }

    suspend fun getUserById(userId: Int): User? {
        return repository.getUserById(userId)
    }

    suspend fun canClaimToday(userId: Int): Boolean {
        val user = repository.getUserById(userId) ?: return false
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        return user.lastClaimDate != today
    }


    suspend fun claimStreakPoints(userId: Int, basePoints: Int) {
        val user = repository.getUserById(userId) ?: return
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val updatedUser = user.copy(
            points = user.points + basePoints,
            lastClaimDate = today
        )
        repository.updateUser(updatedUser)
    }

    fun recordLogin(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            repository.updateLastLoginDate(userId, today)
        }
    }

    fun recordMoodEntry(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserById(userId) ?: return@launch
            val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            repository.updateLastMoodEntryDate(userId, today)
        }
    }


    fun evolvePet(userId: Int, newPetDrawable: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserById(userId) ?: return@launch
            val updatedPets = if (user.unlockedPets.isEmpty()) newPetDrawable else "${user.unlockedPets},$newPetDrawable"
            repository.updateUnlockedPets(userId, updatedPets, user.currentPetIndex + 1)
        }
    }

    fun updateCurrentPetIndex(userId: Int, index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCurrentPetIndex(userId, index)
        }
    }
}