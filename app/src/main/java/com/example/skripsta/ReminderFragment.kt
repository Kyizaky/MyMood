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
import com.example.skripsta.adapter.ReminderAdapter
import com.example.skripsta.data.Reminder
import com.example.skripsta.databinding.FragmentReminderBinding
import com.example.skripsta.databinding.ItemReminderBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class ReminderFragment : Fragment() {

    // ViewBinding untuk layout fragment reminder
    private lateinit var binding: FragmentReminderBinding

    // SharedPreferences untuk menyimpan data reminder
    private lateinit var sharedPreferences: SharedPreferences

    // Adapter RecyclerView
    private lateinit var adapter: ReminderAdapter

    // List data reminder
    private val reminders = mutableListOf<Reminder>()

    // ID berikutnya untuk reminder baru
    private var nextReminderId: Int = 0

    // Launcher untuk izin exact alarm (Android 12+)
    private val requestExactAlarmPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->

            // Mengecek apakah izin exact alarm sudah diberikan
            val alarmManager =
                requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                alarmManager.canScheduleExactAlarms()
            ) {
                // Menjadwalkan ulang semua reminder
                reminders.forEach { reminder ->
                    scheduleReminder(reminder)
                }
                android.widget.Toast.makeText(
                    context,
                    "Exact alarm permission granted",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } else {
                android.widget.Toast.makeText(
                    context,
                    "Exact alarm permission required",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }

    // Launcher untuk izin notifikasi (Android 13+)
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                android.widget.Toast.makeText(
                    context,
                    "Notification permission granted",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } else {
                android.widget.Toast.makeText(
                    context,
                    "Notification permission required for reminders",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout menggunakan ViewBinding
        binding = FragmentReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi SharedPreferences
        sharedPreferences =
            requireContext().getSharedPreferences("ReminderPrefs", Context.MODE_PRIVATE)

        // Memuat data reminder dari penyimpanan
        loadReminders()

        // Cek izin notifikasi saat fragment dibuka
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermissionLauncher.launch(
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        }

        // Setup RecyclerView
        adapter = ReminderAdapter(
            reminders,
            { reminder ->
                // Navigasi ke halaman edit reminder
                val action =
                    ReminderFragmentDirections
                        .actionReminderFragmentToAddReminderFragment(reminder)
                findNavController().navigate(action)
            },
            { reminder ->
                // Hapus reminder
                deleteReminder(reminder)
            }
        )

        binding.reminderRecyclerView.layoutManager =
            LinearLayoutManager(context)
        binding.reminderRecyclerView.adapter = adapter

        // Tombol kembali
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // FAB untuk menambah reminder baru
        binding.addReminderFab.setOnClickListener {
            val action =
                ReminderFragmentDirections
                    .actionReminderFragmentToAddReminderFragment(null)
            findNavController().navigate(action)
        }

        // Menerima hasil dari AddReminderFragment
        setFragmentResultListener("addReminderResult") { _, bundle ->
            val reminder = bundle.getParcelable<Reminder>("reminder")
            if (reminder != null) {

                if (reminder.id == -1) {
                    // Menambahkan reminder baru
                    val newReminder =
                        Reminder(nextReminderId++, reminder.hour, reminder.minute)
                    reminders.add(newReminder)
                    scheduleReminder(newReminder)
                } else {
                    // Mengedit reminder yang sudah ada
                    val index =
                        reminders.indexOfFirst { it.id == reminder.id }

                    if (index != -1) {
                        val updatedReminder =
                            Reminder(reminder.id, reminder.hour, reminder.minute)
                        reminders[index] = updatedReminder
                        cancelReminder(reminder)
                        scheduleReminder(updatedReminder)
                    }
                }

                // Simpan data dan update tampilan
                saveReminders()
                adapter.notifyDataSetChanged()
                updateEmptyState()
            }
        }
    }

    // Memuat reminder dari SharedPreferences
    private fun loadReminders() {
        val gson = Gson()
        val json = sharedPreferences.getString("reminders", null)
        val type = object : TypeToken<List<Reminder>>() {}.type

        if (json != null) {
            reminders.clear()
            reminders.addAll(gson.fromJson(json, type))
        }

        nextReminderId = sharedPreferences.getInt("nextReminderId", 0)
        updateEmptyState()
    }

    // Menyimpan reminder ke SharedPreferences
    private fun saveReminders() {
        val gson = Gson()
        val json = gson.toJson(reminders)
        sharedPreferences.edit().putString("reminders", json).apply()
        sharedPreferences.edit().putInt("nextReminderId", nextReminderId).apply()
    }

    // Menghapus reminder
    private fun deleteReminder(reminder: Reminder) {
        cancelReminder(reminder)
        reminders.remove(reminder)
        saveReminders()
        adapter.notifyDataSetChanged()
        updateEmptyState()
    }

    // Menjadwalkan alarm reminder
    private fun scheduleReminder(reminder: Reminder) {
        val alarmManager =
            requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cek izin exact alarm (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            android.widget.Toast.makeText(
                context,
                "Exact alarm permission required",
                android.widget.Toast.LENGTH_LONG
            ).show()

            val intent =
                Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            requestExactAlarmPermissionLauncher.launch(intent)
            return
        }

        // Cek izin notifikasi (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            android.widget.Toast.makeText(
                context,
                "Notification permission required",
                android.widget.Toast.LENGTH_LONG
            ).show()
            requestNotificationPermissionLauncher.launch(
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            return
        }

        // Mengatur waktu alarm
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.set(Calendar.HOUR_OF_DAY, reminder.hour)
        calendar.set(Calendar.MINUTE, reminder.minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val currentTime = Calendar.getInstance(TimeZone.getDefault())

        // Cek apakah waktu sama dengan menit saat ini
        val isSameMinute =
            reminder.hour == currentTime.get(Calendar.HOUR_OF_DAY) &&
                    reminder.minute == currentTime.get(Calendar.MINUTE)

        // Jika waktu sudah lewat, jadwalkan ke hari berikutnya
        if (!isSameMinute && calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            Log.d(
                "ReminderFragment",
                "Reminder ${reminder.id} scheduled for next day: ${calendar.time}"
            )
        } else {
            Log.d(
                "ReminderFragment",
                "Reminder ${reminder.id} scheduled for: ${calendar.time}"
            )
        }

        // Intent untuk BroadcastReceiver
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
            // Set exact alarm
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
                "ReminderFragment",
                "Reminder ${reminder.id} alarm set successfully"
            )

            // Jika waktunya sama dengan menit sekarang, langsung kirim notifikasi
            if (isSameMinute) {
                requireContext().sendBroadcast(intent)
            }

        } catch (e: SecurityException) {
            android.widget.Toast.makeText(
                context,
                "Exact alarm permission required",
                android.widget.Toast.LENGTH_LONG
            ).show()

            Log.e(
                "ReminderFragment",
                "Failed to set alarm for reminder ${reminder.id}: ${e.message}"
            )

            val intent =
                Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            requestExactAlarmPermissionLauncher.launch(intent)
        }
    }

    // Membatalkan alarm reminder
    private fun cancelReminder(reminder: Reminder) {
        val alarmManager =
            requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(requireContext(), ReminderReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)

        Log.d(
            "ReminderFragment",
            "Reminder ${reminder.id} alarm canceled"
        )
    }

    // Mengatur tampilan kosong / ada data
    private fun updateEmptyState() {
        if (reminders.isEmpty()) {
            binding.reminderRecyclerView.visibility = View.GONE
            binding.bellIcon.visibility = View.VISIBLE
            binding.tvbell.visibility = View.VISIBLE
        } else {
            binding.reminderRecyclerView.visibility = View.VISIBLE
            binding.bellIcon.visibility = View.GONE
            binding.tvbell.visibility = View.GONE
        }
    }
}
