package com.example.skripsta.data

data class Item(
    val drawableId: Int, // Drawable untuk status tidak dipilih
    val selectedDrawableId: Int, // Drawable untuk status dipilih
    val text: String,
    var isSelected: Boolean = false
)