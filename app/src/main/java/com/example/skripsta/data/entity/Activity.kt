package com.example.skripsta.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity Activity merepresentasikan tabel `activity_table`
 * pada database Room.
 *
 * Setiap objek Activity akan disimpan sebagai satu baris
 * data di dalam tabel activity_table.
 */
@Entity(tableName = "activity_table")
data class Activity(

    /**
     * Primary Key dari tabel activity_table.
     * - Nilai akan digenerate otomatis oleh Room
     * - Digunakan sebagai identitas unik setiap Activity
     */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /**
     * Nama aktivitas.
     * Contoh: "Belajar", "Olahraga", "Bekerja"
     *
     * Digunakan untuk ditampilkan ke UI
     * dan sebagai referensi pemilihan aktivitas.
     */
    val name: String,

    /**
     * Resource ID ikon default aktivitas.
     * Digunakan saat aktivitas dalam kondisi tidak dipilih.
     */
    val iconRes: Int,

    /**
     * Resource ID ikon aktivitas ketika dipilih.
     * Digunakan untuk memberikan feedback visual ke pengguna.
     */
    val selectedIconRes: Int
)
