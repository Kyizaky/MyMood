package com.example.skripsta

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.skripsta.data.entity.Activity
import com.example.skripsta.viewmodel.ActivityViewModel
import com.example.skripsta.data.entity.Icon
import com.example.skripsta.viewmodel.IconViewModel
import kotlinx.coroutines.runBlocking

class FragmentAddActivity : Fragment() {

    // ViewModel untuk data activity
    private lateinit var mActivityViewModel: ActivityViewModel

    // ViewModel untuk data icon
    private lateinit var mIconViewModel: IconViewModel

    // SharedPreferences untuk menyimpan activity yang sedang dipilih
    private lateinit var sharedPreferences: SharedPreferences

    // Argument dari navigation (edit / add mode)
    private val args: FragmentAddActivityArgs by navArgs()

    // Icon yang dipilih user
    private var selectedIcon: Icon? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate layout fragment
        val view = inflater.inflate(R.layout.fragment_add_activity, container, false)

        // Inisialisasi ViewModel
        mActivityViewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)
        mIconViewModel = ViewModelProvider(this).get(IconViewModel::class.java)

        // Inisialisasi SharedPreferences
        sharedPreferences =
            requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        // Inisialisasi komponen UI
        val titleTextView = view.findViewById<TextView>(R.id.tv_add_activity_title)
        val editTextActivity = view.findViewById<EditText>(R.id.edit_text_activity)
        val spinnerIcon = view.findViewById<Spinner>(R.id.spinner_icon)
        val saveButton = view.findViewById<Button>(R.id.btn_save_activity)

        // Mengecek apakah mode edit atau tambah activity
        if (args.activityId != -1) {
            titleTextView.text = "Edit Activity"
            editTextActivity.setText(args.activityName)
            saveButton.text = "Update Activity"
        } else {
            titleTextView.text = "Add New Activity"
            saveButton.text = "Save Activity"
        }

        // Mengamati data icon dari database
        mIconViewModel.allIcons.observe(viewLifecycleOwner) { icons ->

            Log.d(
                "FragmentAddActivity",
                "Icons loaded: ${icons?.map { it.colorRes } ?: "null"}"
            )

            // Jika icon kosong
            if (icons.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "No icons available", Toast.LENGTH_SHORT).show()
                return@observe
            }

            // Adapter custom untuk spinner icon
            val adapter = IconAdapter(requireContext(), icons)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerIcon.adapter = adapter

            // Jika mode edit, set icon yang sebelumnya dipilih
            if (args.activityId != -1) {
                val position = icons.indexOfFirst {
                    it.colorRes == args.selectedIconRes &&
                            it.noColorRes == args.iconRes
                }

                if (position != -1) {
                    spinnerIcon.setSelection(position)
                    selectedIcon = icons[position]
                } else {
                    spinnerIcon.setSelection(0)
                    selectedIcon = icons[0]
                    Log.w(
                        "FragmentAddActivity",
                        "No matching icon found for edit, defaulting to first icon"
                    )
                }
            } else {
                spinnerIcon.setSelection(0)
                selectedIcon = icons[0]
            }

            // Listener ketika icon dipilih
            spinnerIcon.setOnItemSelectedListener(
                object : android.widget.AdapterView.OnItemSelectedListener {

                    override fun onItemSelected(
                        parent: android.widget.AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        selectedIcon = icons[position]
                        Log.d(
                            "FragmentAddActivity",
                            "Selected icon: ${selectedIcon?.colorRes}"
                        )
                    }

                    override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
                        selectedIcon = icons[0]
                        Log.d(
                            "FragmentAddActivity",
                            "No icon selected, defaulting to: ${selectedIcon?.colorRes}"
                        )
                    }
                }
            )
        }

        // Tombol kembali
        view.findViewById<ImageView>(R.id.ic_back).setOnClickListener {
            findNavController().popBackStack()
        }

        // Tombol simpan / update activity
        saveButton.setOnClickListener {
            saveOrUpdateActivity(view)
        }

        return view
    }

    // Fungsi untuk menyimpan atau memperbarui activity
    private fun saveOrUpdateActivity(view: View) {

        val activityName =
            view.findViewById<EditText>(R.id.edit_text_activity)
                .text.toString().trim()

        // Validasi input nama activity
        if (activityName.isBlank()) {
            Toast.makeText(
                requireContext(),
                "Please enter an activity name",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Validasi icon
        if (selectedIcon == null) {
            Toast.makeText(
                requireContext(),
                "Please select an icon",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Batas maksimal activity (10)
        if (args.activityId == -1) {
            val activityCount =
                runBlocking { mActivityViewModel.getActivityCount() }

            if (activityCount >= 10) {
                Toast.makeText(
                    requireContext(),
                    "Cannot add more than 10 activities",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }

        // Update SharedPreferences jika nama activity diubah
        if (args.activityId != -1 && args.activityName != activityName) {
            val selectedNames =
                sharedPreferences
                    .getStringSet("selected_activity_names", emptySet())
                    ?.toMutableSet() ?: mutableSetOf()

            if (args.activityName in selectedNames) {
                selectedNames.remove(args.activityName)
                selectedNames.add(activityName)
                sharedPreferences.edit()
                    .putStringSet("selected_activity_names", selectedNames)
                    .apply()
            }
        }

        // Membuat objek Activity
        val activity = Activity(
            id = if (args.activityId != -1) args.activityId else 0,
            name = activityName,
            iconRes = selectedIcon!!.colorRes,
            selectedIconRes = selectedIcon!!.noColorRes
        )

        // Menyimpan atau memperbarui data
        if (args.activityId != -1) {
            mActivityViewModel.updateActivity(activity)
            Toast.makeText(
                requireContext(),
                "Activity updated successfully!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            mActivityViewModel.addActivity(activity)
            Toast.makeText(
                requireContext(),
                "Activity added successfully!",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Kembali ke fragment sebelumnya
        findNavController().popBackStack()
    }

    // Adapter custom untuk spinner icon
    private class IconAdapter(
        context: Context,
        private val icons: List<Icon>
    ) : ArrayAdapter<Icon>(
        context,
        android.R.layout.simple_spinner_item,
        icons
    ) {

        // Tampilan icon pada spinner (selected)
        override fun getView(
            position: Int,
            convertView: View?,
            parent: ViewGroup
        ): View {
            val view =
                super.getView(position, convertView, parent) as TextView
            val icon = icons[position]
            view.text = ""
            view.setCompoundDrawablesWithIntrinsicBounds(
                icon.colorRes, 0, 0, 0
            )
            view.compoundDrawablePadding = 8
            return view
        }

        // Tampilan dropdown icon
        override fun getDropDownView(
            position: Int,
            convertView: View?,
            parent: ViewGroup
        ): View {
            val view =
                super.getDropDownView(position, convertView, parent) as TextView
            val icon = icons[position]
            view.text = "Icon ${position + 1}"
            view.setCompoundDrawablesWithIntrinsicBounds(
                icon.colorRes, 0, 0, 0
            )
            view.compoundDrawablePadding = 8
            return view
        }
    }
}
