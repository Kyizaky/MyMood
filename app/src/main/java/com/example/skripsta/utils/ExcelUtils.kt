package com.example.skripsta.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.skripsta.data.MoodEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

object ExcelUtils {

    suspend fun saveMoodEntriesToExcel(context: Context, moodEntries: List<MoodEntry>) {
        withContext(Dispatchers.IO) {
            try {
                val file = createExcelFile(context, moodEntries)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "✅ File saved to Download: ${file.name}", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "❌ Failed to save: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    suspend fun shareMoodEntriesAsExcel(context: Context, moodEntries: List<MoodEntry>) {
        withContext(Dispatchers.IO) {
            try {
                val file = createExcelFile(context, moodEntries)

                withContext(Dispatchers.Main) {
                    val uri: Uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    context.startActivity(
                        Intent.createChooser(shareIntent, "Share Mood Report")
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "❌ Failed to share: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun createExcelFile(context: Context, moodEntries: List<MoodEntry>): File {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Mood Entries")

        // Header
        val headerRow = sheet.createRow(0)
        val headers = listOf("No", "Mood", "Activities", "Feeling", "Title", "Journal", "Date", "Time")
        headers.forEachIndexed { index, title ->
            headerRow.createCell(index).setCellValue(title)
        }

        // Data
        moodEntries.forEachIndexed { index, entry ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(entry.id.toDouble())
            row.createCell(1).setCellValue(MoodUtils.getMoodText(entry.mood))
            row.createCell(2).setCellValue(entry.activities)
            row.createCell(3).setCellValue(entry.perasaan)
            row.createCell(4).setCellValue(entry.judul)
            row.createCell(5).setCellValue(entry.jurnal)
            row.createCell(6).setCellValue(MoodUtils.formatTanggal(entry.tanggal))
            row.createCell(7).setCellValue(entry.jam)
        }

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) downloadsDir.mkdirs()

        val fileName = "MoodEntries_${System.currentTimeMillis()}.xlsx"
        val file = File(downloadsDir, fileName)

        FileOutputStream(file).use { out ->
            workbook.write(out)
        }

        workbook.close()
        return file
    }
}
