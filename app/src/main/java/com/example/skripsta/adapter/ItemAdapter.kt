package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.R
import com.example.skripsta.data.Item

/**
 * Adapter RecyclerView yang berfungsi untuk menampilkan
 * daftar Item dalam bentuk grid atau list.
 *
 * @param items List data Item yang akan ditampilkan.
 *              Setiap Item memiliki ikon, teks, dan status pemilihan.
 * @param onItemClicked Callback yang akan dipanggil
 *                      ketika user memilih salah satu item.
 */
class ItemAdapter(
    val items: List<Item>,
    private val onItemClicked: (Item) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    /**
     * ViewHolder bertugas menyimpan referensi komponen UI
     * dari setiap item agar performa RecyclerView lebih optimal
     * (menghindari pemanggilan findViewById berulang kali).
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // ImageView untuk menampilkan ikon item
        private val icon: ImageView = itemView.findViewById(R.id.item_icon)

        // TextView untuk menampilkan teks/nama item
        private val text: TextView = itemView.findViewById(R.id.item_text)

        /**
         * Fungsi bind digunakan untuk menghubungkan
         * data Item dengan tampilan (UI).
         *
         * @param item Data Item yang akan ditampilkan.
         */
        fun bind(item: Item) {

            /**
             * Mengatur ikon berdasarkan status pemilihan item.
             * - Jika item dipilih, gunakan ikon khusus (selectedDrawableId)
             * - Jika tidak dipilih, gunakan ikon default (drawableId)
             */
            icon.setImageResource(
                if (item.isSelected) item.selectedDrawableId else item.drawableId
            )

            // Menampilkan teks item ke TextView
            text.text = item.getDisplayName()

            /**
             * Mengubah warna latar belakang item
             * untuk memberikan indikasi visual kepada user.
             */
            if (item.isSelected) {
                // Warna latar belakang saat item dipilih
                itemView.setBackgroundColor(
                    itemView.context.resources.getColor(R.color.vista)
                )
            } else {
                // Warna latar belakang default saat item tidak dipilih
                itemView.setBackgroundColor(
                    itemView.context.resources.getColor(R.color.white)
                )
            }

            /**
             * Mengatur aksi ketika item diklik.
             * Pada implementasi ini, hanya satu item
             * yang boleh dipilih dalam satu waktu (single selection).
             */
            itemView.setOnClickListener {

                // Mengatur semua item menjadi tidak terpilih
                items.forEach { it.isSelected = false }

                // Menandai item yang diklik sebagai terpilih
                item.isSelected = true

                /**
                 * Memberi tahu RecyclerView bahwa data berubah
                 * sehingga tampilan akan diperbarui.
                 */
                notifyDataSetChanged()

                // Memanggil callback untuk mengirim item terpilih
                onItemClicked(item)
            }
        }
    }

    /**
     * Membuat ViewHolder baru dengan cara
     * meng-inflate layout item_grid.xml.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_grid, parent, false)
        return ViewHolder(view)
    }

    /**
     * Menghubungkan data Item ke ViewHolder
     * berdasarkan posisi item di dalam list.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    /**
     * Mengembalikan jumlah total item
     * yang akan ditampilkan di RecyclerView.
     */
    override fun getItemCount(): Int = items.size
}

/**
 * Extension function untuk mengambil
 * nama tampilan dari Item.
 * Digunakan agar logika tampilan lebih rapi
 * dan tidak tercampur dengan data model.
 */
fun Item.getDisplayName(): String = this.text
