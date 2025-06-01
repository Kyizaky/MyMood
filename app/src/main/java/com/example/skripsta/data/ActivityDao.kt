package com.example.skripsta.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activity_table ORDER BY name ASC")
    fun getAllActivities(): LiveData<List<Activity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: Activity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllActivities(activities: List<Activity>)
}