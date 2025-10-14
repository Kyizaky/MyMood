package com.example.skripsta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.fragment.findNavController

class ObserveFirstFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_observe_first, container, false)

        val goBack = view.findViewById<ImageView>(R.id.ic_go_back)

        goBack.setOnClickListener {
            val action = ObserveFirstFragmentDirections.actionObserveFirstFragmentToKegiatanFragment()
            findNavController().navigate(action)
        }

        return view
    }

}