package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.R
import com.example.skripsta.data.MoodEntry

class RiwayatAdapter() : RecyclerView.Adapter<RiwayatAdapter.MyViewHolder>() {

    private var moodList = emptyList<MoodEntry>()

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageMood: ImageView = itemView.findViewById(R.id.imageViewIcon)
        val feeling: TextView = itemView.findViewById(R.id.textViewStory)
        val tgl: TextView = itemView.findViewById(R.id.tv_cal)
        val jam: TextView = itemView.findViewById(R.id.tv_jam)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mood_entry, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = moodList[position]
        holder.tgl.text = currentItem.tanggal
        holder.jam.text = currentItem.jam
        holder.imageMood.setImageResource(convertMoodToImage(currentItem.mood))
        holder.feeling.text = currentItem.perasaan

    }

    private fun convertMoodToImage(dataMood: Int?): Int {
        return when (dataMood) {
            1 -> R.drawable.para1
            2 -> R.drawable.para2
            3 -> R.drawable.para3
            4 -> R.drawable.para4
            5 -> R.drawable.para5
            else -> R.drawable.ic_breathe
        }
    }

    override fun getItemCount(): Int = moodList.size
}
