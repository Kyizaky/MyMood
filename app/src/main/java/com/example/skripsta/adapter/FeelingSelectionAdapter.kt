package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.data.entity.Feeling
import com.example.skripsta.databinding.ItemFeelingSelectionBinding

/**
 * Adapter RecyclerView untuk menampilkan daftar feeling
 * dengan fitur multi-selection menggunakan CheckBox.
 *
 * Adapter ini membatasi jumlah pilihan
 * maksimal sebanyak 5 feeling.
 *
 * @param feelings List data feeling yang akan ditampilkan
 * @param initialSelectedNames Daftar nama feeling yang
 *        sudah terpilih sebelumnya
 * @param onSelectionChanged Callback untuk mengirim
 *        daftar nama feeling yang sedang terpilih
 */
class FeelingSelectionAdapter(
    private val feelings: List<Feeling>,
    private val initialSelectedNames: Set<String>,
    private val onSelectionChanged: (List<String>) -> Unit
) : RecyclerView.Adapter<FeelingSelectionAdapter.ViewHolder>() {

    /**
     * Menyimpan nama feeling yang sedang terpilih.
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
        val binding: ItemFeelingSelectionBinding
    ) : RecyclerView.ViewHolder(binding.root)

    /**
     * Membuat ViewHolder baru dengan cara
     * meng-inflate layout ItemFeelingSelectionBinding.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFeelingSelectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    /**
     * Menghubungkan data Feeling ke ViewHolder
     * berdasarkan posisi item di dalam list.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feeling = feelings[position]

        with(holder.binding) {

            // Menampilkan nama feeling
            feelingName.text = feeling.name

            /**
             * Menentukan status CheckBox berdasarkan
             * apakah feeling sudah termasuk
             * dalam daftar selectedNames.
             */
            checkBoxSelection.isChecked =
                selectedNames.contains(feeling.name)

            /**
             * Menangani perubahan status CheckBox.
             */
            checkBoxSelection.setOnCheckedChangeListener { _, isChecked ->

                if (isChecked) {
                    // Jika CheckBox dicentang
                    if (selectedNames.size < 5) {
                        // Tambahkan feeling ke daftar pilihan
                        selectedNames.add(feeling.name)
                    } else {
                        // Batalkan centang jika melebihi batas maksimal
                        checkBoxSelection.isChecked = false
                        Toast.makeText(
                            holder.itemView.context,
                            "Maximum 5 feelings can be selected",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // Jika CheckBox dilepas, hapus dari daftar pilihan
                    selectedNames.remove(feeling.name)
                }

                /**
                 * Mengirim daftar terbaru feeling
                 * yang sedang terpilih ke Fragment / Activity.
                 */
                onSelectionChanged(selectedNames.toList())
            }
        }
    }

    /**
     * Mengembalikan jumlah total feeling
     * yang akan ditampilkan di RecyclerView.
     */
    override fun getItemCount(): Int = feelings.size

    /**
     * Mengambil daftar nama feeling
     * yang sedang terpilih.
     */
    fun getSelectedNames(): List<String> = selectedNames.toList()
}
