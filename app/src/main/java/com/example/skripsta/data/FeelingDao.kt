package com.example.skripsta.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FeelingDao {
    @Query("SELECT * FROM feeling_table ORDER BY name ASC")
    fun getAllFeelings(): LiveData<List<Feeling>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeeling(feeling: Feeling)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFeelings(feelings: List<Feeling>)
}