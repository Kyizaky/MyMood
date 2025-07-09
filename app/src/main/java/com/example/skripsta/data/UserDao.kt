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

    @Query("SELECT * FROM user_table WHERE tanggal = :selectedDate ORDER BY jam ASC")
    fun getJournalsByDate(selectedDate: String): LiveData<List<User>>

    @Query("SELECT * FROM user_table WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?

    // Fungsi untuk memperbarui poin dan tanggal login (dari kode asli)
    @Query("UPDATE user_table SET points = points + :points, lastLoginDate = :lastLoginDate WHERE id = :userId")
    suspend fun updatePointsAndLastLogin(userId: Int, points: Int, lastLoginDate: String)

    // Fungsi baru untuk memeriksa kelayakan claim berdasarkan lastClaimDate
    @Query("SELECT lastClaimDate FROM user_table WHERE id = :userId")
    suspend fun getLastClaimDate(userId: Int): String?

    // Fungsi baru untuk memperbarui streak, poin, dan tanggal claim
    @Query("UPDATE user_table SET streakCount = :streakCount, points = points + :points, lastClaimDate = :lastClaimDate WHERE id = :userId")
    suspend fun updateStreakAndPoints(userId: Int, streakCount: Int, points: Int, lastClaimDate: String)
}