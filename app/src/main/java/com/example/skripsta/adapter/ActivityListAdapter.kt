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
import com.example.skripsta.data.entity.Activity

/**
 * Adapter RecyclerView berbasis ListAdapter
 * untuk menampilkan daftar seluruh aktivitas.
 *
 * Adapter ini menggunakan DiffUtil untuk
 * mengoptimalkan pembaruan data sehingga
 * hanya item yang berubah saja yang di-render ulang.
 *
 * @param onEditClick Callback ketika user memilih menu Edit
 * @param onDeleteClick Callback ketika user memilih menu Delete
 */
class ActivityListAdapter(
    private val onEditClick: (Activity) -> Unit,
    private val onDeleteClick: (Activity) -> Unit
) : ListAdapter<Activity, ActivityListAdapter.ActivityViewHolder>(
    ActivityDiffCallback()
) {

    /**
     * Membuat ViewHolder baru dengan cara
     * meng-inflate layout item_all_activity.xml.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_all_activity, parent, false)
        return ActivityViewHolder(view)
    }

    /**
     * Menghubungkan data Activity dengan ViewHolder
     * berdasarkan posisi item di dalam list.
     */
    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = getItem(position)
        holder.bind(activity, onEditClick, onDeleteClick)
    }

    /**
     * ViewHolder yang bertugas menyimpan dan
     * mengatur komponen UI untuk setiap item Activity.
     */
    class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // TextView untuk menampilkan nama aktivitas
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_activity_name)

        // ImageView untuk menampilkan ikon aktivitas
        private val iconImageView: ImageView = itemView.findViewById(R.id.iv_activity_icon)

        // ImageView untuk menampilkan ikon menu (titik tiga)
        private val moreOptions: ImageView = itemView.findViewById(R.id.iv_more_options)

        /**
         * Menghubungkan data Activity ke tampilan UI.
         *
         * @param activity Data aktivitas yang akan ditampilkan
         * @param onEditClick Callback saat user memilih Edit
         * @param onDeleteClick Callback saat user memilih Delete
         */
        fun bind(
            activity: Activity,
            onEditClick: (Activity) -> Unit,
            onDeleteClick: (Activity) -> Unit
        ) {

            /**
             * Menampilkan nama aktivitas dengan huruf pertama kapital
             * agar tampilan lebih rapi dan konsisten.
             */
            nameTextView.text = activity.name.replaceFirstChar {
                it.uppercaseChar()
            }

            // Menampilkan ikon aktivitas
            iconImageView.setImageResource(activity.iconRes)

            /**
             * Menampilkan PopupMenu ketika ikon more options diklik.
             * Menu ini berisi aksi Edit dan Delete.
             */
            moreOptions.setOnClickListener { view ->

                // Membuat PopupMenu yang terikat pada view ikon
                val popupMenu = PopupMenu(view.context, view)

                // Mengisi menu dari file XML activity_menu
                popupMenu.menuInflater.inflate(
                    R.menu.activity_menu,
                    popupMenu.menu
                )

                /**
                 * Menangani aksi klik pada item menu.
                 */
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {

                        // Aksi edit aktivitas
                        R.id.action_edit -> {
                            onEditClick(activity)
                            true
                        }

                        // Aksi hapus aktivitas
                        R.id.action_delete -> {
                            onDeleteClick(activity)
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
     * perubahan data Activity secara efisien.
     */
    class ActivityDiffCallback : DiffUtil.ItemCallback<Activity>() {

        /**
         * Mengecek apakah dua item merepresentasikan
         * data yang sama (biasanya berdasarkan ID).
         */
        override fun areItemsTheSame(oldItem: Activity, newItem: Activity): Boolean {
            return oldItem.id == newItem.id
        }

        /**
         * Mengecek apakah isi data dua item sama.
         * Jika sama, RecyclerView tidak perlu melakukan rebind.
         */
        override fun areContentsTheSame(oldItem: Activity, newItem: Activity): Boolean {
            return oldItem == newItem
        }
    }
}
