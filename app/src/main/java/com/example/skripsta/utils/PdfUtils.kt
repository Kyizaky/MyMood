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
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream

object PdfUtils {

    // --- Fungsi utama Save ---
    suspend fun saveMoodEntriesToPdf(context: Context, moodEntries: List<MoodEntry>) {
        withContext(Dispatchers.IO) {
            try {
                val file = createPdfFile(context, moodEntries)

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

    // --- Fungsi utama Share ---
    suspend fun shareMoodEntriesAsPdf(context: Context, moodEntries: List<MoodEntry>) {
        withContext(Dispatchers.IO) {
            try {
                val file = createPdfFile(context, moodEntries)

                withContext(Dispatchers.Main) {
                    val uri: Uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    context.startActivity(Intent.createChooser(shareIntent, "Share Mood Report"))
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "❌ Failed to share: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // --- Pembuatan PDF (dipakai Save & Share) ---
    private fun createPdfFile(context: Context, moodEntries: List<MoodEntry>): File {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) downloadsDir.mkdirs()

        val fileName = "MoodEntries_${System.currentTimeMillis()}.pdf"
        val file = File(downloadsDir, fileName)

        val document = Document()
        PdfWriter.getInstance(document, FileOutputStream(file))
        document.open()

        // Judul
        val title = Paragraph("Mood Entries Report\n\n")
        title.alignment = Element.ALIGN_CENTER
        document.add(title)

        // Tabel data
        val table = PdfPTable(8)
        table.widthPercentage = 100f

        val headers = listOf("No", "Mood", "Activities", "Feeling", "Title", "Journal", "Date", "Time")
        headers.forEach {
            val cell = PdfPCell(Paragraph(it))
            cell.horizontalAlignment = Element.ALIGN_CENTER
            table.addCell(cell)
        }

        moodEntries.forEach { entry ->
            table.addCell(entry.id.toString())
            table.addCell(MoodUtils.getMoodText(entry.mood))
            table.addCell(entry.activities)
            table.addCell(entry.perasaan)
            table.addCell(entry.judul)
            table.addCell(entry.jurnal)
            table.addCell(MoodUtils.formatTanggal(entry.tanggal))
            table.addCell(entry.jam)
        }

        document.add(table)
        document.close()

        return file
    }
}
