package com.example.skripsta.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "user_table")
data class User (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val mood: Int,
    val activities: String,
    val perasaan: String,
    val judul: String,
    val jurnal: String,
    val tanggal: String, // Format: MM/dd/yyyy
    val jam: String // Format: HH:mm
): Parcelable
