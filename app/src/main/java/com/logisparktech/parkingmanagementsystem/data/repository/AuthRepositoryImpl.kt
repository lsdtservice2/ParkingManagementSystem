package com.logisparktech.parkingmanagementsystem.data.repository

import com.logisparktech.parkingmanagementsystem.data.remote.ApiService
import com.logisparktech.parkingmanagementsystem.data.remote.dto.LoginRequest
import com.logisparktech.parkingmanagementsystem.data.remote.dto.LoginResponse
import com.logisparktech.parkingmanagementsystem.data.remote.dto.UserData
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
            val response = apiService.login(LoginRequest(contact, password))
            val body = response.body()

            if (response.isSuccessful && body != null) {
                if (body.success == "true") {
                    Result.success(body)
                } else {
                    Result.failure(Exception(body.message))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception("Login failed: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
