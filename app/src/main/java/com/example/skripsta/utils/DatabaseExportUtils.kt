package com.example.skripsta.utils

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.skripsta.data.MoodEntry
import com.example.skripsta.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream

object DatabaseExportUtils {

    suspend fun exportDatabaseToCsv(
        context: Context,
        moodList: List<MoodEntry>,
        userList: List<User>
    ) = withContext(Dispatchers.IO) {
        try {
            val fileName = "EunoiaDB.csv"
            val mimeType = "text/csv"

            // 🔹 Gunakan MediaStore (aman untuk Android 10+)
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw Exception("Gagal membuat file CSV")

            resolver.openOutputStream(uri)?.use { outputStream ->
                writeCsv(outputStream, moodList, userList)
            }

            MediaScannerConnection.scanFile(context, arrayOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/$fileName"), arrayOf("text/csv")) { _, _ -> }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "✅ Database berhasil diekspor ke folder Download!", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "❌ Gagal ekspor: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun writeCsv(outputStream: OutputStream, moodList: List<MoodEntry>, userList: List<User>) {
        outputStream.bufferedWriter().use { writer ->
            // USER SECTION
            writer.write("=== USER DATA ===\n")
            writer.write("ID,Points,LastClaimDate,LastLoginDate,LastMoodEntryDate,UnlockedPets,CurrentPetIndex\n")
            userList.forEach {
                writer.write("${it.id},${it.points},${it.lastClaimDate ?: "-"},${it.lastLoginDate ?: "-"},${it.lastMoodEntryDate ?: "-"},${it.unlockedPets},${it.currentPetIndex}\n")
            }

            writer.write("\n=== MOOD DATA ===\n")
            writer.write("ID,Mood,Activities,ActivityIcon,Perasaan,Judul,Jurnal,Tanggal,Jam\n")
            moodList.forEach {
                writer.write("${it.id},${it.mood},${it.activities},${it.activityIcon},${it.perasaan},${escapeCsv(it.judul)},${escapeCsv(it.jurnal)},${it.tanggal},${it.jam}\n")
            }
        }
    }

    private fun escapeCsv(text: String): String {
        return "\"" + text.replace("\"", "\"\"") + "\""
    }
}
