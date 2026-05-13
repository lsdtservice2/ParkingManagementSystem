package com.logisparktech.parkingmanagementsystem.data.repository

import com.logisparktech.parkingmanagementsystem.data.remote.ApiService
import com.logisparktech.parkingmanagementsystem.data.remote.dto.LoginRequest
import com.logisparktech.parkingmanagementsystem.data.remote.dto.LoginResponse
import com.logisparktech.parkingmanagementsystem.data.remote.dto.UserData
import com.logisparktech.parkingmanagementsystem.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AuthRepository {
    override suspend fun login(contact: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(contact, password))
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                if (loginResponse.success == "true") {
                    Result.success(loginResponse)
                } else {
                    Result.failure(Exception(loginResponse.message))
                }
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
