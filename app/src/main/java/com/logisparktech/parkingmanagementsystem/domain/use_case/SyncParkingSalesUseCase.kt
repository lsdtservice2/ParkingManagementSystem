package com.logisparktech.parkingmanagementsystem.domain.use_case

import com.logisparktech.parkingmanagementsystem.domain.repository.TicketRepository
import javax.inject.Inject

class SyncParkingSalesUseCase @Inject constructor(
    private val repository: TicketRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.syncTicketsToServer()
    }
}
