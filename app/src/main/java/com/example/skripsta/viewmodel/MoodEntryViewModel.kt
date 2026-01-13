package com.example.skripsta.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.skripsta.data.AppDatabase
import com.example.skripsta.data.entity.MoodEntry
import com.example.skripsta.data.repository.MoodEntryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MoodEntryViewModel(application: Application) : AndroidViewModel(application) {

    val readAllData: LiveData<List<MoodEntry>>
    private val repository: MoodEntryRepository

    init {
        val moodEntryDao = AppDatabase.Companion.getDatabase(application).moodEntryDao()
        repository = MoodEntryRepository(moodEntryDao)
        readAllData = repository.readAllMoodEntry
    }

    fun addMoodEntry(moodEntry: MoodEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addMoodEntry(moodEntry)
        }
    }

    fun replaceMoodEntry(moodList: List<MoodEntry>) = viewModelScope.launch {
        repository.replaceMoodEntry(moodList)
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