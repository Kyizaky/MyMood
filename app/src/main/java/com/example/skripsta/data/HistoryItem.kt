package com.example.skripsta.data

import com.example.skripsta.data.User

// Sealed class to represent either a Date Header or a History Entry
sealed class HistoryItem {
    data class DateHeader(val date: String) : HistoryItem() // e.g., "22 Mei"
    data class Entry(val user: User) : HistoryItem() // The actual history entry
}