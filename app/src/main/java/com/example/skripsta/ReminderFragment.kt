package com.example.skripsta

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.skripsta.databinding.FragmentReminderBinding
import java.util.*

class ReminderFragment : Fragment() {

    private lateinit var binding: FragmentReminderBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var selectedTime: Calendar? = null
    private var isReminderActive: Boolean = false
    private val REQUEST_CODE = 1001

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                val message = binding.reminderMessageEditText.text.toString().trim()
                if (message.isNotEmpty() && binding.selectedTimeTextView.text != "No time selected") {
                    if (isReminderActive) {
                        scheduleReminder(message)
                        Toast.makeText(context, getString(R.string.reminder_activated), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.notification_permission_required),
                    Toast.LENGTH_LONG
                ).show()
                binding.reminderSwitch.isChecked = false
                isReminderActive = false
                sharedPreferences.edit().putBoolean("isReminderActive", false).apply()
            }
        }

    private val requestExactAlarmPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (alarmManager.canScheduleExactAlarms()) {
                    val message = sharedPreferences.getString("reminderMessage", null)
                    if (message != null && isReminderActive) {
                        scheduleReminder(message)
                        Toast.makeText(context, getString(R.string.reminder_activated), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.exact_alarm_permission_required),
                        Toast.LENGTH_LONG
                    ).show()
                    binding.reminderSwitch.isChecked = false
                    isReminderActive = false
                    sharedPreferences.edit().putBoolean("isReminderActive", false).apply()
                }
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
        selectedTime = Calendar.getInstance()
        loadSavedReminder()

        binding.selectTimeButton.setOnClickListener {
            showTimePicker()
        }

        binding.saveReminderButton.setOnClickListener {
            saveReminder()
        }

        binding.reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            isReminderActive = isChecked
            sharedPreferences.edit().putBoolean("isReminderActive", isReminderActive).apply()

            if (isChecked) {
                val message = sharedPreferences.getString("reminderMessage", null)
                val hour = sharedPreferences.getInt("reminderHour", -1)
                val minute = sharedPreferences.getInt("reminderMinute", -1)

                if (message != null && hour != -1 && minute != -1) {
                    selectedTime?.set(Calendar.HOUR_OF_DAY, hour)
                    selectedTime?.set(Calendar.MINUTE, minute)
                    selectedTime?.set(Calendar.SECOND, 0)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val notificationPermissionGranted = ContextCompat.checkSelfPermission(
                            requireContext(),
                            android.Manifest.permission.POST_NOTIFICATIONS
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        Log.d("ReminderFragment", "Notification permission granted: $notificationPermissionGranted")
                        if (!notificationPermissionGranted) {
                            requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            return@setOnCheckedChangeListener
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        val canScheduleExactAlarms = alarmManager.canScheduleExactAlarms()
                        Log.d("ReminderFragment", "Can schedule exact alarms: $canScheduleExactAlarms")
                        if (!canScheduleExactAlarms) {
                            Toast.makeText(
                                context,
                                getString(R.string.exact_alarm_permission_required),
                                Toast.LENGTH_LONG
                            ).show()
                            val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                            requestExactAlarmPermissionLauncher.launch(intent)
                            return@setOnCheckedChangeListener
                        }
                    }

                    scheduleReminder(message)
                    Toast.makeText(context, getString(R.string.reminder_activated), Toast.LENGTH_SHORT).show()
                } else {
                    binding.reminderSwitch.isChecked = false
                    isReminderActive = false
                    sharedPreferences.edit().putBoolean("isReminderActive", false).apply()
                    Toast.makeText(context, getString(R.string.please_save_reminder_first), Toast.LENGTH_SHORT).show()
                }
            } else {
                cancelReminder()
                Toast.makeText(context, getString(R.string.reminder_deactivated), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadSavedReminder() {
        val savedMessage = sharedPreferences.getString("reminderMessage", null)
        if (savedMessage != null) {
            binding.reminderMessageEditText.setText(savedMessage)
        }

        val savedHour = sharedPreferences.getInt("reminderHour", -1)
        val savedMinute = sharedPreferences.getInt("reminderMinute", -1)
        if (savedHour != -1 && savedMinute != -1) {
            selectedTime?.set(Calendar.HOUR_OF_DAY, savedHour)
            selectedTime?.set(Calendar.MINUTE, savedMinute)
            selectedTime?.set(Calendar.SECOND, 0)
            binding.selectedTimeTextView.text = String.format("%02d:%02d", savedHour, savedMinute)
        }

        isReminderActive = sharedPreferences.getBoolean("isReminderActive", false)
        binding.reminderSwitch.isChecked = isReminderActive
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                selectedTime?.set(Calendar.HOUR_OF_DAY, selectedHour)
                selectedTime?.set(Calendar.MINUTE, selectedMinute)
                selectedTime?.set(Calendar.SECOND, 0)
                binding.selectedTimeTextView.text = String.format("%02d:%02d", selectedHour, selectedMinute)

                sharedPreferences.edit().apply {
                    putInt("reminderHour", selectedHour)
                    putInt("reminderMinute", selectedMinute)
                }.apply()
            },
            hour, minute, true
        )
        timePickerDialog.show()
    }

    private fun saveReminder() {
        val message = binding.reminderMessageEditText.text.toString().trim()

        if (message.isEmpty()) {
            Toast.makeText(context, getString(R.string.enter_reminder_message), Toast.LENGTH_SHORT).show()
            return
        }

        if (binding.selectedTimeTextView.text == getString(R.string.no_time_selected)) {
            Toast.makeText(context, getString(R.string.select_time), Toast.LENGTH_SHORT).show()
            return
        }

        sharedPreferences.edit().putString("reminderMessage", message).apply()

        if (isReminderActive) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val notificationPermissionGranted = ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                Log.d("ReminderFragment", "Notification permission granted: $notificationPermissionGranted")
                if (!notificationPermissionGranted) {
                    requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    return
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val canScheduleExactAlarms = alarmManager.canScheduleExactAlarms()
                Log.d("ReminderFragment", "Can schedule exact alarms: $canScheduleExactAlarms")
                if (!canScheduleExactAlarms) {
                    Toast.makeText(
                        context,
                        getString(R.string.exact_alarm_permission_required),
                        Toast.LENGTH_LONG
                    ).show()
                    val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    requestExactAlarmPermissionLauncher.launch(intent)
                    return
                }
            }

            scheduleReminder(message)
            Toast.makeText(context, getString(R.string.reminder_set_successfully), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, getString(R.string.reminder_saved_not_active), Toast.LENGTH_LONG).show()
        }
    }

    private fun scheduleReminder(message: String) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), ReminderReceiver::class.java).apply {
            putExtra("message", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, selectedTime!!.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, selectedTime!!.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        Log.d("ReminderFragment", "Scheduling reminder at: ${calendar.time} (${calendar.timeInMillis})")
        Log.d("ReminderFragment", "Current time: ${System.currentTimeMillis()}")

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
            Log.d("ReminderFragment", "Alarm scheduled successfully")
        } catch (e: SecurityException) {
            Log.e("ReminderFragment", "Failed to schedule alarm: ${e.message}")
            Toast.makeText(
                context,
                getString(R.string.exact_alarm_permission_required),
                Toast.LENGTH_LONG
            ).show()
            binding.reminderSwitch.isChecked = false
            isReminderActive = false
            sharedPreferences.edit().putBoolean("isReminderActive", false).apply()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                requestExactAlarmPermissionLauncher.launch(intent)
            }
        }
    }

    private fun cancelReminder() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        Log.d("ReminderFragment", "Reminder canceled")
    }
}