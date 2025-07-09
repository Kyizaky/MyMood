package com.example.skripsta.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UserViewModel(application: Application) : AndroidViewModel(application) {

    val readAllData: LiveData<List<User>>
    private val repository: UserRepository

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
        readAllData = repository.readAllData
    }

    fun addUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addUser(user)
            Log.d("UserViewModel", "Added user: $user")
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteUser(user)
            Log.d("UserViewModel", "Deleted user: $user")
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUser(user)
            Log.d("UserViewModel", "Updated user: $user")
        }
    }

    fun getJournalsByDate(selectedDate: String): LiveData<List<User>> {
        return repository.getJournalsByDate(selectedDate)
    }

    suspend fun getUserById(userId: Int): User? {
        return repository.getUserById(userId)
    }

    fun canClaimStreakPoints(userId: Int): Boolean {
        return runBlocking(Dispatchers.IO) {
            val lastClaimDate = repository.getLastClaimDate(userId)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            Log.d("UserViewModel", "User ID: $userId, Last Claim Date: $lastClaimDate, Today: $today")
            lastClaimDate != today
        }
    }

    fun claimStreakPoints(userId: Int, pointsPerDay: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserById(userId)
            if (user == null) {
                Log.d("UserViewModel", "User with ID $userId not found")
                return@launch
            }
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time
            )

            val newStreak = when {
                user.lastClaimDate == yesterday || user.lastClaimDate == null -> user.streakCount + 1
                user.lastClaimDate != today -> 1
                else -> user.streakCount
            }

            val newPoints = pointsPerDay * newStreak
            Log.d("UserViewModel", "Awarding $newPoints points to userId: $userId, New Streak: $newStreak, Date: $today")
            repository.updateStreakAndPoints(userId, newStreak, newPoints, today)

            // Verify update
            val updatedUser = repository.getUserById(userId)
            Log.d("UserViewModel", "After claiming points, user: $updatedUser")
        }
    }
}