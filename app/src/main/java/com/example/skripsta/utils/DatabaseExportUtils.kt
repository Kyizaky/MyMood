package com.example.skripsta.utils

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.skripsta.data.entity.MoodEntry
import com.example.skripsta.data.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream

/**
 * DatabaseExportUtils berfungsi untuk mengekspor
 * data database aplikasi ke dalam file CSV.
 *
 * File CSV berisi:
 * - Data User
 * - Data MoodEntry
 *
 * File hasil ekspor akan disimpan ke folder Download.
 */
object DatabaseExportUtils {

    /**
     * Mengekspor data database ke file CSV.
     *
     * @param context Context aplikasi
     * @param moodList daftar data MoodEntry
     * @param userList daftar data User
     *
     * Proses dijalankan pada thread IO menggunakan coroutine
     * agar tidak menghambat UI.
     */
    suspend fun exportDatabaseToCsv(
        context: Context,
        moodList: List<MoodEntry>,
        userList: List<User>
    ) = withContext(Dispatchers.IO) {
        try {
            val fileName = "EunoiaDB.csv"
            val mimeType = "text/csv"

            // 🔹 Menggunakan MediaStore agar kompatibel dan aman
            //     untuk Android 10 (API 29) ke atas
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val resolver = context.contentResolver

            // Membuat file CSV baru di folder Download
            val uri = resolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: throw Exception("Gagal membuat file CSV")

            // Menulis isi CSV ke file
            resolver.openOutputStream(uri)?.use { outputStream ->
                writeCsv(outputStream, moodList, userList)
            }

            // Memastikan file langsung terdeteksi oleh sistem
            MediaScannerConnection.scanFile(
                context,
                arrayOf(
                    Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    ).toString() + "/$fileName"
                ),
                arrayOf("text/csv")
            ) { _, _ -> }

            // Menampilkan notifikasi sukses di UI Thread
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "✅ Database berhasil diekspor ke folder Download!",
                    Toast.LENGTH_LONG
                ).show()
            }

        } catch (e: Exception) {
            e.printStackTrace()

            // Menampilkan notifikasi error di UI Thread
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "❌ Gagal ekspor: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Menuliskan data User dan MoodEntry ke dalam format CSV.
     *
     * @param outputStream stream tujuan penulisan file
     * @param moodList daftar data mood
     * @param userList daftar data user
     */
    private fun writeCsv(
        outputStream: OutputStream,
        moodList: List<MoodEntry>,
        userList: List<User>
    ) {
        outputStream.bufferedWriter().use { writer ->

            // ===== SECTION USER =====
            writer.write("=== USER DATA ===\n")
            writer.write("ID,Points,LastClaimDate,LastLoginDate,LastMoodEntryDate,UnlockedPets,CurrentPetIndex\n")

            userList.forEach {
                writer.write(
                    "${it.id},${it.points},${it.lastClaimDate ?: "-"}," +
                            "${it.lastLoginDate ?: "-"}," +
                            "${it.lastMoodEntryDate ?: "-"}," +
                            "${it.unlockedPets},${it.currentPetIndex}\n"
                )
            }

            // ===== SECTION MOOD =====
            writer.write("\n=== MOOD DATA ===\n")
            writer.write("ID,Mood,Activities,ActivityIcon,Perasaan,Judul,Jurnal,Tanggal,Jam\n")

            moodList.forEach {
                writer.write(
                    "${it.id},${it.mood},${it.activities},${it.activityIcon}," +
                            "${it.perasaan},${escapeCsv(it.judul)}," +
                            "${escapeCsv(it.jurnal)},${it.tanggal},${it.jam}\n"
                )
            }
        }
    }

    /**
     * Mengamankan teks CSV dari karakter khusus seperti tanda kutip.
     *
     * Digunakan agar teks jurnal tidak merusak format CSV.
     */
    private fun escapeCsv(text: String): String {
        return "\"" + text.replace("\"", "\"\"") + "\""
    }
}
