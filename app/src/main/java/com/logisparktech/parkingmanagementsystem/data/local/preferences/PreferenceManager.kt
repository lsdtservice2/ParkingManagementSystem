package com.logisparktech.parkingmanagementsystem.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import java.util.TimeZone
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
            putLong("login_timestamp", System.currentTimeMillis())
        }
    }

    fun getAccessToken(): String {
        return sharedPreferences.getString("access_token", "") ?: ""
    }

    fun isLoggedIn(): Boolean {
        val token = getAccessToken()
        if (token.isEmpty()) return false

        // Check if JWT is expired
        if (isJwtExpired(token)) {
            clear() // Logout: token is no longer valid
            return false
        }

        return true
    }

    private fun isJwtExpired(token: String): Boolean {
        val payload = decodeJwt(token) ?: return true

        val exp = payload.optLong("exp", 0)
        val currentTime = System.currentTimeMillis() / 1000 // seconds
//        Log.d("PreferenceManager", "Expired : ${currentTime >= exp}")

        return currentTime >= exp
    }

    private fun decodeJwt(token: String): JSONObject? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val payload = String(
                Base64.decode(
                    parts[1],
                    Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
                )
            )
//            Log.d("PreferenceManager", "jwt Decode json $payload")

            JSONObject(payload)
        } catch (e: Exception) {
//            Log.e("PreferenceManager", "Failed to decode JWT", e)

            null
        }
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
        // Daily Reset Logic
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kathmandu")
        }
        val today = sdf.format(Date())
        val lastResetDay = sharedPreferences.getString("last_reset_day", "")

        if (today != lastResetDay) {
            // New day detected: Reset token number to 0
            sharedPreferences.edit {
                putInt("token_number", 0)
                putString("last_reset_day", today)
            }
        }

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

        // Get and increment token number
        val currentToken = getTokenNumber()
        val nextToken = currentToken + 1
        saveTokenNumber(nextToken)

        // Format: TNX-TYPE-0001
        val paddedToken = String.format(Locale.US, "%04d", nextToken)
//        return "TNX-$vehicleCode-$paddedToken"
        return nextToken.toString()
    }

//    fun saveLastSync(timestamp: String) {
//        sharedPreferences.edit { putString("last_sync", timestamp) }
//    }
//
//    fun getLastSync(): String {
//        return sharedPreferences.getString("last_sync", "") ?: ""
//    }
//
//    fun setSyncStatus(status: Boolean) {
//        sharedPreferences.edit { putBoolean("sync_status", status) }
//    }
//
//    fun getSyncStatus(): Boolean {
//        return sharedPreferences.getBoolean("sync_status", false)
//    }

    fun clear() {
        sharedPreferences.edit {
            remove("access_token")
            remove("user_name")
            remove("user_contact")
            remove("login_timestamp")
            // "token_number" and "last_reset_day" are NOT removed to maintain sequence on same day
        }
    }
}
