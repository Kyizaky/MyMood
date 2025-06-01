package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.data.Activity
import com.example.skripsta.databinding.ItemActivitySelectionBinding

class ActivitySelectionAdapter(
    private val activities: List<Activity>,
    private val initialSelectedNames: Set<String>,
    private val onSelectionChanged: (List<String>) -> Unit
) : RecyclerView.Adapter<ActivitySelectionAdapter.ViewHolder>() {

    private val selectedNames = mutableSetOf<String>().apply { addAll(initialSelectedNames) }

    inner class ViewHolder(val binding: ItemActivitySelectionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActivitySelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = activities[position]
        with(holder.binding) {
            activityIcon.setImageResource(activity.iconRes)
            activityName.text = activity.name
            checkBoxSelection.isChecked = selectedNames.contains(activity.name)

            checkBoxSelection.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (selectedNames.size < 5) {
                        selectedNames.add(activity.name)
                    } else {
                        checkBoxSelection.isChecked = false
                        Toast.makeText(holder.itemView.context, "Maximum 5 activities can be selected", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    selectedNames.remove(activity.name)
                }
                onSelectionChanged(selectedNames.toList())
            }
        }
    }

    override fun getItemCount(): Int = activities.size

    fun getSelectedNames(): List<String> = selectedNames.toList()
}