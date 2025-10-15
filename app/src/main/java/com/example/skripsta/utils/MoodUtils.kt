package com.example.skripsta.utils

import com.example.skripsta.R
import java.text.SimpleDateFormat
import java.util.Locale

object MoodUtils {

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
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH)
            val date = inputFormat.parse(tanggal)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            tanggal
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