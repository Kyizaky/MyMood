package com.example.skripsta.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface FeelingDao {
    @Query("SELECT * FROM feeling_table ORDER BY name ASC")
    fun getAllFeelings(): LiveData<List<Feeling>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeeling(feeling: Feeling)

    @Insert
    suspend fun insert(feeling: Feeling)

    @Update
    suspend fun update(feeling: Feeling)

    @Delete
    suspend fun delete(feeling: Feeling)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFeelings(feelings: List<Feeling>)

    @Query("SELECT COUNT(*) FROM feeling_table")
    suspend fun getFeelingCount(): Int
}