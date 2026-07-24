package com.logisparktech.parkingmanagementsystem.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("parking_prefs", Context.MODE_PRIVATE)

    fun saveUserData(token: String, name: String, contact: String) {
        sharedPreferences.edit {
            putString("access_token", token)
            putString("user_name", name)
            putString("user_contact", contact)
//            putLong("login_timestamp", System.currentTimeMillis())
        }
    }

    fun getAccessToken(): String {
        return sharedPreferences.getString("access_token", "") ?: ""
    }

    fun isLoggedIn(): Boolean {
        val loginTime = sharedPreferences.getLong("login_timestamp", 0L)
        if (loginTime == 0L) return false

        val currentTime = System.currentTimeMillis()

        // Create Calendar instances to compare the date
        val loginCal = java.util.Calendar.getInstance().apply { timeInMillis = loginTime }
        val currentCal = java.util.Calendar.getInstance().apply { timeInMillis = currentTime }

        // Check if Year and Day of Year are different
        val isSameDay =
            loginCal.get(java.util.Calendar.YEAR) == currentCal.get(java.util.Calendar.YEAR) &&
                    loginCal.get(java.util.Calendar.DAY_OF_YEAR) == currentCal.get(java.util.Calendar.DAY_OF_YEAR)

        if (!isSameDay) {
            clear() // Auto logout: wipe session data because the date has changed
            return false
        }

        return true
    }


    fun getBranch(): String {
        return sharedPreferences.getString("user_branch", "") ?: ""
    }

    fun getBranchCode(): String {
        return sharedPreferences.getString("user_code", "") ?: ""
    }

    fun getName(): String {
        return sharedPreferences.getString("user_name", "") ?: ""
    }

    fun saveTokenNumber(token: Int) {
        sharedPreferences.edit { putInt("token_number", token) }
    }

    fun getTokenNumber(): Int {
        return sharedPreferences.getInt("token_number", 0)
    }

    fun getContact(): String {
        return sharedPreferences.getString("user_contact", "") ?: ""
    }

    fun generateTicketCode(vehicleType: String): String {
        // Derive vehicle classification: 4W / 2W / HV
        val vehicleCode = when {
            vehicleType.contains("car", ignoreCase = true) ||
            vehicleType.contains("4", ignoreCase = true) ||
            vehicleType.contains("four", ignoreCase = true) -> "4W"

            vehicleType.contains("bike", ignoreCase = true) ||
            vehicleType.contains("2", ignoreCase = true) ||
            vehicleType.contains("two", ignoreCase = true) ||
            vehicleType.contains("cycle", ignoreCase = true) -> "2W"

            vehicleType.contains("bus", ignoreCase = true) ||
            vehicleType.contains("truck", ignoreCase = true) ||
            vehicleType.contains("heavy", ignoreCase = true) -> "HV"

            else -> "4W"
        }

        // MMSS: zero-padded minute (2 digits) + second (2 digits)
        val cal = java.util.Calendar.getInstance()
        val minute = String.format("%02d", cal.get(java.util.Calendar.MINUTE))
        val second = String.format("%02d", cal.get(java.util.Calendar.SECOND))

        return "TNX-$vehicleCode-$minute$second"
    }

    fun saveLastSync(timestamp: String) {
        sharedPreferences.edit { putString("last_sync", timestamp) }
    }

    fun getLastSync(): String {
        return sharedPreferences.getString("last_sync", "") ?: ""
    }

    fun setSyncStatus(status: Boolean) {
        sharedPreferences.edit { putBoolean("sync_status", status) }
    }

    fun getSyncStatus(): Boolean {
        return sharedPreferences.getBoolean("sync_status", false)
    }

    fun clear() {
        sharedPreferences.edit { clear() }
    }
}
