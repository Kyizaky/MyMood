package com.example.skripsta.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.R
import com.example.skripsta.data.User

class RiwayatAdapter(private val fragment: Fragment) : RecyclerView.Adapter<RiwayatAdapter.MyViewHolder>() {

    private var userList = emptyList<User>()

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageMood: ImageView = itemView.findViewById(R.id.imageViewIcon)
        val feeling: TextView = itemView.findViewById(R.id.textViewStory)
        val tgl: TextView = itemView.findViewById(R.id.tv_cal)
        val jam: TextView = itemView.findViewById(R.id.tv_jam)
        val cvMood: ConstraintLayout = itemView.findViewById(R.id.consMood)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mood_entry, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = userList[position]
        holder.tgl.text = currentItem.tanggal
        holder.jam.text = currentItem.jam
        holder.imageMood.setImageResource(convertMoodToImage(currentItem.mood))
        holder.feeling.text = currentItem.perasaan

    }

    private fun convertMoodToImage(dataMood: Int?): Int {
        return when (dataMood) {
            1 -> R.drawable.mood1
            2 -> R.drawable.mood2
            3 -> R.drawable.mood3
            4 -> R.drawable.mood4
            5 -> R.drawable.mood5
            else -> R.drawable.mood5
        }
    }

    fun setData(user: List<User>) {
        this.userList = user
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = userList.size
}
