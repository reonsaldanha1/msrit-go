package edu.msrit.go.data

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("msrit_go_prefs", Context.MODE_PRIVATE)

    var savedUsn: String
        get() = prefs.getString("saved_usn", "1MS22CS042") ?: "1MS22CS042"
        set(value) = prefs.edit().putString("saved_usn", value).apply()

    var savedDobDay: String
        get() = prefs.getString("saved_dob_day", "15") ?: "15"
        set(value) = prefs.edit().putString("saved_dob_day", value).apply()

    var savedDobMonth: String
        get() = prefs.getString("saved_dob_month", "08") ?: "08"
        set(value) = prefs.edit().putString("saved_dob_month", value).apply()

    var savedDobYear: String
        get() = prefs.getString("saved_dob_year", "2004") ?: "2004"
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

    val formattedDobPassword: String
        get() = "${savedDobYear.trim()}-${savedDobMonth.trim()}-${savedDobDay.trim()}"

    fun clearCredentials() {
        prefs.edit()
            .remove("saved_usn")
            .remove("saved_dob_day")
            .remove("saved_dob_month")
            .remove("saved_dob_year")
            .apply()
    }
}
