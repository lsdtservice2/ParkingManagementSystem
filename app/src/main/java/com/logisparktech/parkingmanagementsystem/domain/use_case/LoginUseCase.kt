package com.logisparktech.parkingmanagementsystem.domain.use_case

import com.logisparktech.parkingmanagementsystem.data.remote.dto.LoginResponse
import com.logisparktech.parkingmanagementsystem.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(contact: String, password: String): Result<LoginResponse> {
        return repository.login(contact, password)
    }
}
