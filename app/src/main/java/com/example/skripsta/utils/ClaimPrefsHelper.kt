package com.example.skripsta.utils

import android.content.Context
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * ClaimPrefsHelper adalah helper untuk mengelola
 * data klaim harian menggunakan SharedPreferences.
 *
 * Data klaim disimpan dalam bentuk daftar tanggal (String)
 * dan digunakan untuk fitur reward, streak, atau daily claim.
 */
object ClaimPrefsHelper {

    /** Nama SharedPreferences */
    private const val PREFS_NAME = "AppPrefs"

    /** Key untuk menyimpan daftar tanggal klaim */
    private const val CLAIM_KEY = "claim_dates"

    /** Formatter tanggal dengan format yyyy-MM-dd */
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /**
     * Menyimpan tanggal hari ini sebagai klaim.
     *
     * - Mengambil tanggal hari ini
     * - Menambahkan ke daftar klaim yang sudah ada
     * - Menggunakan Set untuk mencegah duplikasi tanggal
     */
    fun saveClaimDateToday(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Ambil tanggal hari ini
        val today = LocalDate.now().format(dateFormatter)

        // Ambil klaim yang sudah tersimpan sebelumnya
        val existingClaims = prefs.getString(CLAIM_KEY, "")
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?.toMutableSet() ?: mutableSetOf()

        // Tambahkan tanggal hari ini
        existingClaims.add(today)

        // Simpan kembali ke SharedPreferences
        prefs.edit()
            .putString(CLAIM_KEY, existingClaims.joinToString(","))
            .apply()
    }

    /**
     * Mengambil seluruh tanggal klaim yang tersimpan.
     *
     * @return List<LocalDate> berisi seluruh tanggal klaim valid
     */
    fun getAllClaimDates(context: Context): List<LocalDate> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val claimStrings = prefs.getString(CLAIM_KEY, "") ?: ""

        return claimStrings.split(",")
            .filter { it.isNotBlank() }
            .mapNotNull {
                try {
                    // Konversi String ke LocalDate
                    LocalDate.parse(it, dateFormatter)
                } catch (e: Exception) {
                    // Abaikan data yang tidak valid
                    null
                }
            }
    }

    /**
     * Membersihkan data klaim lama dan hanya
     * menyimpan klaim pada minggu berjalan.
     *
     * - Minggu dihitung dari hari Minggu sampai Sabtu
     * - Data di luar rentang minggu ini akan dihapus
     */
    fun cleanOldClaimsKeepThisWeek(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val today = LocalDate.now()

        // Menentukan awal minggu (Minggu)
        val weekStart = today.minusDays((today.dayOfWeek.value % 7).toLong())

        // Menentukan akhir minggu (Sabtu)
        val weekEnd = weekStart.plusDays(6)

        // Filter klaim yang masih berada dalam minggu ini
        val filteredClaims = getAllClaimDates(context)
            .filter { it in weekStart..weekEnd }
            .map { it.format(dateFormatter) }
            .toSet()

        // Simpan kembali klaim yang sudah difilter
        prefs.edit()
            .putString(CLAIM_KEY, filteredClaims.joinToString(","))
            .apply()
    }
}
