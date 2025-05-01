package com.example.skripsta.utils

import com.example.skripsta.R

object MoodUtils {
    // Map mood integer to string and drawable resource ID
    fun getMoodString(mood: Int): String {
        return when (mood) {
            1 -> "Angry"
            2 -> "Disgust"
            3 -> "Scary"
            4 -> "Sad"
            5 -> "Happy"
            6 -> "Neutral"
            else -> "Unknown"
        }
    }

    fun getMoodIcon(mood: Int): Int {
        return when (mood) {
            1 -> R.drawable.mood1
            2 -> R.drawable.mood2
            3 -> R.drawable.mood3
            4 -> R.drawable.mood4
            5 -> R.drawable.mood5
            6 -> R.drawable.mood6
            else -> R.drawable.ic_mood
        }
    }
}