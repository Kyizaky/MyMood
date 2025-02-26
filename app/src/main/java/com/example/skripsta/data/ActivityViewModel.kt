package com.example.skripsta.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.skripsta.data.ActivityEntity
import com.example.skripsta.data.ActivityRepository
import com.example.skripsta.data.AppDatabase
import kotlinx.coroutines.launch

class ActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ActivityRepository
    val allActivities: LiveData<List<ActivityEntity>>

    init {
        val activityDao = AppDatabase.getDatabase(application).activityDao()
        repository = ActivityRepository(activityDao)
        allActivities = repository.getAllActivities
    }

    fun insert(activity: ActivityEntity) = viewModelScope.launch {
        repository.insert(activity)
    }

    fun update(activity: ActivityEntity) = viewModelScope.launch {
        repository.update(activity)
    }

    fun delete(activity: ActivityEntity) = viewModelScope.launch {
        repository.delete(activity)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}
