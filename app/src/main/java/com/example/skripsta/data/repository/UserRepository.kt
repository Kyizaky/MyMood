package com.example.skripsta.data.repository

import androidx.lifecycle.LiveData
import com.example.skripsta.data.dao.UserDao
import com.example.skripsta.data.entity.User

class UserRepository(private val userDao: UserDao) {

    val readAllData: LiveData<List<User>> = userDao.readAllData()

    suspend fun addUser(user: User) {
        userDao.addUser(user)
    }

    suspend fun deleteAllUsers() {
        userDao.deleteAllUsers()
    }

    suspend fun replaceUsers(users: List<User>) {
        userDao.replaceUser(users)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun getUserById(userId: Int): User? {
        return userDao.getUserById(userId)
    }

    suspend fun updateLastLoginDate(userId: Int, lastLoginDate: String) {
        userDao.updateLastLoginDate(userId, lastLoginDate)
    }

    suspend fun updateLastMoodEntryDate(userId: Int, lastMoodEntryDate: String) {
        userDao.updateLastMoodEntryDate(userId, lastMoodEntryDate)
    }

    suspend fun updateUnlockedPets(userId: Int, unlockedPets: String, currentPetIndex: Int) {
        userDao.updateUnlockedPets(userId, unlockedPets, currentPetIndex)
    }

    suspend fun updateCurrentPetIndex(userId: Int, currentPetIndex: Int) {
        userDao.updateCurrentPetIndex(userId, currentPetIndex)
    }
}