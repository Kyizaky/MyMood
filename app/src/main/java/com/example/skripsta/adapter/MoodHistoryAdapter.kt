package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.R
import com.example.skripsta.data.entity.MoodEntry
import com.example.skripsta.utils.MoodUtils

/**
 * Adapter RecyclerView untuk menampilkan
 * riwayat mood pengguna.
 *
 * Menggunakan ListAdapter dan DiffUtil
 * agar update data lebih efisien dan optimal.
 *
 * @param onItemClick Callback ketika item mood diklik
 */
class MoodHistoryAdapter(
    private val onItemClick: (MoodEntry) -> Unit
) : ListAdapter<MoodEntry, MoodHistoryAdapter.MoodViewHolder>(DiffCallback()) {

    /**
     * Membuat ViewHolder baru dengan
     * meng-inflate layout item_history_entry.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_entry, parent, false)
        return MoodViewHolder(view, onItemClick)
    }

    /**
     * Menghubungkan data MoodEntry
     * ke ViewHolder berdasarkan posisi.
     */
    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder untuk satu item riwayat mood.
     */
    class MoodViewHolder(
        itemView: View,
        private val onItemClick: (MoodEntry) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        // Komponen UI pada item
        private val moodIcon: ImageView = itemView.findViewById(R.id.moodIcon)
        private val moodText: TextView = itemView.findViewById(R.id.moodText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val moodCard: CardView = itemView.findViewById(R.id.moodCardView)

        /**
         * Mengisi data MoodEntry ke tampilan UI.
         */
        fun bind(entry: MoodEntry) {

            // Menampilkan teks mood berdasarkan nilai mood
            moodText.text = MoodUtils.getMoodText(entry.mood)

            // Menampilkan ikon mood
            moodIcon.setImageResource(
                MoodUtils.getMoodIcon(entry.mood)
            )

            // Menampilkan waktu/jam pencatatan mood
            timeText.text = entry.jam

            // Mengatur warna CardView sesuai mood
            val colorResId = MoodUtils.getMoodColor(entry.mood)
            moodCard.setCardBackgroundColor(
                itemView.context.getColor(colorResId)
            )

            // Menangani aksi klik pada item
            itemView.setOnClickListener {
                onItemClick(entry)
            }
        }
    }

    /**
     * DiffUtil digunakan untuk membandingkan
     * data lama dan baru agar RecyclerView
     * hanya me-render item yang berubah.
     */
    class DiffCallback : DiffUtil.ItemCallback<MoodEntry>() {

        // Mengecek apakah dua item merepresentasikan data yang sama
        override fun areItemsTheSame(
            oldItem: MoodEntry,
            newItem: MoodEntry
        ): Boolean {
            return oldItem.id == newItem.id
        }

        // Mengecek apakah isi data dua item benar-benar sama
        override fun areContentsTheSame(
            oldItem: MoodEntry,
            newItem: MoodEntry
        ): Boolean {
            return oldItem == newItem
        }
    }
}
