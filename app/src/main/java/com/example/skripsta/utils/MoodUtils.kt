package com.example.skripsta.utils

import com.example.skripsta.R

object MoodUtils {

    fun getMoodIcon(mood: Int): Int {
        return when (mood) {
            1 -> R.drawable.para1
            2 -> R.drawable.para2
            3 -> R.drawable.para3
            4 -> R.drawable.para4
            5 -> R.drawable.para5
            else -> R.drawable.ic_breathe
        }
    }

}