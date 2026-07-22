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

    fun saveUserData(name: String, contact: String, branch: String, code: String) {
        sharedPreferences.edit {
            putString("user_name", name)
            putString("user_contact", contact)
            putString("user_branch", branch)
            putString("user_code", code)
            putLong("login_timestamp", System.currentTimeMillis())
        }
    }

//    fun isLoggedIn(): Boolean {
//        val loginTime = sharedPreferences.getLong("login_timestamp", 0L)
//        val currentTime = System.currentTimeMillis()
//        // Check if logged in and not expired (e.g., within last 30 days)
//        return loginTime > 0 && (currentTime - loginTime) < (30L * 24 * 60 * 60 * 1000)
//    }
    fun isLoggedIn(): Boolean {
        val loginTime = sharedPreferences.getLong("login_timestamp", 0L)
        if (loginTime == 0L) return false

        val currentTime = System.currentTimeMillis()

        // Create Calendar instances to compare the date
        val loginCal = java.util.Calendar.getInstance().apply { timeInMillis = loginTime }
        val currentCal = java.util.Calendar.getInstance().apply { timeInMillis = currentTime }

        // Check if Year and Day of Year are different
        val isSameDay = loginCal.get(java.util.Calendar.YEAR) == currentCal.get(java.util.Calendar.YEAR) &&
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

    fun generateTicketCode(): String {
        val nextToken = getTokenNumber() + 1
        saveTokenNumber(nextToken)
        val branchCode = getBranchCode().ifBlank { "BR" }
        val timeSuffix = System.currentTimeMillis().toString().takeLast(4)
        return "$branchCode-SR-$timeSuffix-$nextToken"
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
