package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.R
import com.example.skripsta.utils.MoodUtils

/**
 * Adapter RecyclerView untuk menampilkan
 * legenda mood beserta persentasenya.
 *
 * Biasanya digunakan sebagai keterangan
 * pada grafik atau statistik mood.
 *
 * @param moodData List pasangan data:
 *  - Int    : tipe mood
 *  - String : persentase mood (contoh: "25%")
 */
class MoodLegendAdapter(
    private val moodData: List<Pair<Int, String>>
) : RecyclerView.Adapter<MoodLegendAdapter.ViewHolder>() {

    /**
     * ViewHolder untuk item legenda mood.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        // Ikon yang merepresentasikan mood
        val moodIcon: ImageView = view.findViewById(R.id.mood_icon)

        // Teks persentase mood
        val moodPercentage: TextView = view.findViewById(R.id.mood_percentage)
    }

    /**
     * Membuat ViewHolder dengan
     * meng-inflate layout item_mood_legend.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_legend, parent, false)
        return ViewHolder(view)
    }

    /**
     * Menghubungkan data mood dengan
     * tampilan UI pada setiap item.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        // Mengambil data mood berdasarkan posisi
        val (moodType, percentage) = moodData[position]

        // Menampilkan ikon mood sesuai tipe mood
        holder.moodIcon.setImageResource(
            MoodUtils.getMoodIcon(moodType)
        )

        // Menampilkan persentase mood
        holder.moodPercentage.text = percentage
    }

    /**
     * Mengembalikan jumlah data
     * legenda mood yang ditampilkan.
     */
    override fun getItemCount(): Int = moodData.size
}
