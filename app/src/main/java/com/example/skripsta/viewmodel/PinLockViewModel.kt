package com.example.skripsta.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.skripsta.PinSecureStorage

class PinLockViewModel : ViewModel() {

    fun savePin(context: Context, pin: String) {
        PinSecureStorage.savePin(context, pin)
    }

    fun verifyPin(context: Context, pin: String): Boolean {
        return PinSecureStorage.verifyPin(context, pin)
    }

    fun deletePin(context: Context) {
        PinSecureStorage.deletePin(context)
    }

    fun hasPin(context: Context): Boolean {
        return PinSecureStorage.hasPin(context)
    }
}