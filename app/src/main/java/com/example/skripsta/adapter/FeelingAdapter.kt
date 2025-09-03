package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.R

class FeelingAdapter(
    private val feelings: List<String>,
    private val initialFeeling: String? = null,
    private val onFeelingSelected: (String) -> Unit
) : RecyclerView.Adapter<FeelingAdapter.ViewHolder>() {

    private var selectedFeeling: String? = initialFeeling

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

        // Update background based on selection
        val bgRes = if (feeling == selectedFeeling)
            R.drawable.feeling_background_selected
        else
            R.drawable.feeling_background_default

        holder.textFeeling.setBackgroundResource(bgRes)

        holder.itemView.setOnClickListener {
            // Toggle selection: deselect if the same feeling is clicked again
            selectedFeeling = if (selectedFeeling == feeling) null else feeling
            notifyDataSetChanged()
            selectedFeeling?.let { onFeelingSelected(it) }
        }
    }

    override fun getItemCount(): Int = feelings.size
}