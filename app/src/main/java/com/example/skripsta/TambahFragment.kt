package com.example.skripsta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.data.Item
import com.example.skripsta.data.User
import com.example.skripsta.data.UserViewModel
import com.example.skripsta.adapter.ItemAdapter
import com.example.skripsta.adapter.getDisplayName
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class TambahFragment : Fragment() {

    private lateinit var mUserViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tambah, container, false)

        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.GONE

        view.findViewById<ImageView>(R.id.ic_back).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        view.findViewById<Button>(R.id.btn_save).setOnClickListener {
            insertDataToDatabase(view)
        }

        setupMoodButtons(view)
        setupRecyclerView(view)

        return view
    }

    private fun setupMoodButtons(view: View) {
        val moodButtons = listOf(
            R.id.mood1 to 1, R.id.mood2 to 2, R.id.mood3 to 3,
            R.id.mood4 to 4, R.id.mood5 to 5, R.id.mood6 to 6
        )

        moodButtons.forEach { (id, _) ->
            view.findViewById<ImageButton>(id).setOnClickListener { button ->
                updateMoodSelection(button as ImageButton, moodButtons.map { view.findViewById<ImageButton>(it.first) })
            }
        }
    }

    private fun setupRecyclerView(view: View) {
        val items = listOf(
            Item(R.drawable.ic_bekerja, "Family"),
            Item(R.drawable.ic_belajar, "Friends"),
            Item(R.drawable.ic_belanja, "Beloved"),
            Item(R.drawable.ic_makan, "Colleague"),
            Item(R.drawable.ic_olahraga, "Stranger"),
            Item(R.drawable.ic_renang, "Party"),
            Item(R.drawable.ic_riwayat, "Dating"),
            Item(R.drawable.ic_mood, "Traveling")
        )

        view.findViewById<RecyclerView>(R.id.recycler_view).apply {
            layoutManager = GridLayoutManager(requireContext(), 5)
            adapter = ItemAdapter(items) { selectedItem ->
                items.forEach { it.isSelected = false }
                selectedItem.isSelected = true
                adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun insertDataToDatabase(view: View) {
        val journalContent = view.findViewById<EditText>(R.id.etJornal)?.text.toString()
        val moodType = getSelectedMoodType(view)
        val selectedFeeling = getSelectedChipText(view) // Sekarang mengambil semua chip yang dipilih
        val selectedActivity = getSelectedActivity(view)

        // Validasi input
        if (journalContent.isBlank() || moodType == null || selectedFeeling == null || selectedActivity == null) {
            Toast.makeText(requireContext(), "Lengkapi semua data sebelum menyimpan!", Toast.LENGTH_SHORT).show()
            return
        } else {
            // Simpan ke database dengan nilai yang benar (chip dipisahkan dengan koma)
            mUserViewModel.addUser(User(0, moodType.toInt(), selectedActivity, selectedFeeling, journalContent))
            Toast.makeText(requireContext(), "Berhasil", Toast.LENGTH_LONG).show()
            parentFragmentManager.popBackStack() // Kembali ke layar sebelumnya
        }
    }

    private fun getSelectedMoodType(view: View): Int? {
        val moodButtons = listOf(
            R.id.mood1 to 1, R.id.mood2 to 2, R.id.mood3 to 3,
            R.id.mood4 to 4, R.id.mood5 to 5, R.id.mood6 to 6
        )
        return moodButtons.firstOrNull { view.findViewById<ImageButton>(it.first).isSelected }?.second
    }

    private fun updateMoodSelection(button: ImageButton, allButtons: List<ImageButton>) {
        allButtons.forEach {
            it.isSelected = false
            it.setBackgroundColor(resources.getColor(R.color.white))
        }
        button.isSelected = true
        button.setBackgroundColor(resources.getColor(R.color.vista))
    }

    private fun getSelectedChipText(view: View): String? {
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroup)
        val selectedChips = mutableListOf<String>()

        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            if (chip.isChecked) {
                selectedChips.add(chip.text.toString()) // Tambahkan teks chip yang dipilih
            }
        }

        return if (selectedChips.isNotEmpty()) selectedChips.joinToString(", ") else null
    }

    private fun getSelectedActivity(view: View): String? {
        val adapter = view.findViewById<RecyclerView>(R.id.recycler_view).adapter as? ItemAdapter
        return adapter?.items?.firstOrNull { it.isSelected }?.getDisplayName()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.VISIBLE
    }
}
