package com.example.skripsta.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ActivityRepository
    val allActivities: LiveData<List<Activity>>

    init {
        val activityDao = AppDatabase.getDatabase(application).activityDao()
        repository = ActivityRepository(activityDao)
        allActivities = repository.allActivities
    }

    fun addAllActivities(activities: List<Activity>) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertAllActivities(activities)
    }

    fun addActivity(activity: Activity) {
        viewModelScope.launch {
            repository.insert(activity)
        }
    }

    fun updateActivity(activity: Activity) {
        viewModelScope.launch {
            repository.update(activity)
        }
    }

    fun deleteActivity(activity: Activity) {
        viewModelScope.launch {
            repository.delete(activity)
        }
    }

    suspend fun getActivityCount(): Int {
        return repository.getActivityCount()
    }
}