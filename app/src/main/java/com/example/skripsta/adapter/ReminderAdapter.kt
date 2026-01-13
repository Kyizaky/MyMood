package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.data.Reminder
import com.example.skripsta.databinding.ItemReminderBinding

/**
 * Adapter RecyclerView untuk menampilkan daftar reminder.
 *
 * Setiap item reminder menampilkan:
 * - Pesan reminder
 * - Waktu reminder (jam dan menit)
 * - Menu opsi (Edit & Delete)
 *
 * @param reminders daftar reminder yang akan ditampilkan
 * @param onEdit callback ketika opsi Edit dipilih
 * @param onDelete callback ketika opsi Delete dipilih
 */
class ReminderAdapter(
    private val reminders: MutableList<Reminder>,
    private val onEdit: (Reminder) -> Unit,
    private val onDelete: (Reminder) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ViewHolder>() {

    /**
     * ViewHolder yang menggunakan ViewBinding
     * untuk mengakses komponen UI pada item reminder.
     */
    inner class ViewHolder(val binding: ItemReminderBinding) :
        RecyclerView.ViewHolder(binding.root)

    /**
     * Membuat ViewHolder baru dengan layout item_reminder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReminderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    /**
     * Menghubungkan data reminder ke tampilan UI
     * dan menangani aksi menu Edit dan Delete.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reminder = reminders[position]

        with(holder.binding) {
            // Menampilkan pesan reminder
            reminderMessage.text = reminder.message

            // Menampilkan waktu reminder dengan format HH:mm
            reminderTime.text = String.format("%02d:%02d", reminder.hour, reminder.minute)

            // Menampilkan popup menu saat ikon menu diklik
            reminderMenu.setOnClickListener {
                val popup = PopupMenu(root.context, reminderMenu)
                popup.menu.add("Edit")
                popup.menu.add("Delete")

                popup.setOnMenuItemClickListener { item ->
                    when (item.title) {
                        "Edit" -> onEdit(reminder)
                        "Delete" -> onDelete(reminder)
                    }
                    true
                }
                popup.show()
            }
        }
    }

    /**
     * Mengembalikan jumlah item reminder.
     */
    override fun getItemCount(): Int = reminders.size
}
