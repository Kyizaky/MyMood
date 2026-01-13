package com.example.skripsta.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity Feeling merepresentasikan tabel `feeling_table`
 * pada database Room.
 *
 * Setiap objek Feeling akan disimpan sebagai satu baris
 * data di dalam tabel feeling_table.
 */
@Entity(tableName = "feeling_table")
data class Feeling(

    /**
     * Primary Key dari tabel feeling_table.
     * - Digenerate otomatis oleh Room
     * - Digunakan sebagai identitas unik setiap Feeling
     */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /**
     * Nama perasaan.
     * Contoh: "Senang", "Sedih", "Cemas", "Tenang"
     *
     * Digunakan untuk ditampilkan ke UI
     * serta sebagai data analisis dan statistik mood.
     */
    val name: String
)
