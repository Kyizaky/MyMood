package com.example.skripsta.data

import androidx.lifecycle.LiveData

class FeelingRepository(private val feelingDao: FeelingDao) {

    val allFeelings: LiveData<List<Feeling>> = feelingDao.getAllFeelings()

    suspend fun insertFeeling(feeling: Feeling) {
        feelingDao.insertFeeling(feeling)
    }

    suspend fun insertAllFeelings(feelings: List<Feeling>) {
        feelingDao.insertAllFeelings(feelings)
    }
}