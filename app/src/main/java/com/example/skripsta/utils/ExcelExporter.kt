package com.example.skripsta.utils

import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.example.skripsta.data.MoodEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

object ExcelExporter {

    suspend fun exportMoodEntriesToExcel(context: Context, moodEntries: List<MoodEntry>) {
        withContext(Dispatchers.IO) {
            try {
                // Buat workbook Excel
                val workbook = XSSFWorkbook()
                val sheet = workbook.createSheet("Mood Entries")

                // Header kolom
                val headerRow = sheet.createRow(0)
                val headers = listOf("ID", "Mood", "Activities", "Feeling", "Title", "Journal", "Date", "Time")
                headers.forEachIndexed { index, title ->
                    headerRow.createCell(index).setCellValue(title)
                }

                // Isi data
                moodEntries.forEachIndexed { index, entry ->
                    val row = sheet.createRow(index + 1)
                    row.createCell(0).setCellValue(entry.id.toDouble())
                    row.createCell(1).setCellValue(entry.mood.toDouble())
                    row.createCell(2).setCellValue(entry.activities)
                    row.createCell(3).setCellValue(entry.perasaan)
                    row.createCell(4).setCellValue(entry.judul)
                    row.createCell(5).setCellValue(entry.jurnal)
                    row.createCell(6).setCellValue(entry.tanggal)
                    row.createCell(7).setCellValue(entry.jam)
                }

                // Simpan ke folder Download
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()

                val fileName = "MoodEntries_${System.currentTimeMillis()}.xlsx"
                val file = File(downloadsDir, fileName)

                FileOutputStream(file).use { out ->
                    workbook.write(out)
                }

                workbook.close()

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "✅ File saved to Download: $fileName", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "❌ Failed to export: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
