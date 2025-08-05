package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.R
import com.example.skripsta.data.Feeling

class FeelingListAdapter(
    private val onEditClick: (Feeling) -> Unit,
    private val onDeleteClick: (Feeling) -> Unit
) : ListAdapter<Feeling, FeelingListAdapter.FeelingViewHolder>(FeelingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeelingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_all_feeling, parent, false)
        return FeelingViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeelingViewHolder, position: Int) {
        val feeling = getItem(position)
        holder.bind(feeling, onEditClick, onDeleteClick)
    }

    class FeelingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_feeling_name)
        private val moreOptions: ImageView = itemView.findViewById(R.id.iv_more_options)

        fun bind(feeling: Feeling, onEditClick: (Feeling) -> Unit, onDeleteClick: (Feeling) -> Unit) {
            nameTextView.text = feeling.name.replaceFirstChar { it.uppercaseChar() }

            moreOptions.setOnClickListener { view ->
                val popupMenu = PopupMenu(view.context, view)
                popupMenu.menuInflater.inflate(R.menu.feeling_menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_edit -> {
                            onEditClick(feeling)
                            true
                        }
                        R.id.action_delete -> {
                            onDeleteClick(feeling)
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }
        }
    }

    class FeelingDiffCallback : DiffUtil.ItemCallback<Feeling>() {
        override fun areItemsTheSame(oldItem: Feeling, newItem: Feeling): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Feeling, newItem: Feeling): Boolean {
            return oldItem == newItem
        }
    }
}