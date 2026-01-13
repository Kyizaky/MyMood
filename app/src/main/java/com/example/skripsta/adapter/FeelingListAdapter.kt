package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.R
import com.example.skripsta.data.entity.Feeling

/**
 * Adapter RecyclerView berbasis ListAdapter
 * untuk menampilkan daftar seluruh feeling.
 *
 * Adapter ini menggunakan DiffUtil agar
 * pembaruan data lebih efisien dan performa lebih optimal.
 *
 * @param onEditClick Callback ketika user memilih menu Edit
 * @param onDeleteClick Callback ketika user memilih menu Delete
 */
class FeelingListAdapter(
    private val onEditClick: (Feeling) -> Unit,
    private val onDeleteClick: (Feeling) -> Unit
) : ListAdapter<Feeling, FeelingListAdapter.FeelingViewHolder>(
    FeelingDiffCallback()
) {

    /**
     * Membuat ViewHolder baru dengan cara
     * meng-inflate layout item_all_feeling.xml.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeelingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_all_feeling, parent, false)
        return FeelingViewHolder(view)
    }

    /**
     * Menghubungkan data Feeling dengan ViewHolder
     * berdasarkan posisi item di dalam list.
     */
    override fun onBindViewHolder(holder: FeelingViewHolder, position: Int) {
        val feeling = getItem(position)
        holder.bind(feeling, onEditClick, onDeleteClick)
    }

    /**
     * ViewHolder yang bertugas menyimpan dan
     * mengatur komponen UI untuk setiap item Feeling.
     */
    class FeelingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // TextView untuk menampilkan nama feeling
        private val nameTextView: TextView =
            itemView.findViewById(R.id.tv_feeling_name)

        // ImageView untuk menampilkan ikon menu (titik tiga)
        private val moreOptions: ImageView =
            itemView.findViewById(R.id.iv_more_options)

        /**
         * Menghubungkan data Feeling ke tampilan UI.
         *
         * @param feeling Data feeling yang akan ditampilkan
         * @param onEditClick Callback saat user memilih Edit
         * @param onDeleteClick Callback saat user memilih Delete
         */
        fun bind(
            feeling: Feeling,
            onEditClick: (Feeling) -> Unit,
            onDeleteClick: (Feeling) -> Unit
        ) {

            /**
             * Menampilkan nama feeling dengan huruf pertama kapital
             * agar tampilan lebih rapi dan konsisten.
             */
            nameTextView.text = feeling.name.replaceFirstChar {
                it.uppercaseChar()
            }

            /**
             * Menampilkan PopupMenu ketika ikon more options diklik.
             * Menu ini berisi aksi Edit dan Delete.
             */
            moreOptions.setOnClickListener { view ->

                // Membuat PopupMenu yang terikat pada view ikon
                val popupMenu = PopupMenu(view.context, view)

                // Mengisi menu dari file XML feeling_menu
                popupMenu.menuInflater.inflate(
                    R.menu.feeling_menu,
                    popupMenu.menu
                )

                /**
                 * Menangani aksi klik pada item menu.
                 */
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {

                        // Aksi edit feeling
                        R.id.action_edit -> {
                            onEditClick(feeling)
                            true
                        }

                        // Aksi hapus feeling
                        R.id.action_delete -> {
                            onDeleteClick(feeling)
                            true
                        }

                        else -> false
                    }
                }

                // Menampilkan PopupMenu ke layar
                popupMenu.show()
            }
        }
    }

    /**
     * DiffUtil digunakan untuk membandingkan
     * perubahan data Feeling secara efisien.
     */
    class FeelingDiffCallback : DiffUtil.ItemCallback<Feeling>() {

        /**
         * Mengecek apakah dua item merepresentasikan
         * data yang sama berdasarkan ID.
         */
        override fun areItemsTheSame(oldItem: Feeling, newItem: Feeling): Boolean {
            return oldItem.id == newItem.id
        }

        /**
         * Mengecek apakah isi dua item sama.
         * Jika sama, RecyclerView tidak perlu melakukan rebind.
         */
        override fun areContentsTheSame(oldItem: Feeling, newItem: Feeling): Boolean {
            return oldItem == newItem
        }
    }
}
