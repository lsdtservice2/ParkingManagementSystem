package com.logisparktech.parkingmanagementsystem.domain.use_case

import com.logisparktech.parkingmanagementsystem.domain.repository.TicketRepository
import javax.inject.Inject

class GetUnsyncedCountUseCase @Inject constructor(
    private val repository: TicketRepository
) {
    suspend operator fun invoke(): Int {
        return repository.getUnsyncedCount()
    }
}