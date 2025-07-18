package com.example.skripsta.utils

import android.content.Context
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ClaimPrefsHelper {
    private const val PREFS_NAME = "AppPrefs"
    private const val CLAIM_KEY = "claim_dates"
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun saveClaimDateToday(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = LocalDate.now().format(dateFormatter)
        val existingClaims = prefs.getString(CLAIM_KEY, "")
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?.toMutableSet() ?: mutableSetOf()

        existingClaims.add(today)
        prefs.edit().putString(CLAIM_KEY, existingClaims.joinToString(",")).apply()
    }

    fun getAllClaimDates(context: Context): List<LocalDate> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val claimStrings = prefs.getString(CLAIM_KEY, "") ?: ""
        return claimStrings.split(",")
            .filter { it.isNotBlank() }
            .mapNotNull {
                try {
                    LocalDate.parse(it, dateFormatter)
                } catch (e: Exception) {
                    null
                }
            }
    }

    /**
     * Hapus semua tanggal claim yang berada di luar minggu berjalan.
     * Minggu dimulai dari Sunday (dayOfWeek == 7 % 7 = 0).
     */
    fun cleanOldClaimsKeepThisWeek(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = LocalDate.now()
        val weekStart = today.minusDays((today.dayOfWeek.value % 7).toLong())
        val weekEnd = weekStart.plusDays(6)

        val filteredClaims = getAllClaimDates(context)
            .filter { it in weekStart..weekEnd }
            .map { it.format(dateFormatter) }
            .toSet()

        prefs.edit().putString(CLAIM_KEY, filteredClaims.joinToString(",")).apply()
    }
}
