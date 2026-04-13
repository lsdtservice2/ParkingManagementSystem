package com.logisparktech.parkingmanagementsystem.domain.use_case

import com.logisparktech.parkingmanagementsystem.data.local.entities.RateEntity
import com.logisparktech.parkingmanagementsystem.domain.repository.RateRepository
import javax.inject.Inject

class GetRatesUseCase @Inject constructor(
    private val repository: RateRepository
) {
    suspend operator fun invoke(): List<RateEntity> {
        return repository.getAllRates()
    }
}
