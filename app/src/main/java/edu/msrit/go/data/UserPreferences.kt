package edu.msrit.go.data

import android.content.Context
import android.content.SharedPreferences
import android.webkit.CookieManager

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("msrit_go_prefs", Context.MODE_PRIVATE)

    var isLoggedIn: Boolean
        get() = prefs.getBoolean("is_logged_in", false)
        set(value) = prefs.edit().putBoolean("is_logged_in", value).apply()

    var savedUsn: String
        get() = prefs.getString("saved_usn", "") ?: ""
        set(value) = prefs.edit().putString("saved_usn", value).apply()

    var savedDobDay: String
        get() = prefs.getString("saved_dob_day", "") ?: ""
        set(value) = prefs.edit().putString("saved_dob_day", value).apply()

    var savedDobMonth: String
        get() = prefs.getString("saved_dob_month", "") ?: ""
        set(value) = prefs.edit().putString("saved_dob_month", value).apply()

    var savedDobYear: String
        get() = prefs.getString("saved_dob_year", "") ?: ""
        set(value) = prefs.edit().putString("saved_dob_year", value).apply()

    var rememberMe: Boolean
        get() = prefs.getBoolean("remember_me", true)
        set(value) = prefs.edit().putBoolean("remember_me", value).apply()

    var targetAttendance: Double
        get() = prefs.getFloat("target_attendance", 85.0f).toDouble()
        set(value) = prefs.edit().putFloat("target_attendance", value.toFloat()).apply()

    var isDemoMode: Boolean
        get() = prefs.getBoolean("is_demo_mode", false)
        set(value) = prefs.edit().putBoolean("is_demo_mode", value).apply()

    var lastSyncTime: Long
        get() = prefs.getLong("last_sync_time", 0L)
        set(value) = prefs.edit().putLong("last_sync_time", value).apply()

    var cachedProfileJson: String?
        get() = prefs.getString("cached_profile_json", null)
        set(value) = prefs.edit().putString("cached_profile_json", value).apply()

    var cachedAttendanceJson: String?
        get() = prefs.getString("cached_attendance_json", null)
        set(value) = prefs.edit().putString("cached_attendance_json", value).apply()

    var cachedMarksJson: String?
        get() = prefs.getString("cached_marks_json", null)
        set(value) = prefs.edit().putString("cached_marks_json", value).apply()

    var cachedCircularsJson: String?
        get() = prefs.getString("cached_circulars_json", null)
        set(value) = prefs.edit().putString("cached_circulars_json", value).apply()

    val formattedDobPassword: String
        get() = if (savedDobYear.isNotEmpty()) {
            "${savedDobYear.trim()}-${savedDobMonth.trim().padStart(2, '0')}-${savedDobDay.trim().padStart(2, '0')}"
        } else {
            ""
        }

    fun clearCredentials() {
        prefs.edit()
            .remove("saved_usn")
            .remove("saved_dob_day")
            .remove("saved_dob_month")
            .remove("saved_dob_year")
            .apply()
    }

    fun logout() {
        isLoggedIn = false
        isDemoMode = false
        if (!rememberMe) {
            clearCredentials()
        }
        try {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
