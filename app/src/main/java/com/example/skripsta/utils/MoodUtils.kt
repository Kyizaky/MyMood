package com.example.skripsta.utils

import com.example.skripsta.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

object MoodUtils {

    private val inputDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
    private val outputDateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)
    private val outputDateFormatter2 = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

    fun getMoodIcon(mood: Int): Int {
        return when (mood) {
            1 -> R.drawable.para1
            2 -> R.drawable.para2
            3 -> R.drawable.para3
            4 -> R.drawable.para4
            5 -> R.drawable.para5
            else -> R.drawable.ic_breath
        }
    }

    fun formatTanggal(tanggal: String): String {
        return try {
            val date = LocalDate.parse(tanggal, inputDateFormatter)
            date.format(outputDateFormatter)
        } catch (e: Exception) {
            tanggal // Return original string if parsing fails
        }
    }

    fun formatMonthName(month: String): String {
        return month.lowercase(Locale.ENGLISH).replaceFirstChar { it.titlecase(Locale.US) }
    }


    fun formatCal(tanggal: String): String {
        return try {
            val date = LocalDate.parse(tanggal, inputDateFormatter)
            date.format(outputDateFormatter2)
        } catch (e: Exception) {
            tanggal // Return original string if parsing fails
        }
    }

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