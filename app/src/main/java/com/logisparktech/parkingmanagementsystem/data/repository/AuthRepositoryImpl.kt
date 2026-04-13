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
    override suspend fun login(username: String, password: String): Result<LoginResponse> {
//        if (username == "admin" && password == "admin") {
//            return Result.success(
//                LoginResponse(
//                    success = "true",
//                    message = "Login successful",
//                    data = UserData(
//                        name = "Admin User",
//                        contact = "1234567890",
//                        branch = "Main Branch",
//                        code = "ADM001"
//                    )
//                )
//            )
//        }
        return try {
            val response = apiService.login(LoginRequest(username, password))
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
