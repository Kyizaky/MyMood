package com.example.skripsta.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_table")
data class Activity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val iconRes: Int
)