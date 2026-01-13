package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.R

/**
 * Adapter RecyclerView untuk menampilkan
 * peringkat aktivitas berdasarkan jumlah kemunculan.
 *
 * Data yang digunakan berupa Triple:
 * - String  : nama aktivitas
 * - Int     : jumlah aktivitas
 * - Int     : resource ID ikon aktivitas
 */
class ActivityRankingAdapter(
    private var activityList: List<Triple<String, Int, Int>>
) : RecyclerView.Adapter<ActivityRankingAdapter.ViewHolder>() {

    /**
     * ViewHolder bertugas menyimpan referensi
     * komponen UI pada setiap item ranking aktivitas.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        // TextView untuk menampilkan peringkat (1, 2, 3, dst)
        val rankText: TextView = view.findViewById(R.id.tv_rank)

        // ImageView untuk menampilkan ikon aktivitas
        val activityIcon: ImageView = view.findViewById(R.id.iv_icon)

        // TextView untuk menampilkan nama aktivitas
        val activityName: TextView = view.findViewById(R.id.tv_activity_name)

        // TextView untuk menampilkan jumlah kemunculan aktivitas
        val activityCount: TextView = view.findViewById(R.id.tv_activity_count)
    }

    /**
     * Membuat ViewHolder baru dengan cara
     * meng-inflate layout item_activity_ranking.xml.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity_ranking, parent, false)
        return ViewHolder(view)
    }

    /**
     * Menghubungkan data ranking aktivitas
     * ke ViewHolder berdasarkan posisi item.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        // Destructuring Triple menjadi variabel yang lebih jelas
        val (activityName, count, iconResId) = activityList[position]

        /**
         * Menampilkan peringkat aktivitas.
         * position dimulai dari 0, sehingga perlu ditambah 1.
         */
        holder.rankText.text = (position + 1).toString()

        // Menampilkan nama aktivitas
        holder.activityName.text = activityName

        // Menampilkan jumlah aktivitas dengan format "x{jumlah}"
        holder.activityCount.text = "x$count"

        /**
         * Menampilkan ikon aktivitas.
         * Ikon diambil langsung dari resource ID
         * yang berasal dari database.
         */
        holder.activityIcon.setImageResource(iconResId)
    }

    /**
     * Mengembalikan jumlah total data ranking aktivitas.
     */
    override fun getItemCount(): Int = activityList.size

    /**
     * Memperbarui data ranking aktivitas.
     * Setelah data diperbarui, RecyclerView akan
     * me-render ulang seluruh item.
     *
     * @param newList List data terbaru hasil perhitungan ranking
     */
    fun updateData(newList: List<Triple<String, Int, Int>>) {
        activityList = newList
        notifyDataSetChanged()
    }
}
