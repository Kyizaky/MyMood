package com.example.skripsta.data.repository

import androidx.lifecycle.LiveData
import com.example.skripsta.data.dao.FeelingDao
import com.example.skripsta.data.entity.Feeling

class FeelingRepository(private val feelingDao: FeelingDao) {

    val allFeelings: LiveData<List<Feeling>> = feelingDao.getAllFeelings()

    suspend fun insert(feeling: Feeling) {
        feelingDao.insert(feeling)
    }

    suspend fun update(feeling: Feeling) {
        feelingDao.update(feeling)
    }

    suspend fun delete(feeling: Feeling) {
        feelingDao.delete(feeling)
    }

    suspend fun getFeelingCount(): Int {
        return feelingDao.getFeelingCount()
    }

    suspend fun insertAllFeelings(feelings: List<Feeling>) {
        feelingDao.insertAllFeelings(feelings)
    }
}