package com.example.skripsta.data

import androidx.lifecycle.LiveData

class UserRepository(private val userDao: UserDao) {

    val readALlData: LiveData<List<User>> = userDao.readAllData()

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

    // Fungsi baru untuk mendukung gamifikasi
    suspend fun getUserById(userId: Int): User? {
        return userDao.getUserById(userId)
    }

    suspend fun updatePointsAndLastLogin(userId: Int, points: Int, lastLoginDate: String) {
        userDao.updatePointsAndLastLogin(userId, points, lastLoginDate)
    }
}