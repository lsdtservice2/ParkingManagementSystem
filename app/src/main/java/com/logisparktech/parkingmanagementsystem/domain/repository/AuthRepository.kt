package com.logisparktech.parkingmanagementsystem.domain.repository

import com.logisparktech.parkingmanagementsystem.data.remote.dto.LoginResponse

interface AuthRepository {
    suspend fun login(contact: String, password: String): Result<LoginResponse>
}
