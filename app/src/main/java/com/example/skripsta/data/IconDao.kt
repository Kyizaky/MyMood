package com.example.skripsta.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface IconDao {
    @Insert
    suspend fun insert(icon: Icon)

    @Query("SELECT * FROM icon_table")
    fun getAllIcons(): LiveData<List<Icon>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllicons(icons: List<Icon>)
}