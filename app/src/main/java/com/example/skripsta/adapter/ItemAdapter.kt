package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.R
import com.example.skripsta.data.Item

class ItemAdapter(
    val items: List<Item>,
    private val onItemClicked: (List<Item>) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.item_icon)
        private val text: TextView = itemView.findViewById(R.id.item_text)

        fun bind(item: Item) {
            icon.setImageResource(item.iconResId)
            text.text = item.text
            itemView.setBackgroundColor(
                if (item.isSelected) itemView.context.resources.getColor(R.color.vista)
                else itemView.context.resources.getColor(R.color.white)
            )

            itemView.setOnClickListener {
                item.isSelected = !item.isSelected
                notifyDataSetChanged()
                onItemClicked(items.filter { it.isSelected })
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_grid, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}


fun Item.getDisplayName(): String = this.text