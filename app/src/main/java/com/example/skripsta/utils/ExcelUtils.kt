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
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

/**
 * ExcelUtils
 *
 * Utility class yang bertanggung jawab untuk:
 * 1. Mengonversi data MoodEntry menjadi file Excel (.xlsx)
 * 2. Menyimpan file Excel ke folder Download perangkat
 * 3. Membagikan file Excel melalui aplikasi lain (WhatsApp, Email, Drive, dll)
 *
 * Library yang digunakan:
 * - Apache POI (XSSFWorkbook) untuk manipulasi file Excel
 * - Coroutine untuk menjalankan proses berat di background thread
 */
object ExcelUtils {

    /**
     * Menyimpan data MoodEntry ke dalam file Excel
     * dan otomatis meletakkannya di folder Download.
     *
     * @param context Context aplikasi
     * @param moodEntries List data mood yang akan diekspor
     *
     * Proses penulisan file dilakukan pada Dispatcher.IO
     * agar tidak menghambat performa UI.
     */
    suspend fun saveMoodEntriesToExcel(
        context: Context,
        moodEntries: List<MoodEntry>
    ) {
        withContext(Dispatchers.IO) {
            try {
                // Membuat file Excel dari data mood
                val file = createExcelFile(context, moodEntries)

                // Menampilkan notifikasi sukses di Main Thread
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "✅ File berhasil disimpan di folder Download: ${file.name}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()

                // Menampilkan pesan error ke pengguna
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "❌ Gagal menyimpan file: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /**
     * Membagikan file Excel yang berisi data MoodEntry
     * melalui Intent Share Android.
     *
     * @param context Context aplikasi
     * @param moodEntries List data mood yang akan dibagikan
     *
     * File akan dibuat terlebih dahulu, kemudian dibagikan
     * menggunakan FileProvider agar aman sesuai kebijakan Android.
     */
    suspend fun shareMoodEntriesAsExcel(
        context: Context,
        moodEntries: List<MoodEntry>
    ) {
        withContext(Dispatchers.IO) {
            try {
                // Membuat file Excel sementara
                val file = createExcelFile(context, moodEntries)

                withContext(Dispatchers.Main) {

                    // Mengubah File menjadi Uri menggunakan FileProvider
                    val uri: Uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )

                    // Membuat intent share
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    // Menampilkan chooser aplikasi
                    context.startActivity(
                        Intent.createChooser(shareIntent, "Bagikan Laporan Mood")
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()

                // Menampilkan pesan error
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "❌ Gagal membagikan file: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /**
     * Membuat file Excel (.xlsx) dari daftar MoodEntry.
     *
     * @param context Context aplikasi
     * @param moodEntries List data mood
     * @return File Excel yang telah dibuat
     *
     * Method ini:
     * 1. Membuat workbook Excel
     * 2. Menambahkan header kolom
     * 3. Mengisi data mood per baris
     * 4. Menyimpan file ke folder Download
     */
    private fun createExcelFile(
        context: Context,
        moodEntries: List<MoodEntry>
    ): File {

        // Membuat workbook Excel (.xlsx)
        val workbook = XSSFWorkbook()

        // Membuat satu sheet bernama "Mood Entries"
        val sheet = workbook.createSheet("Mood Entries")

        // ================= HEADER =================
        val headerRow = sheet.createRow(0)
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

        // Menuliskan judul kolom
        headers.forEachIndexed { index, title ->
            headerRow.createCell(index).setCellValue(title)
        }

        // ================= DATA =================
        moodEntries.forEachIndexed { index, entry ->
            val row = sheet.createRow(index + 1)

            // Nomor / ID data
            row.createCell(0).setCellValue(entry.id.toDouble())

            // Konversi nilai mood (int) ke teks
            row.createCell(1).setCellValue(
                MoodUtils.getMoodText(entry.mood)
            )

            // Aktivitas yang dilakukan
            row.createCell(2).setCellValue(entry.activities)

            // Perasaan pengguna
            row.createCell(3).setCellValue(entry.perasaan)

            // Judul jurnal
            row.createCell(4).setCellValue(entry.judul)

            // Isi jurnal
            row.createCell(5).setCellValue(entry.jurnal)

            // Format tanggal agar lebih mudah dibaca
            row.createCell(6).setCellValue(
                MoodUtils.formatTanggal(entry.tanggal)
            )

            // Waktu pencatatan mood
            row.createCell(7).setCellValue(entry.jam)
        }

        // ================= FILE OUTPUT =================

        // Mengambil direktori Download
        val downloadsDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )

        // Membuat folder jika belum ada
        if (!downloadsDir.exists()) downloadsDir.mkdirs()

        // Nama file Excel dengan timestamp agar unik
        val fileName = "MoodEntries_${System.currentTimeMillis()}.xlsx"
        val file = File(downloadsDir, fileName)

        // Menulis workbook ke file
        FileOutputStream(file).use { out ->
            workbook.write(out)
        }

        // Menutup workbook untuk mencegah memory leak
        workbook.close()

        return file
    }
}
