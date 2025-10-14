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
    val points: Int,
    val lastClaimDate: String?,
    val lastLoginDate: String?, // Tracks last login date
    val lastMoodEntryDate: String?, // Tracks last mood entry date
    val unlockedPets: String, // Comma-separated list of unlocked pet drawable names
    val currentPetIndex: Int // Index of currently displayed pet
) : Parcelable