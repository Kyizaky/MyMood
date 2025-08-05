package com.example.skripsta.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FeelingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FeelingRepository
    val allFeelings: LiveData<List<Feeling>>

    init {
        val feelingDao = AppDatabase.getDatabase(application).feelingDao()
        repository = FeelingRepository(feelingDao)
        allFeelings = repository.allFeelings
    }

    fun addAllFeelings(feelings: List<Feeling>) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertAllFeelings(feelings)
    }

    fun addFeeling(feeling: Feeling) {
        viewModelScope.launch {
            repository.insert(feeling)
        }
    }

    fun updateFeeling(feeling: Feeling) {
        viewModelScope.launch {
            repository.update(feeling)
        }
    }

    fun deleteFeeling(feeling: Feeling) {
        viewModelScope.launch {
            repository.delete(feeling)
        }
    }

    suspend fun getFeelingCount(): Int {
        return repository.getFeelingCount()
    }
}