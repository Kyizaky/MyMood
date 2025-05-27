package com.example.skripsta.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsta.data.Feeling
import com.example.skripsta.databinding.ItemFeelingSelectionBinding

class FeelingSelectionAdapter(
    private val feelings: List<Feeling>,
    private val initialSelectedNames: Set<String>,
    private val onSelectionChanged: (List<String>) -> Unit
) : RecyclerView.Adapter<FeelingSelectionAdapter.ViewHolder>() {

    private val selectedNames = mutableSetOf<String>().apply { addAll(initialSelectedNames) }

    inner class ViewHolder(val binding: ItemFeelingSelectionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFeelingSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feeling = feelings[position]
        with(holder.binding) {
            feelingName.text = feeling.name
            checkBoxSelection.isChecked = selectedNames.contains(feeling.name)

            checkBoxSelection.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (selectedNames.size < 5) {
                        selectedNames.add(feeling.name)
                    } else {
                        checkBoxSelection.isChecked = false
                        Toast.makeText(holder.itemView.context, "Maximum 5 feelings can be selected", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    selectedNames.remove(feeling.name)
                }
                onSelectionChanged(selectedNames.toList())
            }
        }
    }

    override fun getItemCount(): Int = feelings.size

    fun getSelectedNames(): List<String> = selectedNames.toList()
}