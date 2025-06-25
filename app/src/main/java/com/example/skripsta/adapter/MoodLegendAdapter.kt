package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.R

class MoodLegendAdapter(private val moodData: List<Pair<Int, String>>) :
    RecyclerView.Adapter<MoodLegendAdapter.ViewHolder>() {

    private val moodIcons = mapOf(
        1 to R.drawable.mood1,   // Ganti dengan icon sesuai
        2 to R.drawable.mood2, // Ganti dengan icon sesuai
        3 to R.drawable.mood3,    // Ganti dengan icon sesuai
        4 to R.drawable.mood4,     // Ganti dengan icon sesuai
        5 to R.drawable.mood5,   // Ganti dengan icon sesuai
        6 to R.drawable.mood6  // Ganti dengan icon sesuai
    )

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val moodIcon: ImageView = view.findViewById(R.id.mood_icon)
        val moodPercentage: TextView = view.findViewById(R.id.mood_percentage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_legend, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (moodType, percentage) = moodData[position]
        holder.moodIcon.setImageResource(moodIcons[moodType] ?: R.drawable.ic_medi)
        holder.moodPercentage.text = percentage
    }

    override fun getItemCount() = moodData.size
}
