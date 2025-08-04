package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.R
import com.example.skripsta.utils.MoodUtils

class MoodLegendAdapter(private val moodData: List<Pair<Int, String>>) :
    RecyclerView.Adapter<MoodLegendAdapter.ViewHolder>() {

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
        holder.moodIcon.setImageResource(MoodUtils.getMoodIcon(moodType))
        holder.moodPercentage.text = percentage
    }

    override fun getItemCount(): Int = moodData.size
}
