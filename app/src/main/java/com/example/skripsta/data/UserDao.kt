package com.example.skripsta.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addUser(user: User)

    @Query("SELECT * FROM user_table ORDER BY id ASC")
    fun readAllData(): LiveData<List<User>>

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT * FROM user_table WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?

    @Query("UPDATE user_table SET streakCount = :streakCount, points = :points, lastClaimDate = :lastClaimDate WHERE id = :userId")
    suspend fun updateStreakAndPoints(userId: Int, streakCount: Int, points: Int, lastClaimDate: String)

    @Query("UPDATE user_table SET lastLoginDate = :lastLoginDate WHERE id = :userId")
    suspend fun updateLastLoginDate(userId: Int, lastLoginDate: String)

    @Query("UPDATE user_table SET lastMoodEntryDate = :lastMoodEntryDate WHERE id = :userId")
    suspend fun updateLastMoodEntryDate(userId: Int, lastMoodEntryDate: String)

    @Query("UPDATE user_table SET unlockedPets = :unlockedPets, currentPetIndex = :currentPetIndex WHERE id = :userId")
    suspend fun updateUnlockedPets(userId: Int, unlockedPets: String, currentPetIndex: Int)

    @Query("UPDATE user_table SET currentPetIndex = :currentPetIndex WHERE id = :userId")
    suspend fun updateCurrentPetIndex(userId: Int, currentPetIndex: Int)
}