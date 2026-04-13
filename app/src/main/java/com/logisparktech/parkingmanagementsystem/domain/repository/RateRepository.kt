package com.logisparktech.parkingmanagementsystem.domain.repository

import com.logisparktech.parkingmanagementsystem.data.local.entities.RateEntity

interface RateRepository {
    suspend fun syncRates(branch: String): Result<Unit>
    suspend fun getAllRates(): List<RateEntity>
    suspend fun getRateById(rateId: String): RateEntity?
    suspend fun clearAllRates()
}
