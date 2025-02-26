package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.R

class ActivityRankingAdapter(private var activityList: List<Triple<String, Int, Int>>) :
    RecyclerView.Adapter<ActivityRankingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rankText: TextView = view.findViewById(R.id.tv_rank)
        val activityIcon: ImageView = view.findViewById(R.id.iv_icon)
        val activityName: TextView = view.findViewById(R.id.tv_activity_name)
        val activityCount: TextView = view.findViewById(R.id.tv_activity_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity_ranking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (activityName, count, iconResId) = activityList[position]

        holder.rankText.text = (position + 1).toString()
        holder.activityName.text = activityName
        holder.activityCount.text = "x$count"
        holder.activityIcon.setImageResource(iconResId) // Ambil ikon langsung dari database
    }

    override fun getItemCount(): Int = activityList.size

    fun updateData(newList: List<Triple<String, Int, Int>>) {
        activityList = newList
        notifyDataSetChanged()
    }
}
