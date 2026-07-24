package com.logisparktech.parkingmanagementsystem.data.repository

import com.logisparktech.parkingmanagementsystem.data.local.dao.RateDao
import com.logisparktech.parkingmanagementsystem.data.local.entities.RateEntity
import com.logisparktech.parkingmanagementsystem.data.remote.ApiService
import com.logisparktech.parkingmanagementsystem.domain.repository.RateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RateRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val rateDao: RateDao
) : RateRepository {

    override suspend fun syncRates(branch: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getVehicleRates()
            val body = response.body()

            if (!response.isSuccessful || body == null) {
                return@withContext Result.failure(Exception("Network error: ${response.code()}"))
            }

            if (body.success) {
                val remoteRates = body.data.map { dto ->
                    val price = dto.rate

                    RateEntity(
                        rateId = dto.id,
                        vehicleType = dto.name,
                        pricePerHour = price,
                        exceedingMin = dto.exceedingMin?.toDoubleOrNull()?.toInt() 
                            ?: dto.exceedingMin?.toIntOrNull() 
                            ?: 0,
                        active30Min = dto.active30Min,
                        halfHourCost = dto.halfHourCost ?: 0.0
                    )
                }

                rateDao.refreshRates(remoteRates)
                Result.success(Unit)
            } else {
                Result.failure(Exception(body.message))
            }
        } catch (e: Exception) {
            // Recommendation: Add a logging tool here (like Timber or Firebase Crashlytics)
            Result.failure(e)
        }
    }

    override suspend fun getAllRates(): List<RateEntity> = withContext(Dispatchers.IO) {
        rateDao.getAllRates()
    }

    override suspend fun getRateById(rateId: String): RateEntity? = withContext(Dispatchers.IO) {
        rateDao.getRateById(rateId)
    }

    override suspend fun clearAllRates() = withContext(Dispatchers.IO) {
        rateDao.clearAllRates()
    }
}