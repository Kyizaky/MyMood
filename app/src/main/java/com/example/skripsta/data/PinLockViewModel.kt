package com.example.skripsta

import android.content.Context
import androidx.lifecycle.ViewModel
import android.content.SharedPreferences

class PinLockViewModel : ViewModel() {

    private companion object {
        const val PREF_NAME = "pin_prefs"
        const val PIN_KEY = "user_pin"
    }

    fun savePin(context: Context, pin: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(PIN_KEY, pin).apply()
    }

    fun verifyPin(context: Context, pin: String): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedPin = prefs.getString(PIN_KEY, null)
        return pin == savedPin
    }

    fun hasPin(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.contains(PIN_KEY)
    }

    fun clearPin(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(PIN_KEY).apply()
    }
}