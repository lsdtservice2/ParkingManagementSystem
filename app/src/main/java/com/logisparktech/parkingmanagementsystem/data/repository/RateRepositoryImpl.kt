package com.logisparktech.parkingmanagementsystem.data.repository

import com.logisparktech.parkingmanagementsystem.data.local.dao.RateDao
import com.logisparktech.parkingmanagementsystem.data.local.entities.RateEntity
import com.logisparktech.parkingmanagementsystem.data.remote.ApiService
import com.logisparktech.parkingmanagementsystem.domain.repository.RateRepository
import javax.inject.Inject

class RateRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val rateDao: RateDao
) : RateRepository {

    override suspend fun syncRates(branch: String): Result<Unit> {
        return try {
            val response = apiService.getVehicleRates(branch)
            if (response.isSuccessful && response.body() != null) {
                val rateResponse = response.body()!!
                if (rateResponse.success == "true") {
                    val remoteRates = rateResponse.data.map { dto ->
                        RateEntity(
                            rateId = dto.id,
                            vehicleType = dto.name,
                            pricePerHour = dto.rate.toDoubleOrNull() ?: 0.0,
                            exceedingMin = dto.exceedingMin.toIntOrNull() ?: 0,
                            active30Min = dto.active30Min == "1",
                            halfHourCost = dto.halfHourCost?.toDoubleOrNull() ?: 0.0
                        )
                    }
                    rateDao.clearAllRates()
                    rateDao.insertAllRates(remoteRates)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(rateResponse.message))
                }
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllRates(): List<RateEntity> {
        return rateDao.getAllRates()
    }

    override suspend fun getRateById(rateId: String): RateEntity? {
        return rateDao.getRateById(rateId)
    }

    override suspend fun clearAllRates() {
        rateDao.clearAllRates()
    }
}
