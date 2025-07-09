package com.example.skripsta.data

import androidx.lifecycle.LiveData

class UserRepository(private val userDao: UserDao) {

    val readAllData: LiveData<List<User>> = userDao.readAllData()

    suspend fun addUser(user: User) {
        userDao.addUser(user)
    }

    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    fun getJournalsByDate(selectedDate: String): LiveData<List<User>> {
        return userDao.getJournalsByDate(selectedDate)
    }

    suspend fun getUserById(userId: Int): User? {
        return userDao.getUserById(userId)
    }

    suspend fun getLastClaimDate(userId: Int): String? {
        return userDao.getLastClaimDate(userId)
    }

    suspend fun updateStreakAndPoints(userId: Int, streakCount: Int, points: Int, lastClaimDate: String) {
        userDao.updateStreakAndPoints(userId, streakCount, points, lastClaimDate)
    }
}