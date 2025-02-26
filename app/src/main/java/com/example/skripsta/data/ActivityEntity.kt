package com.example.skripsta.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_table")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val count: Int = 1,
    val imageUri: String? = null // Menyimpan URI gambar sebagai string
)
