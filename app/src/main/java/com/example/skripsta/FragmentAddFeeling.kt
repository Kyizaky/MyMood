package com.example.skripsta

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.skripsta.data.entity.Feeling
import com.example.skripsta.viewmodel.FeelingViewModel
import kotlinx.coroutines.runBlocking

class FragmentAddFeeling : Fragment() {

    // ViewModel untuk mengelola data feeling
    private lateinit var mFeelingViewModel: FeelingViewModel

    // SharedPreferences untuk menyimpan feeling yang sedang dipilih user
    private lateinit var sharedPreferences: SharedPreferences

    // Argument dari navigation (digunakan untuk mode tambah atau edit)
    private val args: FragmentAddFeelingArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate layout fragment
        val view = inflater.inflate(R.layout.fragment_add_feeling, container, false)

        // Inisialisasi ViewModel
        mFeelingViewModel = ViewModelProvider(this).get(FeelingViewModel::class.java)

        // Inisialisasi SharedPreferences
        sharedPreferences =
            requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        // Inisialisasi komponen UI
        val titleTextView =
            view.findViewById<TextView>(R.id.tv_add_feeling_title)
        val editTextFeeling =
            view.findViewById<EditText>(R.id.edit_text_feeling)
        val saveButton =
            view.findViewById<Button>(R.id.btn_save_feeling)

        // Mengecek apakah fragment berada pada mode edit
        if (args.feelingId != -1) {
            titleTextView.text = "Edit Feeling"
            editTextFeeling.setText(args.feelingName)
            saveButton.text = "Update Feeling"
        }

        // Tombol kembali ke halaman sebelumnya
        view.findViewById<ImageView>(R.id.ic_back).setOnClickListener {
            findNavController().popBackStack()
        }

        // Tombol simpan / update feeling
        saveButton.setOnClickListener {
            saveOrUpdateFeeling(view)
        }

        return view
    }

    // Fungsi untuk menyimpan atau memperbarui data feeling
    private fun saveOrUpdateFeeling(view: View) {

        // Mengambil input nama feeling
        val feelingName =
            view.findViewById<EditText>(R.id.edit_text_feeling)
                .text.toString().trim()

        // Validasi input tidak boleh kosong
        if (feelingName.isBlank()) {
            Toast.makeText(
                requireContext(),
                "Please enter a feeling name",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Validasi jumlah feeling maksimal (10) saat menambah data
        if (args.feelingId == -1) {
            val feelingCount =
                runBlocking { mFeelingViewModel.getFeelingCount() }

            if (feelingCount >= 10) {
                Toast.makeText(
                    requireContext(),
                    "Cannot add more than 10 feelings",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }

        // Update SharedPreferences jika nama feeling diubah saat edit
        if (args.feelingId != -1 && args.feelingName != feelingName) {
            val selectedNames =
                sharedPreferences
                    .getStringSet("selected_feeling_names", emptySet())
                    ?.toMutableSet() ?: mutableSetOf()

            if (args.feelingName in selectedNames) {
                selectedNames.remove(args.feelingName)
                selectedNames.add(feelingName)

                sharedPreferences.edit()
                    .putStringSet("selected_feeling_names", selectedNames)
                    .apply()
            }
        }

        // Membuat objek Feeling
        val feeling = Feeling(
            id = if (args.feelingId != -1) args.feelingId else 0,
            name = feelingName
        )

        // Menyimpan atau memperbarui data ke database
        if (args.feelingId != -1) {
            mFeelingViewModel.updateFeeling(feeling)
            Toast.makeText(
                requireContext(),
                "Feeling updated successfully!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            mFeelingViewModel.addFeeling(feeling)
            Toast.makeText(
                requireContext(),
                "Feeling added successfully!",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Kembali ke fragment sebelumnya
        findNavController().popBackStack()
    }
}
