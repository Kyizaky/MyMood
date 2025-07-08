package com.example.skripsta.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserViewModel(application: Application) : AndroidViewModel(application) {

    val readAllData: LiveData<List<User>>
    private val repository: UserRepository

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
        readAllData = repository.readALlData // Fixed typo: readALlData -> readAllData
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

    suspend fun canAwardDailyPoints(userId: Int): Boolean {
        val user = repository.getUserById(userId)
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        Log.d("UserViewModel", "User ID: $userId, User: $user, Current Date: $currentDate, Last Login: ${user?.lastLoginDate}")
        return user == null || user.lastLoginDate != currentDate
    }

    fun awardDailyLoginPoints(userId: Int, pointsToAward: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            Log.d("UserViewModel", "Awarding $pointsToAward points to userId: $userId, Date: $currentDate")
            repository.updatePointsAndLastLogin(userId, pointsToAward, currentDate)
            // Verify update
            val updatedUser = repository.getUserById(userId)
            Log.d("UserViewModel", "After awarding points, user: $updatedUser")
        }
    }
}