package com.example.skripsta.utils

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.example.skripsta.data.MoodEntry
import com.example.skripsta.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object DatabaseImportUtils {

    suspend fun importDatabaseFromCsv(
        context: Context,
        uri: Uri,
        onDataParsed: (List<MoodEntry>, List<User>) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val moodList = mutableListOf<MoodEntry>()
            val userList = mutableListOf<User>()

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                var line: String?
                var currentSection = ""

                while (reader.readLine().also { line = it } != null) {
                    val text = line!!.trim()

                    if (text.startsWith("===")) {
                        currentSection = if (text.contains("USER")) "USER" else "MOOD"
                        continue
                    }

                    if (text.isEmpty() || text.startsWith("ID")) continue

                    val parts = text.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
                        .map { it.replace("\"", "").trim() }

                    when (currentSection) {
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

                        "MOOD" -> {
                            if (parts.size >= 9) {
                                val mood = MoodEntry(
                                    id = parts[0].toInt(),
                                    mood = parts[1].toInt(),
                                    activities = parts[2],
                                    activityIcon = parts[3].toInt(),
                                    perasaan = parts[4],
                                    judul = parts[5],
                                    jurnal = parts[6],
                                    tanggal = parts[7],
                                    jam = parts[8]
                                )
                                moodList.add(mood)
                            }
                        }
                    }
                }

                reader.close()
            }

            withContext(Dispatchers.Main) {
                onDataParsed(moodList, userList)
                Toast.makeText(context, "✅ Import berhasil! ${moodList.size} mood & ${userList.size} user", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "❌ Gagal import: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
