package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.R
import com.example.skripsta.data.User
import com.example.skripsta.utils.MoodUtils

class MoodAdapter(
    private var entries: List<User>,
    private val onItemClick: (User) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_entry, parent, false)
        return MoodViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(entries[position])
    }

    override fun getItemCount(): Int = entries.size

    class MoodViewHolder(
        itemView: View,
        private val onItemClick: (User) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val moodIcon: ImageView = itemView.findViewById(R.id.moodIcon)
        private val moodText: TextView = itemView.findViewById(R.id.moodText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val moodCard: CardView = itemView.findViewById(R.id.moodCardView)

        fun bind(user: User) {
            // Map mood integer to string and icon
            moodText.text = user.perasaan
            moodIcon.setImageResource(MoodUtils.getMoodIcon(user.mood))

            timeText.text = user.jam
            val colorResId = when (user.mood) {
                1 -> R.color.mood_1
                2 -> R.color.mood_2
                3 -> R.color.mood_3
                4 -> R.color.mood_4
                5 -> R.color.mood_5
                6 -> R.color.mood_6
                else -> R.color.transparent
            }
            moodCard.setCardBackgroundColor(itemView.context.getColor(colorResId))

            itemView.setOnClickListener { onItemClick(user) }
        }
    }
}