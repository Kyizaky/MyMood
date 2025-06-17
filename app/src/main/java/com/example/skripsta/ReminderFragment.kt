package com.example.skripsta

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skripsta.data.Reminder
import com.example.skripsta.databinding.FragmentReminderBinding
import com.example.skripsta.databinding.ItemReminderBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class ReminderAdapter(
    private val reminders: MutableList<Reminder>,
    private val onEdit: (Reminder) -> Unit,
    private val onDelete: (Reminder) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<ReminderAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemReminderBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reminder = reminders[position]
        with(holder.binding) {
            reminderMessage.text = reminder.message
            reminderTime.text = String.format("%02d:%02d", reminder.hour, reminder.minute)

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
}

class ReminderFragment : Fragment() {

    private lateinit var binding: FragmentReminderBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapter: ReminderAdapter
    private val reminders = mutableListOf<Reminder>()
    private var nextReminderId: Int = 0

    private val requestExactAlarmPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                reminders.forEach { reminder ->
                    scheduleReminder(reminder)
                }
                android.widget.Toast.makeText(context, "Exact alarm permission granted", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                android.widget.Toast.makeText(context, "Exact alarm permission required", android.widget.Toast.LENGTH_LONG).show()
            }
        }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                android.widget.Toast.makeText(context, "Notification permission granted", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                android.widget.Toast.makeText(context, "Notification permission required for reminders", android.widget.Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("ReminderPrefs", Context.MODE_PRIVATE)
        loadReminders()

        // Check notification permission on fragment open
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // Setup RecyclerView
        adapter = ReminderAdapter(reminders, { reminder ->
            val action = ReminderFragmentDirections.actionReminderFragmentToAddReminderFragment(reminder)
            findNavController().navigate(action)
        }, { reminder -> deleteReminder(reminder) })
        binding.reminderRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.reminderRecyclerView.adapter = adapter

        // Back button
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // FAB to add new reminder
        binding.addReminderFab.setOnClickListener {
            val action = ReminderFragmentDirections.actionReminderFragmentToAddReminderFragment(null)
            findNavController().navigate(action)
        }

        // Listen for result from AddReminderFragment
        setFragmentResultListener("addReminderResult") { _, bundle ->
            val reminder = bundle.getParcelable<Reminder>("reminder")
            if (reminder != null) {
                if (reminder.id == -1) {
                    // Add new reminder
                    val newReminder = Reminder(nextReminderId++, reminder.hour, reminder.minute)
                    reminders.add(newReminder)
                    scheduleReminder(newReminder)
                } else {
                    // Edit existing reminder
                    val index = reminders.indexOfFirst { it.id == reminder.id }
                    if (index != -1) {
                        val updatedReminder = Reminder(reminder.id, reminder.hour, reminder.minute)
                        reminders[index] = updatedReminder
                        cancelReminder(reminder)
                        scheduleReminder(updatedReminder)
                    }
                }
                saveReminders()
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun loadReminders() {
        val gson = Gson()
        val json = sharedPreferences.getString("reminders", null)
        val type = object : TypeToken<List<Reminder>>() {}.type
        if (json != null) {
            reminders.clear()
            reminders.addAll(gson.fromJson(json, type))
        }
        nextReminderId = sharedPreferences.getInt("nextReminderId", 0)
    }

    private fun saveReminders() {
        val gson = Gson()
        val json = gson.toJson(reminders)
        sharedPreferences.edit().putString("reminders", json).apply()
        sharedPreferences.edit().putInt("nextReminderId", nextReminderId).apply()
    }

    private fun deleteReminder(reminder: Reminder) {
        cancelReminder(reminder)
        reminders.remove(reminder)
        saveReminders()
        adapter.notifyDataSetChanged()
    }

    private fun scheduleReminder(reminder: Reminder) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            android.widget.Toast.makeText(context, "Exact alarm permission required", android.widget.Toast.LENGTH_LONG).show()
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            requestExactAlarmPermissionLauncher.launch(intent)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            android.widget.Toast.makeText(context, "Notification permission required", android.widget.Toast.LENGTH_LONG).show()
            requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            return
        }

        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.set(Calendar.HOUR_OF_DAY, reminder.hour)
        calendar.set(Calendar.MINUTE, reminder.minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val currentTime = Calendar.getInstance(TimeZone.getDefault())
        val isSameMinute = (reminder.hour == currentTime.get(Calendar.HOUR_OF_DAY) &&
                reminder.minute == currentTime.get(Calendar.MINUTE))

        if (!isSameMinute && calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            Log.d("ReminderFragment", "Reminder ${reminder.id} scheduled for next day: ${calendar.time}")
        } else {
            Log.d("ReminderFragment", "Reminder ${reminder.id} scheduled for: ${calendar.time}")
        }

        val intent = Intent(requireContext(), ReminderReceiver::class.java).apply {
            putExtra("message", reminder.message)
            putExtra("reminderId", reminder.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            Log.d("ReminderFragment", "Reminder ${reminder.id} alarm set successfully")

            // Trigger immediate notification if set for current minute
            if (isSameMinute) {
                Log.d("ReminderFragment", "Triggering immediate notification for reminder ${reminder.id}")
                requireContext().sendBroadcast(intent)
            }
        } catch (e: SecurityException) {
            android.widget.Toast.makeText(context, "Exact alarm permission required", android.widget.Toast.LENGTH_LONG).show()
            Log.e("ReminderFragment", "Failed to set alarm for reminder ${reminder.id}: ${e.message}")
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            requestExactAlarmPermissionLauncher.launch(intent)
        }
    }

    private fun cancelReminder(reminder: Reminder) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d("ReminderFragment", "Reminder ${reminder.id} alarm canceled")
    }
}