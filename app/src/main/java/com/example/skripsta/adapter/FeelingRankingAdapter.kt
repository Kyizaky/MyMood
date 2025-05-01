package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.R

class FeelingRankingAdapter(
    private var feelings: List<Triple<String, Int, Int>> // Nama perasaan, frekuensi, placeholder untuk ikon (tidak digunakan)
) : RecyclerView.Adapter<FeelingRankingAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rankText: TextView = itemView.findViewById(R.id.rank_text)
        private val feelingName: TextView = itemView.findViewById(R.id.activity_name) // Reuse ID dari layout aktivitas
        private val frequencyText: TextView = itemView.findViewById(R.id.frequency_text)

        fun bind(feeling: Triple<String, Int, Int>, position: Int) {
            val rank = position + 1
            rankText.text = rank.toString()
            feelingName.text = feeling.first
            frequencyText.text = "x${feeling.second}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feeling_ranking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(feelings[position], position)
    }

    override fun getItemCount(): Int = feelings.size

    fun updateData(newFeelings: List<Triple<String, Int, Int>>) {
        feelings = newFeelings
        notifyDataSetChanged()
    }
}