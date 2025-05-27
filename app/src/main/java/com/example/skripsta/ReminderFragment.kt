package com.example.skripsta

import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skripsta.adapter.ReminderAdapter
import com.example.skripsta.data.Reminder
import com.example.skripsta.databinding.DialogAddReminderBinding
import com.example.skripsta.databinding.FragmentReminderBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

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
                Toast.makeText(context, "Exact alarm permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Exact alarm permission required", Toast.LENGTH_LONG).show()
            }
        }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(context, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Notification permission required for reminders", Toast.LENGTH_LONG).show()
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
        adapter = ReminderAdapter(reminders, { reminder -> showEditReminderDialog(reminder) }, { reminder -> deleteReminder(reminder) })
        binding.reminderRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.reminderRecyclerView.adapter = adapter

        // Back button
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // FAB to add new reminder
        binding.addReminderFab.setOnClickListener {
            // Navigate to AddReminderFragment
            findNavController().navigate(R.id.action_reminderFragment_to_addReminderFragment)
        }

        // Listen for result from AddReminderFragment
        setFragmentResultListener("addReminderResult") { _, bundle ->
            val newReminder = bundle.getParcelable<Reminder>("newReminder")
            if (newReminder != null) {
                val reminderWithId = newReminder.copy(id = nextReminderId++)
                reminders.add(reminderWithId)
                saveReminders()
                adapter.notifyDataSetChanged()
                scheduleReminder(reminderWithId)
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

    private fun showEditReminderDialog(reminder: Reminder) {
        val dialog = Dialog(requireContext())
        val dialogBinding = DialogAddReminderBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialogBinding.root.findViewById<TextView>(R.id.reminderTitle).text = "Edit Reminder"

        dialogBinding.selectedTimeTextView.text = String.format("%02d:%02d", reminder.hour, reminder.minute)
        dialogBinding.dayMonday.isChecked = reminder.days.contains(Calendar.MONDAY)
        dialogBinding.dayTuesday.isChecked = reminder.days.contains(Calendar.TUESDAY)
        dialogBinding.dayWednesday.isChecked = reminder.days.contains(Calendar.WEDNESDAY)
        dialogBinding.dayThursday.isChecked = reminder.days.contains(Calendar.THURSDAY)
        dialogBinding.dayFriday.isChecked = reminder.days.contains(Calendar.FRIDAY)
        dialogBinding.daySaturday.isChecked = reminder.days.contains(Calendar.SATURDAY)
        dialogBinding.daySunday.isChecked = reminder.days.contains(Calendar.SUNDAY)

        dialogBinding.saveButton.setOnClickListener {
            // Check notification permission before saving
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(context, "Notification permission required to save reminder", Toast.LENGTH_LONG).show()
                requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                return@setOnClickListener
            }

            val timeText = dialogBinding.selectedTimeTextView.text.toString()
            if (timeText == "No time selected") {
                Toast.makeText(context, "Please select a time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val days = mutableListOf<Int>()
            if (dialogBinding.dayMonday.isChecked) days.add(Calendar.MONDAY)
            if (dialogBinding.dayTuesday.isChecked) days.add(Calendar.TUESDAY)
            if (dialogBinding.dayWednesday.isChecked) days.add(Calendar.WEDNESDAY)
            if (dialogBinding.dayThursday.isChecked) days.add(Calendar.THURSDAY)
            if (dialogBinding.dayFriday.isChecked) days.add(Calendar.FRIDAY)
            if (dialogBinding.daySaturday.isChecked) days.add(Calendar.SATURDAY)
            if (dialogBinding.daySunday.isChecked) days.add(Calendar.SUNDAY)

            if (days.isEmpty()) {
                Toast.makeText(context, "Please select at least one day", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val timeParts = timeText.split(":")
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()

            val updatedReminder = Reminder(reminder.id, hour, minute, days)
            val index = reminders.indexOfFirst { it.id == reminder.id }
            reminders[index] = updatedReminder
            saveReminders()
            adapter.notifyDataSetChanged()
            cancelReminder(reminder)
            scheduleReminder(updatedReminder)
            dialog.dismiss()
        }

        dialogBinding.cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.selectTimeButton.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    dialogBinding.selectedTimeTextView.text = String.format("%02d:%02d", hour, minute)
                },
                reminder.hour,
                reminder.minute,
                true
            ).show()
        }

        dialog.show()
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
            Toast.makeText(context, "Exact alarm permission required", Toast.LENGTH_LONG).show()
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
            Toast.makeText(context, "Notification permission required", Toast.LENGTH_LONG).show()
            requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            return
        }

        val calendar = Calendar.getInstance(TimeZone.getDefault())
        reminder.days.forEach { day ->
            calendar.set(Calendar.DAY_OF_WEEK, day)
            calendar.set(Calendar.HOUR_OF_DAY, reminder.hour)
            calendar.set(Calendar.MINUTE, reminder.minute)
            calendar.set(Calendar.SECOND, 0)
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
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
            } catch (e: SecurityException) {
                Toast.makeText(context, "Exact alarm permission required", Toast.LENGTH_LONG).show()
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                requestExactAlarmPermissionLauncher.launch(intent)
            }
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
    }
}