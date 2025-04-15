package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.R
import com.example.skripsta.data.HistoryItem

class HistoryAdapter(
    private val historyItems: List<HistoryItem>,
    private val onItemClick: (HistoryItem.Entry) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_DATE_HEADER = 0
        private const val TYPE_ENTRY = 1
    }

    // ViewHolder for Date Header
    class DateHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateHeaderText: TextView = itemView.findViewById(R.id.dateHeaderText)
    }

    // ViewHolder for History Entry
    class EntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val moodIcon: ImageView = itemView.findViewById(R.id.moodIcon)
        val moodText: TextView = itemView.findViewById(R.id.moodText)
        val timeText: TextView = itemView.findViewById(R.id.timeText)
    }

    override fun getItemViewType(position: Int): Int {
        return when (historyItems[position]) {
            is HistoryItem.DateHeader -> TYPE_DATE_HEADER
            is HistoryItem.Entry -> TYPE_ENTRY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_DATE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_date_header, parent, false)
                DateHeaderViewHolder(view)
            }
            TYPE_ENTRY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_history_entry, parent, false)
                EntryViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = historyItems[position]) {
            is HistoryItem.DateHeader -> {
                val dateHeaderHolder = holder as DateHeaderViewHolder
                dateHeaderHolder.dateHeaderText.text = item.date
            }
            is HistoryItem.Entry -> {
                val entryHolder = holder as EntryViewHolder
                val user = item.user
                entryHolder.moodText.text = user.perasaan
                entryHolder.timeText.text = user.jam
                entryHolder.moodIcon.setImageResource(convertMoodToImage(user.mood))

                // Handle click on the entry
                entryHolder.itemView.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }

    override fun getItemCount(): Int = historyItems.size

    // Function to convert mood to image resource (same as in your previous code)
    private fun convertMoodToImage(mood: Int): Int {
        return when (mood) {
            1 -> R.drawable.mood1
            2 -> R.drawable.mood2
            3 -> R.drawable.mood3
            4 -> R.drawable.mood4
            5 -> R.drawable.mood5
            else -> R.drawable.mood6
        }
    }
}