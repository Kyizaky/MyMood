package com.example.skripsta

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.skripsta.data.Reminder
import com.example.skripsta.databinding.FragmentAddReminderBinding

class AddReminderFragment : Fragment() {

    private lateinit var binding: FragmentAddReminderBinding
    private val args: AddReminderFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inisialisasi ViewBinding
        binding = FragmentAddReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup TimePicker (format 24 jam)
        binding.timePicker.setIs24HourView(true)

        val reminder = args.reminder
        if (reminder != null) {
            // Mode edit reminder
            binding.reminderTitle.text = "Edit Reminder"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                binding.timePicker.hour = reminder.hour
                binding.timePicker.minute = reminder.minute
            } else {
                binding.timePicker.currentHour = reminder.hour
                binding.timePicker.currentMinute = reminder.minute
            }
        }

        // Tombol kembali
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // Tombol batal
        binding.cancelButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // Tombol simpan
        binding.saveButton.setOnClickListener {

            // Cek izin notifikasi (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    context,
                    "Notification permission required",
                    Toast.LENGTH_LONG
                ).show()
                findNavController().popBackStack()
                return@setOnClickListener
            }

            // Ambil jam dan menit dari TimePicker
            val hour = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                binding.timePicker.hour
            } else {
                binding.timePicker.currentHour
            }

            val minute = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                binding.timePicker.minute
            } else {
                binding.timePicker.currentMinute
            }

            // Buat objek Reminder
            val resultReminder = if (reminder != null) {
                Reminder(reminder.id, hour, minute)
            } else {
                Reminder(-1, hour, minute) // ID di-set di ReminderFragment
            }

            // Kirim hasil ke ReminderFragment
            val result = Bundle().apply {
                putParcelable("reminder", resultReminder)
            }
            parentFragmentManager.setFragmentResult("addReminderResult", result)
            findNavController().popBackStack()
        }
    }
}
