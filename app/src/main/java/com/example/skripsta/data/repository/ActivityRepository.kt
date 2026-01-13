package com.example.skripsta.data.repository

import androidx.lifecycle.LiveData
import com.example.skripsta.data.dao.ActivityDao
import com.example.skripsta.data.entity.Activity

class ActivityRepository(private val activityDao: ActivityDao) {
    val allActivities: LiveData<List<Activity>> = activityDao.getAllActivities()

    suspend fun insert(activity: Activity) {
        activityDao.insert(activity)
    }

    suspend fun update(activity: Activity) {
        activityDao.update(activity)
    }

    suspend fun delete(activity: Activity) {
        activityDao.delete(activity)
    }

    suspend fun getActivityCount(): Int {
        return activityDao.getActivityCount()
    }

    suspend fun insertAllActivities(activities: List<Activity>) {
        activityDao.insertAllActivities(activities)
    }
}