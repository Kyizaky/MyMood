package com.example.skripsta.data

import androidx.lifecycle.LiveData

class MoodEntryRepository(private val moodEntryDao: MoodEntryDao) {

    val readAllMoodEntry: LiveData<List<MoodEntry>> = moodEntryDao.readAllData()

    suspend fun addMoodEntry(moodEntry: MoodEntry) {
        moodEntryDao.addMoodEntry(moodEntry)
    }

    suspend fun deleteAllMoodEntry() {
        moodEntryDao.deleteAllMoodEntry()
    }

    suspend fun replaceMoodEntry(moodList: List<MoodEntry>) {
        moodEntryDao.replaceMoodEntry(moodList)
    }

    suspend fun updateMoodEntry(moodEntry: MoodEntry) {
        moodEntryDao.updateMoodEntry(moodEntry)
    }

    suspend fun deleteMoodEntry(moodEntry: MoodEntry) {
        moodEntryDao.deleteMoodEntry(moodEntry)
    }

    fun getJournalsByDate(selectedDate: String): LiveData<List<MoodEntry>> {
        return moodEntryDao.getJournalsByDate(selectedDate)
    }


}