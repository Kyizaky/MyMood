package com.example.skripsta.utils

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.example.skripsta.data.entity.MoodEntry
import com.example.skripsta.data.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * DatabaseImportUtils berfungsi untuk mengimpor
 * data database aplikasi dari file CSV.
 *
 * File CSV harus memiliki struktur yang sesuai
 * dengan hasil ekspor dari DatabaseExportUtils.
 */
object DatabaseImportUtils {

    /**
     * Mengimpor data database dari file CSV.
     *
     * @param context Context aplikasi
     * @param uri Uri file CSV yang dipilih pengguna
     * @param onDataParsed callback berisi data hasil parsing
     *        (MoodEntry dan User)
     *
     * Proses dijalankan pada thread IO agar tidak
     * mengganggu performa UI.
     */
    suspend fun importDatabaseFromCsv(
        context: Context,
        uri: Uri,
        onDataParsed: (List<MoodEntry>, List<User>) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val moodList = mutableListOf<MoodEntry>()
            val userList = mutableListOf<User>()

            // Membuka file CSV melalui ContentResolver
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                var line: String?
                var currentSection = ""

                // Membaca file baris per baris
                while (reader.readLine().also { line = it } != null) {
                    val text = line!!.trim()

                    // Menentukan section data (USER atau MOOD)
                    if (text.startsWith("===")) {
                        currentSection = if (text.contains("USER")) "USER" else "MOOD"
                        continue
                    }

                    // Lewati baris kosong dan header kolom
                    if (text.isEmpty() || text.startsWith("ID")) continue

                    // Split CSV dengan dukungan teks di dalam tanda kutip
                    val parts = text
                        .split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
                        .map { it.replace("\"", "").trim() }

                    when (currentSection) {

                        // ===== PARSING DATA USER =====
                        "USER" -> {
                            if (parts.size >= 7) {
                                val user = User(
                                    id = parts[0].toInt(),
                                    points = parts[1].toInt(),
                                    lastClaimDate = parts[2].ifEmpty { null },
                                    lastLoginDate = parts[3].ifEmpty { null },
                                    lastMoodEntryDate = parts[4].ifEmpty { null },
                                    unlockedPets = parts[5],
                                    currentPetIndex = parts[6].toInt()
                                )
                                userList.add(user)
                            }
                        }

                        // ===== PARSING DATA MOOD =====
                        "MOOD" -> {
                            if (parts.size >= 10) {
                                val mood = MoodEntry(
                                    id = parts[0].toInt(),
                                    user_Id = parts[1].toInt(),
                                    mood = parts[2].toInt(),
                                    activities = parts[3],
                                    activityIcon = parts[4].toInt(),
                                    perasaan = parts[5],
                                    judul = parts[6],
                                    jurnal = parts[7],
                                    tanggal = parts[8],
                                    jam = parts[9]
                                )
                                moodList.add(mood)
                            }
                        }
                    }
                }

                reader.close()
            }

            // Mengirim hasil parsing ke UI Thread
            withContext(Dispatchers.Main) {
                onDataParsed(moodList, userList)
                Toast.makeText(
                    context,
                    "✅ Import berhasil! ${moodList.size} mood & ${userList.size} user",
                    Toast.LENGTH_LONG
                ).show()
            }

        } catch (e: Exception) {
            e.printStackTrace()

            // Menampilkan pesan error di UI Thread
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "❌ Gagal import: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
