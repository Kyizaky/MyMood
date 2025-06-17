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
        binding = FragmentAddReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup TimePicker
        binding.timePicker.setIs24HourView(true)
        val reminder = args.reminder
        if (reminder != null) {
            binding.reminderTitle.text = "Edit Reminder"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                binding.timePicker.hour = reminder.hour
                binding.timePicker.minute = reminder.minute
            } else {
                binding.timePicker.currentHour = reminder.hour
                binding.timePicker.currentMinute = reminder.minute
            }
        }

        // Back button
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // Cancel button
        binding.cancelButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // Save button
        binding.saveButton.setOnClickListener {
            // Check notification permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    context,
                    "Notification permission required to save reminder",
                    Toast.LENGTH_LONG
                ).show()
                findNavController().popBackStack()
                return@setOnClickListener
            }

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

            val resultReminder = if (reminder != null) {
                Reminder(reminder.id, hour, minute)
            } else {
                Reminder(-1, hour, minute) // ID will be set in ReminderFragment
            }

            // Pass result back to ReminderFragment
            val result = Bundle().apply {
                putParcelable("reminder", resultReminder)
            }
            parentFragmentManager.setFragmentResult("addReminderResult", result)
            findNavController().popBackStack()
        }
    }
}