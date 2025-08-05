package com.example.skripsta.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "icon_table")
data class Icon(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val colorRes: Int,
    val noColorRes: Int
)