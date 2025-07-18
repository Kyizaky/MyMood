package com.example.skripsta.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MoodEntryViewModel(application: Application) : AndroidViewModel(application) {

    val readAllData: LiveData<List<MoodEntry>>
    private val repository: MoodEntryRepository

    init {
        val moodEntryDao = AppDatabase.getDatabase(application).moodEntryDao()
        repository = MoodEntryRepository(moodEntryDao)
        readAllData = repository.readAllMoodEntry
    }

    fun addMoodEntry(moodEntry: MoodEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addMoodEntry(moodEntry)
        }
    }

    fun updateMoodEntry(moodEntry: MoodEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMoodEntry(moodEntry)
        }
    }

    fun deleteMoodEntry(moodEntry: MoodEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMoodEntry(moodEntry)
        }
    }

    fun getJournalsByDate(selectedDate: String): LiveData<List<MoodEntry>> {
        return repository.getJournalsByDate(selectedDate)
    }
}