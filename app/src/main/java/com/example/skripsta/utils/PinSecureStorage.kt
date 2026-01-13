package com.example.skripsta

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * PinSecureStorage
 *
 * Object ini berfungsi untuk menyimpan dan mengelola PIN pengguna
 * secara AMAN menggunakan EncryptedSharedPreferences.
 *
 * Data PIN:
 * - Tidak disimpan dalam bentuk teks biasa
 * - Dienkripsi menggunakan AES-256
 * - Aman dari akses aplikasi lain maupun root basic
 *
 * Cocok digunakan untuk fitur:
 * - PIN Lock aplikasi
 * - Keamanan data sensitif pengguna
 */
object PinSecureStorage {

    // Nama file SharedPreferences terenkripsi
    private const val PREF_NAME = "secure_pin_prefs"

    // Key untuk menyimpan PIN pengguna
    private const val PIN_KEY = "user_pin"

    /**
     * Fungsi internal untuk mendapatkan instance
     * EncryptedSharedPreferences.
     *
     * Enkripsi yang digunakan:
     * - MasterKey: AES256_GCM
     * - Enkripsi key: AES256_SIV
     * - Enkripsi value: AES256_GCM
     *
     * Kombinasi ini merupakan standar keamanan tinggi
     * yang direkomendasikan oleh Android Security Library.
     */
    private fun getPrefs(context: Context) =
        EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            MasterKey.Builder(context)
                // Skema enkripsi untuk MasterKey
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            // Skema enkripsi untuk KEY SharedPreferences
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            // Skema enkripsi untuk VALUE SharedPreferences
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    /**
     * Menyimpan PIN pengguna ke storage terenkripsi.
     *
     * @param context Context aplikasi
     * @param pin PIN yang dimasukkan pengguna
     *
     * Catatan:
     * - PIN akan langsung dienkripsi
     * - Tidak dapat dibaca sebagai teks biasa
     */
    fun savePin(context: Context, pin: String) {
        getPrefs(context)
            .edit()
            .putString(PIN_KEY, pin)
            .apply()
    }

    /**
     * Memverifikasi PIN yang dimasukkan pengguna.
     *
     * @param context Context aplikasi
     * @param pin PIN yang dimasukkan user
     * @return Boolean (true jika PIN cocok, false jika salah)
     *
     * Mekanisme:
     * - Mengambil PIN tersimpan (sudah didekripsi otomatis)
     * - Membandingkan dengan input pengguna
     */
    fun verifyPin(context: Context, pin: String): Boolean {
        val savedPin = getPrefs(context).getString(PIN_KEY, null)
        return pin == savedPin
    }

    /**
     * Mengecek apakah pengguna sudah memiliki PIN atau belum.
     *
     * @param context Context aplikasi
     * @return true jika PIN sudah diset, false jika belum
     *
     * Digunakan untuk:
     * - Menentukan apakah fitur PIN Lock aktif
     * - Menampilkan halaman setup PIN atau login PIN
     */
    fun hasPin(context: Context): Boolean {
        return getPrefs(context).contains(PIN_KEY)
    }

    /**
     * Menghapus PIN pengguna dari storage.
     *
     * @param context Context aplikasi
     *
     * Digunakan saat:
     * - User menonaktifkan PIN Lock
     * - Reset keamanan aplikasi
     */
    fun deletePin(context: Context) {
        getPrefs(context)
            .edit()
            .remove(PIN_KEY)
            .apply()
    }
}
