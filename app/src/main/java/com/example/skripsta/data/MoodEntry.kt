package com.example.skripsta.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "mood_entry_table")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val mood: Int,
    val activities: String,
    val activityIcon: Int,
    val perasaan: String,
    val judul: String,
    val jurnal: String,
    val tanggal: String,
    val jam: String
): Parcelable