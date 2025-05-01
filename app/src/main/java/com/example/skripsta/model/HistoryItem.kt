package com.example.skripsta.model

import com.example.skripsta.data.User

// Represents a section in the outer RecyclerView (a date and its list of entries)
data class HistorySection(
    val date: String,           // e.g., "15 April"
    val entries: List<User>     // List of entries for this date
)