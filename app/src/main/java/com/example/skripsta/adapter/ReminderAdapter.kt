package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.data.Reminder
import com.example.skripsta.databinding.ItemReminderBinding
import java.util.*

class ReminderAdapter(
    private val reminders: MutableList<Reminder>,
    private val onEdit: (Reminder) -> Unit,
    private val onDelete: (Reminder) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemReminderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reminder = reminders[position]
        with(holder.binding) {
            reminderMessage.text = reminder.message
            reminderTime.text = String.format("%02d:%02d", reminder.hour, reminder.minute)
            reminderDays.text = reminder.days.map { dayOfWeekToString(it) }.joinToString(", ")

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

    override fun getItemCount(): Int = reminders.size

    private fun dayOfWeekToString(day: Int): String {
        return when (day) {
            Calendar.SUNDAY -> "Sun"
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue"
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            Calendar.SATURDAY -> "Sat"
            else -> ""
        }
    }
}