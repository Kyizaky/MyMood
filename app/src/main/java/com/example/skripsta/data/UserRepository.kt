package com.example.skripsta.data

import androidx.lifecycle.LiveData

class UserRepository(private val userDao: UserDao) {

    val readALlData: LiveData<List<User>> = userDao.readAllData()

    suspend fun addUser(user: User){
        userDao.addUser(user)
    }

    suspend fun deleteUser(user: User){
        userDao.deleteUser(user)
    }

    fun getJournalsByDate(selectedDate: String): LiveData<List<User>> {
        return userDao.getJournalsByDate(selectedDate)
    }

}