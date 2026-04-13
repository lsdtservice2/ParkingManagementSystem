package com.logisparktech.parkingmanagementsystem.domain.use_case

import com.logisparktech.parkingmanagementsystem.domain.repository.RateRepository
import javax.inject.Inject

class SyncRatesUseCase @Inject constructor(
    private val repository: RateRepository
) {
    suspend operator fun invoke(branch: String): Result<Unit> {
        return repository.syncRates(branch)
    }
}
