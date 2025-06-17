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
import com.example.skripsta.data.Reminder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderReceiver", "onReceive called with action: ${intent.action}")

        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            val sharedPreferences = context.getSharedPreferences("ReminderPrefs", Context.MODE_PRIVATE)
            val gson = Gson()
            val json = sharedPreferences.getString("reminders", null)
            val type = object : TypeToken<List<Reminder>>() {}.type
            val reminders: List<Reminder> = if (json != null) gson.fromJson(json, type) else emptyList()

            reminders.forEach { reminder ->
                scheduleNextReminder(context, reminder)
                Log.d("ReminderReceiver", "Re-scheduled reminder ${reminder.id} after boot")
            }
            return
        }

        val reminderId = intent.getIntExtra("reminderId", -1)
        val message = intent.getStringExtra("message") ?: "Don't forget to fill ur mood today"
        Log.d("ReminderReceiver", "Processing reminder $reminderId with message: $message")

        // Show notification
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

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_home)
            .setContentTitle("Daily Reminder")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationId = reminderId
        notificationManager.notify(notificationId, notification)
        Log.d("ReminderReceiver", "Notification sent with ID: $notificationId")

        // Reschedule the reminder
        val sharedPreferences = context.getSharedPreferences("ReminderPrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("reminders", null)
        val type = object : TypeToken<List<Reminder>>() {}.type
        val reminders: List<Reminder> = if (json != null) gson.fromJson(json, type) else emptyList()
        val reminder = reminders.find { it.id == reminderId }
        if (reminder != null) {
            scheduleNextReminder(context, reminder)
            Log.d("ReminderReceiver", "Scheduled next reminder for ID: $reminderId")
        } else {
            Log.w("ReminderReceiver", "Reminder $reminderId not found in SharedPreferences")
        }
    }

    private fun scheduleNextReminder(context: Context, reminder: Reminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.d("ReminderReceiver", "Cannot schedule exact alarms for reminder ${reminder.id}")
            return
        }

        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.set(Calendar.HOUR_OF_DAY, reminder.hour)
        calendar.set(Calendar.MINUTE, reminder.minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("message", reminder.message)
            putExtra("reminderId", reminder.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.d("ReminderReceiver", "Scheduling reminder ${reminder.id} at: ${calendar.time}")
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
            Log.d("ReminderReceiver", "Reminder ${reminder.id} scheduled successfully")
        } catch (e: SecurityException) {
            Log.e("ReminderReceiver", "Failed to schedule reminder ${reminder.id}: ${e.message}")
        }
    }
}