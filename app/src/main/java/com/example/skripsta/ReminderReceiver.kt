package com.example.skripsta

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.*

class ReminderReceiver : BroadcastReceiver() {

    private val REQUEST_CODE = 1001

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderReceiver", "onReceive called with action: ${intent.action}")

        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            Log.d("ReminderReceiver", "Handling BOOT_COMPLETED")
            val sharedPreferences = context.getSharedPreferences("ReminderPrefs", Context.MODE_PRIVATE)
            val isReminderActive = sharedPreferences.getBoolean("isReminderActive", false)
            if (isReminderActive) {
                val message = sharedPreferences.getString("reminderMessage", null)
                val hour = sharedPreferences.getInt("reminderHour", -1)
                val minute = sharedPreferences.getInt("reminderMinute", -1)

                if (message != null && hour != -1 && minute != -1) {
                    scheduleNextReminder(context, message, hour, minute)
                    Log.d("ReminderReceiver", "Re-scheduled reminder after boot")
                }
            }
            return
        }

        val message = intent.getStringExtra("message") ?: "Reminder"
        Log.d("ReminderReceiver", "Creating notification with message: $message")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "reminder_channel"
        val channel = NotificationChannel(
            channelId,
            "Reminder Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for reminder notifications"
        }
        notificationManager.createNotificationChannel(channel)
        Log.d("ReminderReceiver", "Notification channel created: $channelId")

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // Replace with R.drawable.ic_notification if available
            .setContentTitle("Daily Reminder")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
        Log.d("ReminderReceiver", "Notification sent with ID: $notificationId")

        // Reschedule the reminder for the next day
        val sharedPreferences = context.getSharedPreferences("ReminderPrefs", Context.MODE_PRIVATE)
        val hour = sharedPreferences.getInt("reminderHour", -1)
        val minute = sharedPreferences.getInt("reminderMinute", -1)
        if (hour != -1 && minute != -1) {
            scheduleNextReminder(context, message, hour, minute)
            Log.d("ReminderReceiver", "Scheduled next reminder")
        }
    }

    private fun scheduleNextReminder(context: Context, message: String, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Check if exact alarms are allowed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.d("ReminderReceiver", "Cannot schedule exact alarms, skipping reschedule")
                return
            }
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("message", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        Log.d("ReminderReceiver", "Scheduling next reminder at: ${calendar.time} (${calendar.timeInMillis})")
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
            Log.d("ReminderReceiver", "Next alarm scheduled successfully")
        } catch (e: SecurityException) {
            Log.e("ReminderReceiver", "Failed to schedule next alarm: ${e.message}")
            // Cannot prompt user from BroadcastReceiver, so just log the error
        }
    }
}