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
    private val onItemClicked: (Item) -> Unit // Callback untuk menangani klik
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.item_icon)
        private val text: TextView = itemView.findViewById(R.id.item_text)

        fun bind(item: Item) {
            // Set drawable berdasarkan status isSelected
            icon.setImageResource(if (item.isSelected) item.selectedDrawableId else item.drawableId)
            text.text = item.getDisplayName()

            // Ubah tampilan berdasarkan status pemilihan
            if (item.isSelected) {
                itemView.setBackgroundColor(itemView.context.resources.getColor(R.color.vista)) // Warna dipilih
            } else {
                itemView.setBackgroundColor(itemView.context.resources.getColor(R.color.white)) // Warna default
            }

            // Atur klik listener
            itemView.setOnClickListener {
                // Logika untuk mengatur status item yang dipilih
                items.forEach { it.isSelected = false } // Set semua item menjadi tidak dipilih
                item.isSelected = true // Tandai item yang diklik sebagai dipilih
                notifyDataSetChanged() // Perbarui tampilan RecyclerView

                onItemClicked(item) // Panggil callback dengan item yang dipilih
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