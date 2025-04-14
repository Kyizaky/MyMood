package com.example.skripsta.utils

import android.content.Context
import android.view.View
import android.widget.TextView
import com.example.skripsta.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class MoodMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {

    private val markerText: TextView = findViewById(R.id.tv_marker_content)

    private val moodMapping = mapOf(
        1 to "Angry",
        2 to "Disgust",
        3 to "Scary",
        4 to "Sad",
        5 to "Happy",
        6 to "Neutral"
    )

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e == null || e.y == 0f) {
            markerText.visibility = View.GONE
            this.alpha = 0f // Buat marker jadi transparan sepenuhnya
        } else {
            markerText.visibility = View.VISIBLE
            this.alpha = 1f // Tampilkan marker seperti biasa

            val hour = e.x.toInt()
            val timeText = String.format("%02d:00", hour)
            val moodText = moodMapping[e.y.toInt()] ?: "Tidak diketahui"

            markerText.text = "Jam: $timeText\nMood: $moodText"
        }

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF((-width / 2).toFloat(), -height.toFloat())
    }
}
