package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PickerAdapter(
    private val options: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<PickerAdapter.PickerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return PickerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PickerViewHolder, position: Int) {
        val option = options[position]
        holder.bind(option)
        holder.itemView.setOnClickListener {
            onItemClick(option)
        }
    }

    override fun getItemCount(): Int = options.size

    class PickerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(option: String) {
            textView.text = option
            textView.setPadding(16, 16, 16, 16)
        }
    }
}