package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter RecyclerView sederhana yang digunakan
 * untuk menampilkan daftar pilihan (picker).
 *
 * Contoh penggunaan:
 * - Pemilihan bulan
 * - Pemilihan tahun
 * - Pemilihan kategori tertentu
 *
 * @param options daftar teks pilihan yang ditampilkan
 * @param onItemClick callback saat salah satu item dipilih
 */
class PickerAdapter(
    private val options: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<PickerAdapter.PickerViewHolder>() {

    /**
     * Membuat ViewHolder dengan layout bawaan Android
     * yaitu simple_list_item_1.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return PickerViewHolder(view)
    }

    /**
     * Menghubungkan data pilihan dengan tampilan UI
     * dan menangani aksi klik pada item.
     */
    override fun onBindViewHolder(holder: PickerViewHolder, position: Int) {
        val option = options[position]
        holder.bind(option)

        // Ketika item diklik, kirim nilai option ke callback
        holder.itemView.setOnClickListener {
            onItemClick(option)
        }
    }

    /**
     * Mengembalikan jumlah item pilihan.
     */
    override fun getItemCount(): Int = options.size

    /**
     * ViewHolder untuk menampilkan satu item pilihan.
     */
    class PickerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // TextView bawaan dari simple_list_item_1
        private val textView: TextView = itemView.findViewById(android.R.id.text1)

        /**
         * Menampilkan teks pilihan
         * dan mengatur padding agar lebih rapi.
         */
        fun bind(option: String) {
            textView.text = option
            textView.setPadding(16, 16, 16, 16)
        }
    }
}
