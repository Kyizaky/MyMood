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
 * Adapter RecyclerView untuk menampilkan daftar aktivitas.
 * Adapter ini mendukung:
 * - Pemilihan satu item saja (single selection)
 * - Toggle (klik ulang untuk membatalkan pilihan)
 * - Pengiriman data item terpilih atau null
 *
 * @param items List data Item aktivitas
 * @param initialActivityName Nama aktivitas awal yang akan ditandai sebagai terpilih (opsional)
 * @param onItemSelected Callback yang akan mengirim:
 *        - Item jika ada aktivitas yang dipilih
 *        - null jika tidak ada aktivitas yang dipilih
 */
class ActivityAdapter(
    private val items: List<Item>,
    private val initialActivityName: String? = null,
    private val onItemSelected: (Item?) -> Unit
) : RecyclerView.Adapter<ActivityAdapter.ViewHolder>() {

    /**
     * Menyimpan item yang sedang dipilih.
     * Nilai awal diambil berdasarkan initialActivityName jika tersedia.
     */
    private var selectedItem: Item? = items.find { it.text == initialActivityName }

    /**
     * ViewHolder bertugas menyimpan referensi komponen UI
     * pada setiap item agar RecyclerView lebih efisien.
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // ImageView untuk menampilkan ikon aktivitas
        val imageView: ImageView = itemView.findViewById(R.id.item_icon)

        // TextView untuk menampilkan nama aktivitas
        val textView: TextView = itemView.findViewById(R.id.item_name)
    }

    /**
     * Membuat ViewHolder baru dengan cara
     * meng-inflate layout item_activity.xml.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ViewHolder(view)
    }

    /**
     * Menghubungkan data Item dengan ViewHolder
     * berdasarkan posisi item di dalam list.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        /**
         * Mengatur ikon berdasarkan status pemilihan:
         * - Ikon terpilih jika item.isSelected = true
         * - Ikon default jika item.isSelected = false
         */
        holder.imageView.setImageResource(
            if (item.isSelected) item.selectedDrawableId else item.drawableId
        )

        // Menampilkan nama aktivitas
        holder.textView.text = item.getDisplayName()

        // Mengatur state selected pada itemView (berguna untuk selector background)
        holder.itemView.isSelected = item.isSelected

        /**
         * Mengatur aksi klik pada item.
         * Logika yang digunakan:
         * - Jika item yang diklik sudah terpilih → batalkan pilihan (toggle off)
         * - Jika item berbeda → pilih item baru dan batalkan item sebelumnya
         */
        holder.itemView.setOnClickListener {

            if (selectedItem == item) {
                // Jika item yang sama diklik lagi, maka pilihan dibatalkan
                item.isSelected = false
                selectedItem = null

                // Memperbarui tampilan item yang diklik
                notifyItemChanged(position)

                // Mengirim null sebagai tanda tidak ada item terpilih
                onItemSelected(null)

            } else {
                // Jika ada item sebelumnya yang terpilih, batalkan pilihannya
                selectedItem?.let { prevItem ->
                    prevItem.isSelected = false
                    notifyItemChanged(items.indexOf(prevItem))
                }

                // Menandai item baru sebagai terpilih
                item.isSelected = true
                selectedItem = item

                // Memperbarui tampilan item yang baru dipilih
                notifyItemChanged(position)

                // Mengirim item yang terpilih ke Fragment / Activity
                onItemSelected(item)
            }
        }
    }

    /**
     * Mengembalikan jumlah total item
     * yang akan ditampilkan di RecyclerView.
     */
    override fun getItemCount(): Int = items.size
}
