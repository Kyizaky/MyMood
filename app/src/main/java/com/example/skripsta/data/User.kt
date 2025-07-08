package com.example.skripsta.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val mood: Int,
    val activities: String,
    val activityIcon: Int, // ID drawable aktivitas yang dipilih
    val perasaan: String,
    val judul: String,
    val jurnal: String,
    val tanggal: String, // Format: MM/dd/yyyy
    val jam: String, // Format: HH:mm
    val points: Int = 0, // Kolom baru untuk menyimpan total poin
    val lastLoginDate: String? = null // Kolom baru untuk menyimpan tanggal login terakhir
) : Parcelable