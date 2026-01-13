package com.example.skripsta.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.skripsta.data.AppDatabase
import com.example.skripsta.data.entity.Icon
import com.example.skripsta.data.repository.IconRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IconViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: IconRepository
    val allIcons: LiveData<List<Icon>>

    init {
        val iconDao = AppDatabase.Companion.getDatabase(application).iconDao()
        repository = IconRepository(iconDao)
        allIcons = repository.allIcons
    }

    fun insert(icon: Icon) {
        viewModelScope.launch {
            repository.insert(icon)
        }
    }

    fun addAllIcons(icons: List<Icon>) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertAllIcons(icons)
    }
}