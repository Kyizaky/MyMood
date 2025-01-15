package com.example.skripsta.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val mood: String,
    val aktivitas: String,
    val perasaan: String,
    val jurnal: String
)
