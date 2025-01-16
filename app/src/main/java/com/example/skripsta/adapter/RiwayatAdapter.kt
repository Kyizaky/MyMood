package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.R
import com.example.skripsta.data.User

class RiwayatAdapter: RecyclerView.Adapter<RiwayatAdapter.MyViewHolder>() {

    private var userList = emptyList<User>()

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val imageMood: ImageView = itemView.findViewById(R.id.imageViewIcon)
        val feeling: TextView = itemView.findViewById(R.id.textViewStory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_mood_entry, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = userList[position]
        holder.imageMood.setImageResource(convertMoodToImage(currentItem.mood))
        holder.feeling.text = currentItem.perasaan
    }
    private fun convertMoodToImage(dataMood: Int?): Int {
        return when {
            dataMood == 1 -> R.drawable.mood1
            dataMood == 2 -> R.drawable.mood2
            dataMood == 3 -> R.drawable.mood3
            dataMood == 4 -> R.drawable.mood4
            dataMood == 5 -> R.drawable.mood5
            else -> R.drawable.mood5
        }
    }

    fun setData(user: List<User>){
        this.userList = user
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}