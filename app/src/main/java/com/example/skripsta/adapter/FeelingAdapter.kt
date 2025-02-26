package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.R

class FeelingAdapter(
    private val feelings: List<String>,
    private val onFeelingSelected: (String) -> Unit
) : RecyclerView.Adapter<FeelingAdapter.ViewHolder>() {

    private var selectedFeeling: String? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textFeeling: TextView = view.findViewById(R.id.textFeeling)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feeling, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feeling = feelings[position]
        holder.textFeeling.text = feeling

        // Ubah tampilan jika dipilih
        holder.textFeeling.setBackgroundResource(
            if (feeling == selectedFeeling) R.drawable.circle_background4
            else R.drawable.rounded_background
        )

        holder.itemView.setOnClickListener {
            selectedFeeling = feeling
            notifyDataSetChanged()
            onFeelingSelected(feeling)
        }
    }

    override fun getItemCount(): Int = feelings.size
}
