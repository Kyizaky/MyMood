package com.example.skripsta.data

import androidx.lifecycle.LiveData

class IconRepository(private val iconDao: IconDao) {
    val allIcons: LiveData<List<Icon>> = iconDao.getAllIcons()

    suspend fun insert(icon: Icon) {
        iconDao.insert(icon)
    }

    suspend fun insertAllIcons(icons: List<Icon>) {
        iconDao.insertAllicons(icons)
    }
}