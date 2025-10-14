package com.example.skripsta.data

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.skripsta.utils.ExcelExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MoodEntryViewModel(application: Application) : AndroidViewModel(application) {

    val readAllData: LiveData<List<MoodEntry>>
    private val repository: MoodEntryRepository
    private val context = getApplication<Application>().applicationContext

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

    fun exportMoodEntries() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val moodList = repository.getAllMoodEntriesList() // ✅ ambil langsung dari DB
                if (moodList.isNotEmpty()) {
                    ExcelExporter.exportMoodEntriesToExcel(context, moodList)
                } else {
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "Tidak ada data untuk diexport", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal export: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
