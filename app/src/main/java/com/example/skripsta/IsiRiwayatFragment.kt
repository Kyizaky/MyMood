package com.example.skripsta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skripsta.adapter.ActivityAdapter
import com.example.skripsta.databinding.FragmentIsiRiwayatBinding
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class IsiRiwayatFragment : Fragment() {

    private val args by navArgs<IsiRiwayatFragmentArgs>()
    private lateinit var binding: FragmentIsiRiwayatBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIsiRiwayatBinding.inflate(inflater, container, false)

        binding.tvTitleP.text = args.currentUser.perasaan
        binding.txtDate.text = args.currentUser.tanggal
        binding.txtTime.text = args.currentUser.jam
        binding.tvIsiJurnal.text = args.currentUser.jurnal
        binding.ivMood.setImageResource(convertMoodToImage(args.currentUser.mood))

        // Set RecyclerView untuk aktivitas
        binding.recyclerViewActivities.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
                justifyContent = JustifyContent.FLEX_START // Atur ke START, CENTER, atau SPACE_AROUND
            }
            adapter = ActivityAdapter(args.currentUser.activities)
        }

        return binding.root
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

