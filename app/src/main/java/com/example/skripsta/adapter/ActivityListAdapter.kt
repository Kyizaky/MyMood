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
import com.example.skripsta.data.Activity

class ActivityListAdapter(
    private val onEditClick: (Activity) -> Unit,
    private val onDeleteClick: (Activity) -> Unit
) : ListAdapter<Activity, ActivityListAdapter.ActivityViewHolder>(ActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_all_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = getItem(position)
        holder.bind(activity, onEditClick, onDeleteClick)
    }

    class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_activity_name)
        private val iconImageView: ImageView = itemView.findViewById(R.id.iv_activity_icon)
        private val moreOptions: ImageView = itemView.findViewById(R.id.iv_more_options)

        fun bind(activity: Activity, onEditClick: (Activity) -> Unit, onDeleteClick: (Activity) -> Unit) {
            nameTextView.text = activity.name.replaceFirstChar { it.uppercaseChar() }
            iconImageView.setImageResource(activity.iconRes)

            moreOptions.setOnClickListener { view ->
                val popupMenu = PopupMenu(view.context, view)
                popupMenu.menuInflater.inflate(R.menu.activity_menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_edit -> {
                            onEditClick(activity)
                            true
                        }
                        R.id.action_delete -> {
                            onDeleteClick(activity)
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }
        }
    }

    class ActivityDiffCallback : DiffUtil.ItemCallback<Activity>() {
        override fun areItemsTheSame(oldItem: Activity, newItem: Activity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Activity, newItem: Activity): Boolean {
            return oldItem == newItem
        }
    }
}