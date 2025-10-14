package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.R
import com.example.skripsta.data.MoodEntry
import com.example.skripsta.utils.MoodUtils

class MoodHistoryAdapter(
    private val onItemClick: (MoodEntry) -> Unit
) : ListAdapter<MoodEntry, MoodHistoryAdapter.MoodViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_entry, parent, false)
        return MoodViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MoodViewHolder(
        itemView: View,
        private val onItemClick: (MoodEntry) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val moodIcon: ImageView = itemView.findViewById(R.id.moodIcon)
        private val moodText: TextView = itemView.findViewById(R.id.moodText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val moodCard: CardView = itemView.findViewById(R.id.moodCardView)

        fun bind(entry: MoodEntry) {
            // Text dan ikon mood
            moodText.text = MoodUtils.getMoodText(entry.mood)
            moodIcon.setImageResource(MoodUtils.getMoodIcon(entry.mood))
            timeText.text = entry.jam

            val colorResId = MoodUtils.getMoodColor(entry.mood)
            moodCard.setCardBackgroundColor(itemView.context.getColor(colorResId))

            // Aksi klik
            itemView.setOnClickListener { onItemClick(entry) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MoodEntry>() {
        override fun areItemsTheSame(oldItem: MoodEntry, newItem: MoodEntry) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MoodEntry, newItem: MoodEntry) = oldItem == newItem
    }
}
