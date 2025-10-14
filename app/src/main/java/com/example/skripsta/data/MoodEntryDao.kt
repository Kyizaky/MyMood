package com.example.skripsta.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface MoodEntryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addMoodEntry(moodEntry: MoodEntry)

    @Query("SELECT * FROM mood_entry_table ORDER BY id ASC")
    fun readAllData(): LiveData<List<MoodEntry>>

    @Update
    suspend fun updateMoodEntry(moodEntry: MoodEntry)

    @Delete
    suspend fun deleteMoodEntry(moodEntry: MoodEntry)

    @Query("SELECT * FROM mood_entry_table WHERE tanggal = :selectedDate ORDER BY jam ASC")
    fun getJournalsByDate(selectedDate: String): LiveData<List<MoodEntry>>

    @Query("SELECT * FROM mood_entry_table WHERE tanggal = :date")
    suspend fun getMoodEntriesForDate(date: String): List<MoodEntry>

    @Query("SELECT * FROM mood_entry_table WHERE id = :moodId")
    suspend fun getMoodById(moodId: Int): MoodEntry?

    @Query("SELECT * FROM mood_entry_table ORDER BY id ASC")
    suspend fun getAllMoodEntriesList(): List<MoodEntry>

}