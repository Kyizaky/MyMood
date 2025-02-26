package com.example.skripsta.data

import androidx.lifecycle.LiveData

class ActivityRepository(private val activityDao: ActivityDao) {

    val getAllActivities: LiveData<List<ActivityEntity>> = activityDao.getAllActivities()

    suspend fun insert(activity: ActivityEntity) {
        activityDao.insert(activity)
    }

    suspend fun update(activity: ActivityEntity) {
        activityDao.update(activity)
    }

    suspend fun delete(activity: ActivityEntity) {
        activityDao.delete(activity)
    }

    suspend fun deleteAll() {
        activityDao.deleteAll()
    }
}
