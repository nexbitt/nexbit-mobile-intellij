package com.example.nexbitmobile.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object SecurePrefs {
    private const val PREFS_NAME = "secret_nexbit_shared_prefs"
    private const val KEY_JWT_TOKEN = "jwt_token"

    fun get(context: Context) = EncryptedSharedPreferences.create(
        PREFS_NAME,
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(context: Context, token: String) {
        get(context).edit().putString(KEY_JWT_TOKEN, token).apply()
    }

    fun getToken(context: Context): String? {
        return get(context).getString(KEY_JWT_TOKEN, null)
    }

    fun clearToken(context: Context) {
        get(context).edit().remove(KEY_JWT_TOKEN).apply()
    }

    fun clearAll(context: Context) {
        get(context).edit().clear().apply()
    }
}
