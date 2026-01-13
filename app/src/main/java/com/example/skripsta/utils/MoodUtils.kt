package com.example.skripsta.utils

import com.example.skripsta.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * MoodUtils
 *
 * Kelas utilitas yang berfungsi sebagai helper untuk:
 * 1. Mengelola representasi mood (ikon, warna, dan teks)
 * 2. Melakukan formatting tanggal agar lebih user-friendly
 * 3. Menyediakan konversi data mentah (integer / string)
 *    menjadi bentuk visual dan teks yang mudah dipahami pengguna
 *
 * Seluruh fungsi bersifat stateless dan reusable.
 */
object MoodUtils {

    /**
     * Formatter untuk parsing tanggal dari database
     * Format standar: yyyy-MM-dd
     * Contoh: 2025-01-13
     */
    private val inputDateFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)

    /**
     * Formatter untuk menampilkan tanggal lengkap ke pengguna
     * Format: dd MMMM yyyy
     * Contoh: 13 January 2025
     */
    private val outputDateFormatter =
        DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)

    /**
     * Formatter alternatif untuk tampilan kalender atau list ringkas
     * Format: dd MMM yyyy
     * Contoh: 13 Jan 2025
     */
    private val outputDateFormatter2 =
        DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

    /**
     * Mengembalikan resource icon berdasarkan nilai mood.
     *
     * @param mood Nilai mood dalam bentuk integer (1–5)
     * @return Resource ID drawable yang sesuai dengan mood
     *
     * Digunakan untuk:
     * - Tampilan list mood
     * - Kalender mood
     * - Detail mood entry
     */
    fun getMoodIcon(mood: Int): Int {
        return when (mood) {
            1 -> R.drawable.para1   // Mood sangat buruk
            2 -> R.drawable.para2   // Mood buruk
            3 -> R.drawable.para3   // Mood netral
            4 -> R.drawable.para4   // Mood baik
            5 -> R.drawable.para5   // Mood sangat baik
            else -> R.drawable.ic_breath // Default jika data tidak valid
        }
    }

    /**
     * Memformat tanggal dari format database menjadi format yang
     * lebih mudah dibaca oleh pengguna.
     *
     * @param tanggal String tanggal dari database (yyyy-MM-dd)
     * @return String tanggal terformat (dd MMMM yyyy)
     *
     * Jika parsing gagal, maka string tanggal asli akan dikembalikan
     * untuk mencegah crash aplikasi.
     */
    fun formatTanggal(tanggal: String): String {
        return try {
            val date = LocalDate.parse(tanggal, inputDateFormatter)
            date.format(outputDateFormatter)
        } catch (e: Exception) {
            tanggal // Fallback jika format tidak sesuai
        }
    }

    /**
     * Memformat nama bulan agar huruf awal kapital.
     *
     * @param month Nama bulan dalam bentuk lowercase / tidak konsisten
     * @return Nama bulan dengan huruf kapital di awal
     *
     * Contoh:
     * "january" → "January"
     */
    fun formatMonthName(month: String): String {
        return month
            .lowercase(Locale.ENGLISH)
            .replaceFirstChar { it.titlecase(Locale.US) }
    }

    /**
     * Memformat tanggal untuk tampilan kalender atau daftar singkat.
     *
     * @param tanggal String tanggal dari database (yyyy-MM-dd)
     * @return String tanggal terformat (dd MMM yyyy)
     *
     * Digunakan pada:
     * - Mood calendar
     * - Ringkasan mingguan
     */
    fun formatCal(tanggal: String): String {
        return try {
            val date = LocalDate.parse(tanggal, inputDateFormatter)
            date.format(outputDateFormatter2)
        } catch (e: Exception) {
            tanggal // Fallback jika parsing gagal
        }
    }

    /**
     * Mengembalikan warna berdasarkan nilai mood.
     *
     * @param mood Nilai mood (1–5)
     * @return Resource ID warna
     *
     * Digunakan untuk:
     * - Background card mood
     * - Indikator visual grafik
     * - Kalender mood
     */
    fun getMoodColor(mood: Int): Int {
        return when (mood) {
            1 -> R.color.mood_1
            2 -> R.color.mood_2
            3 -> R.color.mood_3
            4 -> R.color.mood_4
            5 -> R.color.mood_5
            else -> R.color.transparent
        }
    }

    /**
     * Mengonversi nilai mood (integer) menjadi teks deskriptif.
     *
     * @param mood Nilai mood (1–5)
     * @return String representasi teks mood
     *
     * Digunakan untuk:
     * - Export Excel / CSV
     * - Tampilan detail mood
     * - Analisis data mood
     */
    fun getMoodText(mood: Int): String {
        return when (mood) {
            1 -> "Terrible"
            2 -> "Bad"
            3 -> "Okay"
            4 -> "Good"
            5 -> "Excellent"
            else -> "Unknown Mood"
        }
    }
}
