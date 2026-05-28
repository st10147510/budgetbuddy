package com.budgetbuddy.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("budget_buddy_session", Context.MODE_PRIVATE)

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

    val isLoggedIn: Boolean get() = userId != null

    fun clear() = prefs.edit().clear().apply()
}
