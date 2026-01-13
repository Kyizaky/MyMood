package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.data.entity.Activity
import com.example.skripsta.databinding.ItemActivitySelectionBinding

/**
 * Adapter RecyclerView untuk menampilkan daftar aktivitas
 * dengan fitur multi-selection menggunakan CheckBox.
 *
 * Adapter ini memiliki batas maksimal pemilihan
 * sebanyak 5 aktivitas.
 *
 * @param activities List data aktivitas yang ditampilkan
 * @param initialSelectedNames Daftar nama aktivitas yang
 *        sudah terpilih sebelumnya (jika ada)
 * @param onSelectionChanged Callback untuk mengirim
 *        daftar nama aktivitas yang sedang terpilih
 */
class ActivitySelectionAdapter(
    private val activities: List<Activity>,
    private val initialSelectedNames: Set<String>,
    private val onSelectionChanged: (List<String>) -> Unit
) : RecyclerView.Adapter<ActivitySelectionAdapter.ViewHolder>() {

    /**
     * Menyimpan nama aktivitas yang sedang terpilih.
     * Diinisialisasi dari initialSelectedNames.
     */
    private val selectedNames = mutableSetOf<String>().apply {
        addAll(initialSelectedNames)
    }

    /**
     * ViewHolder menggunakan ViewBinding
     * untuk mengakses komponen UI dengan lebih aman.
     */
    inner class ViewHolder(
        val binding: ItemActivitySelectionBinding
    ) : RecyclerView.ViewHolder(binding.root)

    /**
     * Membuat ViewHolder baru dengan cara
     * meng-inflate layout ItemActivitySelectionBinding.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActivitySelectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    /**
     * Menghubungkan data Activity ke ViewHolder
     * berdasarkan posisi item di dalam list.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = activities[position]

        with(holder.binding) {

            // Menampilkan ikon aktivitas
            activityIcon.setImageResource(activity.iconRes)

            // Menampilkan nama aktivitas
            activityName.text = activity.name

            /**
             * Menentukan status CheckBox berdasarkan
             * apakah aktivitas sudah termasuk
             * dalam daftar selectedNames.
             */
            checkBoxSelection.isChecked =
                selectedNames.contains(activity.name)

            /**
             * Menangani perubahan status CheckBox.
             */
            checkBoxSelection.setOnCheckedChangeListener { _, isChecked ->

                if (isChecked) {
                    // Jika CheckBox dicentang
                    if (selectedNames.size < 5) {
                        // Tambahkan aktivitas ke daftar pilihan
                        selectedNames.add(activity.name)
                    } else {
                        // Batalkan centang jika melebihi batas
                        checkBoxSelection.isChecked = false
                        Toast.makeText(
                            holder.itemView.context,
                            "Maximum 5 activities can be selected",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // Jika CheckBox dilepas, hapus dari daftar pilihan
                    selectedNames.remove(activity.name)
                }

                /**
                 * Mengirim daftar terbaru aktivitas
                 * yang sedang terpilih ke Fragment / Activity.
                 */
                onSelectionChanged(selectedNames.toList())
            }
        }
    }

    /**
     * Mengembalikan jumlah total aktivitas
     * yang akan ditampilkan di RecyclerView.
     */
    override fun getItemCount(): Int = activities.size

    /**
     * Mengambil daftar nama aktivitas
     * yang sedang terpilih.
     */
    fun getSelectedNames(): List<String> = selectedNames.toList()
}
