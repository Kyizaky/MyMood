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

    // Fungsi utama yang dipanggil ketika alarm atau broadcast diterima
    override fun onReceive(context: Context, intent: Intent) {

        // Log untuk debugging saat receiver dipanggil
        Log.d("ReminderReceiver", "onReceive called with action: ${intent.action}")

        // Mengecek apakah broadcast berasal dari BOOT_COMPLETED
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {

            // Mengambil data reminder dari SharedPreferences
            val sharedPreferences =
                context.getSharedPreferences("ReminderPrefs", Context.MODE_PRIVATE)
            val gson = Gson()
            val json = sharedPreferences.getString("reminders", null)
            val type = object : TypeToken<List<Reminder>>() {}.type

            // Mengubah JSON menjadi list Reminder
            val reminders: List<Reminder> =
                if (json != null) gson.fromJson(json, type) else emptyList()

            // Menjadwalkan ulang semua reminder setelah device reboot
            reminders.forEach { reminder ->
                scheduleNextReminder(context, reminder)
                Log.d(
                    "ReminderReceiver",
                    "Re-scheduled reminder ${reminder.id} after boot"
                )
            }
            return
        }

        // Mengambil ID reminder dari intent
        val reminderId = intent.getIntExtra("reminderId", -1)

        // Mengambil pesan reminder atau menggunakan pesan default
        val message =
            intent.getStringExtra("message")
                ?: "Don't forget to fill ur mood today"

        Log.d(
            "ReminderReceiver",
            "Processing reminder $reminderId with message: $message"
        )

        // Menampilkan notifikasi
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "reminder_channel"

        // Membuat notification channel (wajib Android 8+)
        val channel = NotificationChannel(
            channelId,
            "Reminder Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for reminder notifications"
        }

        notificationManager.createNotificationChannel(channel)

        // Membangun notifikasi
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_home)
            .setContentTitle("Daily Reminder")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Menggunakan reminderId sebagai notificationId
        val notificationId = reminderId

        // Menampilkan notifikasi ke sistem
        notificationManager.notify(notificationId, notification)

        Log.d(
            "ReminderReceiver",
            "Notification sent with ID: $notificationId"
        )

        // Mengambil ulang data reminder untuk penjadwalan berikutnya
        val sharedPreferences =
            context.getSharedPreferences("ReminderPrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("reminders", null)
        val type = object : TypeToken<List<Reminder>>() {}.type

        val reminders: List<Reminder> =
            if (json != null) gson.fromJson(json, type) else emptyList()

        // Mencari reminder berdasarkan ID
        val reminder = reminders.find { it.id == reminderId }

        if (reminder != null) {
            // Menjadwalkan reminder untuk hari berikutnya
            scheduleNextReminder(context, reminder)
            Log.d(
                "ReminderReceiver",
                "Scheduled next reminder for ID: $reminderId"
            )
        } else {
            // Log jika reminder tidak ditemukan
            Log.w(
                "ReminderReceiver",
                "Reminder $reminderId not found in SharedPreferences"
            )
        }
    }

    // Fungsi untuk menjadwalkan reminder ke hari berikutnya
    private fun scheduleNextReminder(context: Context, reminder: Reminder) {

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Mengecek izin exact alarm untuk Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            Log.d(
                "ReminderReceiver",
                "Cannot schedule exact alarms for reminder ${reminder.id}"
            )
            return
        }

        // Mengatur waktu alarm ke hari berikutnya
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Intent untuk memicu ReminderReceiver kembali
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(
                "message",
                reminder.message ?: "Don't forget to fill your mood today"
            )
            putExtra("reminderId", reminder.id)
        }

        // PendingIntent untuk alarm
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // Menjadwalkan exact alarm
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

            Log.d(
                "ReminderReceiver",
                "Reminder ${reminder.id} scheduled for ${calendar.time}"
            )

        } catch (e: SecurityException) {
            // Menangani error jika izin alarm tidak tersedia
            Log.e(
                "ReminderReceiver",
                "Failed to schedule reminder ${reminder.id}: ${e.message}"
            )
        }
    }
}
