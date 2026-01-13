package com.example.skripsta.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.skripsta.data.entity.User

@Dao
interface UserDao {


    @Query("SELECT * FROM user_table ORDER BY id ASC")
    fun readAllData(): LiveData<List<User>>

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun addUser(user: User)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun addUserAll(users: List<User>)

    @Query("DELETE FROM user_table")
    suspend fun deleteAllUsers()

    @Transaction
    suspend fun replaceUser(users: List<User>) {
        deleteAllUsers()
        addUserAll(users)
    }

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT * FROM user_table WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?

    @Query("UPDATE user_table SET lastLoginDate = :lastLoginDate WHERE id = :userId")
    suspend fun updateLastLoginDate(userId: Int, lastLoginDate: String)

    @Query("UPDATE user_table SET lastMoodEntryDate = :lastMoodEntryDate WHERE id = :userId")
    suspend fun updateLastMoodEntryDate(userId: Int, lastMoodEntryDate: String)

    @Query("UPDATE user_table SET unlockedPets = :unlockedPets, currentPetIndex = :currentPetIndex WHERE id = :userId")
    suspend fun updateUnlockedPets(userId: Int, unlockedPets: String, currentPetIndex: Int)

    @Query("UPDATE user_table SET currentPetIndex = :currentPetIndex WHERE id = :userId")
    suspend fun updateCurrentPetIndex(userId: Int, currentPetIndex: Int)
}