package com.example.skripsta.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.skripsta.data.entity.Icon

@Dao
interface IconDao {
    @Insert
    suspend fun insert(icon: Icon)

    @Query("SELECT * FROM icon_table")
    fun getAllIcons(): LiveData<List<Icon>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAllicons(icons: List<Icon>)
}