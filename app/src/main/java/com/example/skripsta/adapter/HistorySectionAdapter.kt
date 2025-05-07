package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.R
import com.example.skripsta.data.User
import com.example.skripsta.model.HistorySection

class HistorySectionAdapter(
    private var sections: List<HistorySection>,
    private val onItemClick: (User) -> Unit
) : RecyclerView.Adapter<HistorySectionAdapter.SectionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_section, parent, false)
        return SectionViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        holder.bind(sections[position])
    }

    override fun getItemCount(): Int = sections.size

    class SectionViewHolder(
        itemView: View,
        private val onItemClick: (User) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val dateHeaderText: TextView = itemView.findViewById(R.id.dateHeaderText)
        private val moodRecyclerView: RecyclerView = itemView.findViewById(R.id.moodRecyclerView)


        fun bind(section: HistorySection) {
            dateHeaderText.text = section.date

            // Set up the inner RecyclerView for moods
            moodRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = MoodAdapter(section.entries, onItemClick)
                // Disable nested scrolling to prevent interference with the outer RecyclerView
                isNestedScrollingEnabled = false
            }
        }
    }
}