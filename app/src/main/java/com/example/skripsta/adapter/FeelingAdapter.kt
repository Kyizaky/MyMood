package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.R

/**
 * Adapter RecyclerView untuk menampilkan daftar perasaan (feeling).
 * Adapter ini menerapkan:
 * - Single selection (hanya satu feeling yang dapat dipilih)
 * - Toggle selection (klik ulang untuk membatalkan pilihan)
 *
 * @param feelings List teks perasaan yang akan ditampilkan
 * @param initialFeeling Feeling awal yang sudah terpilih (opsional)
 * @param onFeelingSelected Callback yang mengirim:
 *        - String feeling jika ada yang dipilih
 *        - null jika tidak ada feeling yang dipilih
 */
class FeelingAdapter(
    private val feelings: List<String>,
    private val initialFeeling: String? = null,
    private val onFeelingSelected: (String?) -> Unit
) : RecyclerView.Adapter<FeelingAdapter.ViewHolder>() {

    /**
     * Menyimpan feeling yang sedang terpilih.
     * Diinisialisasi dengan initialFeeling jika tersedia.
     */
    private var selectedFeeling: String? = initialFeeling

    /**
     * ViewHolder bertugas menyimpan referensi
     * komponen UI untuk setiap item feeling.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        // TextView untuk menampilkan teks feeling
        val textFeeling: TextView = view.findViewById(R.id.textFeeling)
    }

    /**
     * Membuat ViewHolder baru dengan cara
     * meng-inflate layout item_feeling.xml.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feeling, parent, false)
        return ViewHolder(view)
    }

    /**
     * Menghubungkan data feeling ke ViewHolder
     * berdasarkan posisi item di dalam list.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feeling = feelings[position]

        // Menampilkan teks feeling
        holder.textFeeling.text = feeling

        /**
         * Mengatur background item berdasarkan
         * status pemilihan feeling.
         */
        val bgRes =
            if (feeling == selectedFeeling)
                R.drawable.feeling_background_selected
            else
                R.drawable.feeling_background_default

        holder.textFeeling.setBackgroundResource(bgRes)

        /**
         * Menangani aksi klik pada item feeling.
         */
        holder.itemView.setOnClickListener {

            if (selectedFeeling == feeling) {
                // Jika feeling yang sama diklik kembali, batalkan pilihan
                selectedFeeling = null

                // Memperbarui tampilan item yang diklik
                notifyItemChanged(position)

                // Mengirim null sebagai tanda tidak ada feeling terpilih
                onFeelingSelected(null)

            } else {
                // Jika memilih feeling baru, batalkan feeling sebelumnya
                val previousIndex = feelings.indexOf(selectedFeeling)

                selectedFeeling = feeling

                // Memperbarui tampilan feeling sebelumnya (jika ada)
                if (previousIndex != -1) {
                    notifyItemChanged(previousIndex)
                }

                // Memperbarui tampilan feeling yang baru dipilih
                notifyItemChanged(position)

                // Mengirim feeling yang terpilih
                onFeelingSelected(feeling)
            }
        }
    }

    /**
     * Mengembalikan jumlah total feeling
     * yang akan ditampilkan di RecyclerView.
     */
    override fun getItemCount(): Int = feelings.size
}
