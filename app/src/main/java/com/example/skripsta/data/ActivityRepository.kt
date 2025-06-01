package com.example.skripsta.data

import androidx.lifecycle.LiveData

class ActivityRepository(private val activityDao: ActivityDao) {
    val allActivities: LiveData<List<Activity>> = activityDao.getAllActivities()

    suspend fun insertActivity(activity: Activity) {
        activityDao.insertActivity(activity)
    }

    suspend fun insertAllActivities(activities: List<Activity>) {
        activityDao.insertAllActivities(activities)
    }
}