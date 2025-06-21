package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.R
import com.example.skripsta.data.Item

class ActivityAdapter(
    private val items: List<Item>,
    private val onItemSelected: (Item) -> Unit
) : RecyclerView.Adapter<ActivityAdapter.ViewHolder>() {

    private var selectedItem: Item? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.item_icon)
        val textView: TextView = itemView.findViewById(R.id.item_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_activity, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.imageView.setImageResource(if (item.isSelected) item.drawableId else item.selectedDrawableId)
        holder.textView.text = item.getDisplayName()
        holder.itemView.isSelected = item.isSelected

        holder.itemView.setOnClickListener {
            selectedItem?.let { prevItem ->
                prevItem.isSelected = false
                notifyItemChanged(items.indexOf(prevItem))
            }
            item.isSelected = true
            selectedItem = item
            notifyItemChanged(position)
            onItemSelected(item)
        }
    }

    override fun getItemCount(): Int = items.size
}