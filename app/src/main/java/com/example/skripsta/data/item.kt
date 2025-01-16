package com.example.skripsta.data

data class Item(
    val iconResId: Int,
    val text: String,
    var isSelected: Boolean = false // Tambahkan properti ini
)
