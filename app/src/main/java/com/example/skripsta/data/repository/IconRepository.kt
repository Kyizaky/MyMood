package com.example.skripsta.data.repository

import androidx.lifecycle.LiveData
import com.example.skripsta.data.dao.IconDao
import com.example.skripsta.data.entity.Icon

class IconRepository(private val iconDao: IconDao) {
    val allIcons: LiveData<List<Icon>> = iconDao.getAllIcons()

    suspend fun insert(icon: Icon) {
        iconDao.insert(icon)
    }

    suspend fun insertAllIcons(icons: List<Icon>) {
        iconDao.insertAllicons(icons)
    }
}