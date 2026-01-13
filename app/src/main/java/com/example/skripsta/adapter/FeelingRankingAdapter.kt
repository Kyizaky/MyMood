package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.R

/**
 * Adapter RecyclerView untuk menampilkan
 * peringkat feeling berdasarkan frekuensi kemunculan.
 *
 * Data disimpan dalam bentuk Triple:
 * - String : nama feeling
 * - Int    : jumlah kemunculan
 * - Int    : placeholder ikon (tidak digunakan pada adapter ini)
 */
class FeelingRankingAdapter(
    private var feelings: List<Triple<String, Int, Int>>
) : RecyclerView.Adapter<FeelingRankingAdapter.ViewHolder>() {

    /**
     * ViewHolder bertugas menyimpan referensi
     * komponen UI pada setiap item ranking feeling.
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // TextView untuk menampilkan peringkat (1, 2, 3, dst)
        private val rankText: TextView =
            itemView.findViewById(R.id.rank_text)

        /**
         * TextView untuk menampilkan nama feeling.
         * Menggunakan ID yang sama dengan layout aktivitas
         * untuk menjaga konsistensi tampilan.
         */
        private val feelingName: TextView =
            itemView.findViewById(R.id.activity_name)

        // TextView untuk menampilkan frekuensi feeling
        private val frequencyText: TextView =
            itemView.findViewById(R.id.frequency_text)

        /**
         * Menghubungkan data feeling ke tampilan UI.
         *
         * @param feeling Data feeling dalam bentuk Triple
         * @param position Posisi item dalam RecyclerView
         */
        fun bind(feeling: Triple<String, Int, Int>, position: Int) {

            /**
             * Menentukan peringkat berdasarkan posisi.
             * Karena posisi dimulai dari 0, maka perlu ditambah 1.
             */
            val rank = position + 1
            rankText.text = rank.toString()

            // Menampilkan nama feeling
            feelingName.text = feeling.first

            // Menampilkan frekuensi feeling dengan format "x{jumlah}"
            frequencyText.text = "x${feeling.second}"
        }
    }

    /**
     * Membuat ViewHolder baru dengan cara
     * meng-inflate layout item_feeling_ranking.xml.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feeling_ranking, parent, false)
        return ViewHolder(view)
    }

    /**
     * Menghubungkan data ranking feeling ke ViewHolder
     * berdasarkan posisi item di dalam list.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(feelings[position], position)
    }

    /**
     * Mengembalikan jumlah total data ranking feeling.
     */
    override fun getItemCount(): Int = feelings.size

    /**
     * Memperbarui data ranking feeling.
     * Setelah data diperbarui, RecyclerView akan
     * me-render ulang seluruh item.
     *
     * @param newFeelings List data ranking terbaru
     */
    fun updateData(newFeelings: List<Triple<String, Int, Int>>) {
        feelings = newFeelings
        notifyDataSetChanged()
    }
}
