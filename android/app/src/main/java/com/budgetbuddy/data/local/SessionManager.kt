package com.budgetbuddy.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy { createEncryptedPrefs() }

    private fun createEncryptedPrefs(): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            "budget_buddy_session",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    var userId: String?
        get() = prefs.getString("userId", null)
        set(value) = if (value != null) prefs.edit().putString("userId", value).apply()
                     else prefs.edit().remove("userId").apply()

    var displayName: String?
        get() = prefs.getString("displayName", null)
        set(value) = if (value != null) prefs.edit().putString("displayName", value).apply()
                     else prefs.edit().remove("displayName").apply()

    var email: String?
        get() = prefs.getString("email", null)
        set(value) = if (value != null) prefs.edit().putString("email", value).apply()
                     else prefs.edit().remove("email").apply()

    var photoUrl: String?
        get() = prefs.getString("photoUrl", null)
        set(value) = if (value != null) prefs.edit().putString("photoUrl", value).apply()
                     else prefs.edit().remove("photoUrl").apply()

    var loginTimestamp: Long
        get() = prefs.getLong("loginTimestamp", 0L)
        set(value) = prefs.edit().putLong("loginTimestamp", value).apply()

    val isLoggedIn: Boolean get() = userId != null

    fun isSessionExpired(): Boolean {
        if (loginTimestamp == 0L) return false
        return System.currentTimeMillis() - loginTimestamp > SESSION_DURATION_MS
    }

    fun extendSession() {
        loginTimestamp = System.currentTimeMillis()
    }

    fun clear() = prefs.edit().clear().apply()

    companion object {
        private const val SESSION_DURATION_MS = 60 * 60 * 1000L // 1 hour
    }
}
