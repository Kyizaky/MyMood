package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.R
import com.example.skripsta.RiwayatTanggalFragmentDirections
import com.example.skripsta.data.User

class JournalAdapter : ListAdapter<User, JournalAdapter.JournalViewHolder>(DiffCallback()) {

    class JournalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageMood: ImageView = itemView.findViewById(R.id.imageViewIcon)
        val feeling: TextView = itemView.findViewById(R.id.textViewStory)
        val jam: TextView = itemView.findViewById(R.id.tv_jam)
        val cvMood: ConstraintLayout = itemView.findViewById(R.id.consMood)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mood_entry, parent, false)
        return JournalViewHolder(view)
    }

    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        val journal = getItem(position)
        holder.jam.text = journal.jam
        holder.imageMood.setImageResource(convertMoodToImage(journal.mood))
        holder.feeling.text = journal.perasaan

        holder.cvMood.setOnClickListener {
            val action = RiwayatTanggalFragmentDirections.actionRiwayatTanggalFragmentToIsiRiwayatFragment(journal)
            holder.itemView.findNavController().navigate(action)
        }
    }

    private fun convertMoodToImage(dataMood: Int?): Int {
        return when (dataMood) {
            1 -> R.drawable.mood1
            2 -> R.drawable.mood2
            3 -> R.drawable.mood3
            4 -> R.drawable.mood4
            5 -> R.drawable.mood5
            else -> R.drawable.mood6
        }
    }
    class DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem == newItem
    }
}