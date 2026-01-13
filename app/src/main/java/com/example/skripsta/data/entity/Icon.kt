package com.example.skripsta.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity Icon merepresentasikan tabel `icon_table`
 * pada database Room.
 *
 * Tabel ini digunakan untuk menyimpan pasangan ikon
 * dalam dua kondisi visual: berwarna dan tidak berwarna.
 */
@Entity(tableName = "icon_table")
data class Icon(

    /**
     * Primary Key dari tabel icon_table.
     * - Digenerate otomatis oleh Room
     * - Menjadi identitas unik setiap data ikon
     */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /**
     * Resource ID ikon dalam kondisi berwarna (aktif).
     *
     * Digunakan ketika suatu item (mood, aktivitas, atau status tertentu)
     * berada dalam keadaan dipilih atau aktif.
     */
    val colorRes: Int,

    /**
     * Resource ID ikon dalam kondisi tidak berwarna (non-aktif).
     *
     * Digunakan ketika item belum dipilih atau dalam keadaan default.
     */
    val noColorRes: Int
)
