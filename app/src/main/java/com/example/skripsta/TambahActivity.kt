package com.example.skripsta

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TambahActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)

        // Data untuk item grid
        val items = listOf(
            Item(R.drawable.ic_bekerja, "Family"),
            Item(R.drawable.ic_belajar, "Friends"),
            Item(R.drawable.ic_belanja, "Beloved"),
            Item(R.drawable.ic_makan, "Colleague"),
            Item(R.drawable.ic_olahraga, "Stranger"),
            Item(R.drawable.ic_renang, "Party"),
            Item(R.drawable.ic_riwayat, "Dating"),
            Item(R.drawable.ic_mood, "Traveling")
        )

        // Set Adapter dan LayoutManager
        recyclerView.layoutManager = GridLayoutManager(this, 5) // 4 kolom
        recyclerView.adapter = ItemAdapter(items)
    }
}