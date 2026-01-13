package com.example.skripsta.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.skripsta.data.entity.MoodEntry
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

/**
 * PdfUtils
 *
 * Kelas utilitas yang bertanggung jawab untuk:
 * 1. Mengekspor data MoodEntry ke dalam format PDF
 * 2. Menyimpan file PDF ke folder Download
 * 3. Membagikan file PDF melalui aplikasi lain (Share Intent)
 *
 * Menggunakan library iText untuk pembuatan dokumen PDF.
 * Seluruh proses file dilakukan pada background thread
 * menggunakan Kotlin Coroutines untuk menjaga performa UI.
 */
object PdfUtils {

    /**
     * Fungsi untuk menyimpan data mood ke dalam file PDF
     * dan menyimpannya ke folder Download perangkat.
     *
     * @param context Context aplikasi
     * @param moodEntries Daftar data mood yang akan diekspor
     *
     * Proses:
     * - Berjalan di thread IO
     * - Membuat file PDF
     * - Menampilkan notifikasi Toast jika berhasil atau gagal
     */
    suspend fun saveMoodEntriesToPdf(context: Context, moodEntries: List<MoodEntry>) {
        withContext(Dispatchers.IO) {
            try {
                // Membuat file PDF dari data mood
                val file = createPdfFile(context, moodEntries)

                // Kembali ke Main Thread untuk menampilkan Toast
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "✅ File saved to Download: ${file.name}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "❌ Failed to save: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /**
     * Fungsi untuk membagikan file PDF berisi data mood
     * menggunakan Intent ACTION_SEND.
     *
     * @param context Context aplikasi
     * @param moodEntries Daftar data mood yang akan diekspor
     *
     * File PDF akan dibuat terlebih dahulu,
     * kemudian dibagikan menggunakan FileProvider
     * agar aman dan sesuai dengan kebijakan Android.
     */
    suspend fun shareMoodEntriesAsPdf(context: Context, moodEntries: List<MoodEntry>) {
        withContext(Dispatchers.IO) {
            try {
                // Membuat file PDF
                val file = createPdfFile(context, moodEntries)

                withContext(Dispatchers.Main) {
                    // Mengubah file menjadi URI yang aman
                    val uri: Uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )

                    // Intent untuk membagikan PDF
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
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
                    Toast.makeText(
                        context,
                        "❌ Failed to share: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /**
     * Fungsi inti untuk membuat file PDF.
     * Digunakan baik oleh fungsi Save maupun Share.
     *
     * @param context Context aplikasi
     * @param moodEntries Daftar data mood
     * @return File PDF yang sudah dibuat
     *
     * Struktur PDF:
     * - Judul laporan
     * - Tabel berisi detail mood
     */
    private fun createPdfFile(context: Context, moodEntries: List<MoodEntry>): File {

        // Mengambil direktori Download
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) downloadsDir.mkdirs()

        // Nama file PDF menggunakan timestamp agar unik
        val fileName = "MoodEntries_${System.currentTimeMillis()}.pdf"
        val file = File(downloadsDir, fileName)

        // Inisialisasi dokumen PDF
        val document = Document()
        PdfWriter.getInstance(document, FileOutputStream(file))
        document.open()

        // ================== JUDUL ==================
        val title = Paragraph("Mood Entries Report\n\n")
        title.alignment = Element.ALIGN_CENTER
        document.add(title)

        // ================== TABEL ==================
        // Membuat tabel dengan 8 kolom
        val table = PdfPTable(8)
        table.widthPercentage = 100f

        // Header tabel
        val headers = listOf(
            "No",
            "Mood",
            "Activities",
            "Feeling",
            "Title",
            "Journal",
            "Date",
            "Time"
        )

        headers.forEach {
            val cell = PdfPCell(Paragraph(it))
            cell.horizontalAlignment = Element.ALIGN_CENTER
            table.addCell(cell)
        }

        // Isi tabel dengan data mood
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
