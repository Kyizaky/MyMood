package com.example.skripsta.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feeling_table")
data class Feeling(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)