package com.logisparktech.parkingmanagementsystem.data.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.logisparktech.parkingmanagementsystem.data.remote.ApiService
import com.logisparktech.parkingmanagementsystem.data.remote.dto.LoginRequest
import com.logisparktech.parkingmanagementsystem.data.remote.dto.LoginResponse
import com.logisparktech.parkingmanagementsystem.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AuthRepository {
    override suspend fun login(contact: String, password: String): Result<LoginResponse> =
        withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(LoginRequest(phoneNumber = contact, password = password))
            val body = response.body()

            if (response.isSuccessful && body != null) {
                if (body.success) {
                    Result.success(body)
                } else {
                    Result.failure(Exception(body.message ?: "Login failed"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val parsedMsg = parseErrorMessage(errorBody) ?: errorBody ?: response.message()
                Result.failure(Exception(parsedMsg.ifBlank { "Login failed" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val jsonObject = Gson().fromJson(errorBody, JsonObject::class.java)
            val messageElement = jsonObject.get("message")
            if (messageElement != null) {
                if (messageElement.isJsonArray) {
                    val messages = mutableListOf<String>()
                    messageElement.asJsonArray.forEach { element ->
                        if (element.isJsonPrimitive) {
                            messages.add(element.asString)
                        }
                    }
                    messages.joinToString("\n")
                } else if (messageElement.isJsonPrimitive) {
                    messageElement.asString
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
