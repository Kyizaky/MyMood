package com.example.skripsta.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ActivityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: ActivityEntity)

    @Update
    suspend fun update(activity: ActivityEntity)

    @Delete
    suspend fun delete(activity: ActivityEntity)

    @Query("DELETE FROM activity_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM activity_table ORDER BY count DESC")
    fun getAllActivities(): LiveData<List<ActivityEntity>>  // <---- Tambahkan ini
}

