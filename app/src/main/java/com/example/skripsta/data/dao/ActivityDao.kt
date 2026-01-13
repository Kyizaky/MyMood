package com.example.skripsta.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.skripsta.data.entity.Activity

/**
 * Data Access Object (DAO) untuk entitas Activity.
 *
 * DAO berfungsi sebagai penghubung antara aplikasi
 * dengan database Room untuk melakukan operasi CRUD
 * (Create, Read, Update, Delete).
 */
@Dao
interface ActivityDao {

    /**
     * Menyimpan satu data Activity ke dalam database.
     * Jika terjadi konflik primary key, data akan gagal disimpan
     * karena tidak didefinisikan strategi conflict.
     */
    @Insert
    suspend fun insert(activity: Activity)

    /**
     * Memperbarui data Activity yang sudah ada di database.
     * Data akan dicocokkan berdasarkan primary key.
     */
    @Update
    suspend fun update(activity: Activity)

    /**
     * Menghapus satu data Activity dari database.
     */
    @Delete
    suspend fun delete(activity: Activity)

    /**
     * Mengambil seluruh data Activity dari tabel activity_table
     * dan mengurutkannya berdasarkan nama secara alfabet (ASC).
     *
     * Menggunakan LiveData agar UI dapat otomatis ter-update
     * ketika terjadi perubahan data.
     */
    @Query("SELECT * FROM activity_table ORDER BY name ASC")
    fun getAllActivities(): LiveData<List<Activity>>

    /**
     * Menyimpan satu Activity ke database.
     * Jika data dengan primary key yang sama sudah ada,
     * maka data lama akan digantikan (REPLACE).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: Activity)

    /**
     * Menyimpan banyak Activity sekaligus ke database.
     * Digunakan untuk inisialisasi data atau insert massal.
     * Jika terjadi konflik, data lama akan digantikan.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllActivities(activities: List<Activity>)

    /**
     * Menghitung jumlah total data Activity
     * yang tersimpan di dalam tabel activity_table.
     */
    @Query("SELECT COUNT(*) FROM activity_table")
    suspend fun getActivityCount(): Int
}
