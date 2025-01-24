package com.example.skripsta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.skripsta.data.User

class IsiRiwayatFragment : Fragment(R.layout.fragment_isi_riwayat) {

    private lateinit var imageMood: ImageView
    private lateinit var feelingText: TextView
    private lateinit var aktivitasText: TextView
    private lateinit var jurnalText: TextView
    private lateinit var tanggalJamText: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageMood = view.findViewById(R.id.imageViewMoodDetail)
        feelingText = view.findViewById(R.id.textViewPerasaan)
        aktivitasText = view.findViewById(R.id.textViewAktivitas)
        jurnalText = view.findViewById(R.id.textViewJurnal)
        tanggalJamText = view.findViewById(R.id.textViewTanggalJam)

        // Retrieve data from the arguments
        val currentUser = arguments?.getParcelable<User>("currentUser")

        // Set the data to the views
        currentUser?.let {
            imageMood.setImageResource(convertMoodToImage(it.mood))
            feelingText.text = "Feeling: ${it.perasaan}"
            aktivitasText.text = "Aktivitas: ${it.aktivitas}"
            jurnalText.text = "Jurnal: ${it.jurnal}"
            tanggalJamText.text = "Tanggal & Jam: ${it.tanggal} ${it.jam}"
        }
    }

    private fun convertMoodToImage(mood: Int): Int {
        return when (mood) {
            1 -> R.drawable.mood1
            2 -> R.drawable.mood2
            3 -> R.drawable.mood3
            4 -> R.drawable.mood4
            5 -> R.drawable.mood5
            else -> R.drawable.mood5
        }
    }
}
