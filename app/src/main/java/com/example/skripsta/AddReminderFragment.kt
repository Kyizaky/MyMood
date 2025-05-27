package com.example.skripsta

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.skripsta.data.Reminder
import com.example.skripsta.databinding.FragmentAddReminderBinding
import java.util.Calendar

class AddReminderFragment : Fragment() {

    private var _binding: FragmentAddReminderBinding? = null
    private val binding get() = _binding!!

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(context, "Notification permission granted", Toast.LENGTH_SHORT).show()
                saveReminder()
            } else {
                Toast.makeText(context, "Notification permission required to save reminder", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set default time to now
        val calendar = Calendar.getInstance()
        binding.timePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
        binding.timePicker.minute = calendar.get(Calendar.MINUTE)

        binding.cancelButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.saveButton.setOnClickListener {
            // Check notification permission before saving
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(context, "Notification permission required to save reminder", Toast.LENGTH_LONG).show()
                requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                saveReminder()
            }
        }
    }

    private fun saveReminder() {
        val days = mutableListOf<Int>()
        if (binding.dayMonday.isChecked) days.add(Calendar.MONDAY)
        if (binding.dayTuesday.isChecked) days.add(Calendar.TUESDAY)
        if (binding.dayWednesday.isChecked) days.add(Calendar.WEDNESDAY)
        if (binding.dayThursday.isChecked) days.add(Calendar.THURSDAY)
        if (binding.dayFriday.isChecked) days.add(Calendar.FRIDAY)
        if (binding.daySaturday.isChecked) days.add(Calendar.SATURDAY)
        if (binding.daySunday.isChecked) days.add(Calendar.SUNDAY)

        if (days.isEmpty()) {
            Toast.makeText(context, "Please select at least one day", Toast.LENGTH_SHORT).show()
            return
        }

        val hour = binding.timePicker.hour
        val minute = binding.timePicker.minute

        // Pass the new reminder back to ReminderFragment
        val reminder = Reminder(
            id = -1, // Placeholder ID, will be set in ReminderFragment
            hour = hour,
            minute = minute,
            days = days
        )

        parentFragmentManager.setFragmentResult(
            "addReminderResult",
            Bundle().apply {
                putParcelable("newReminder", reminder)
            }
        )

        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}