package com.logisparktech.parkingmanagementsystem.domain.use_case

import com.logisparktech.parkingmanagementsystem.data.local.entities.TicketEntity
import com.logisparktech.parkingmanagementsystem.domain.repository.TicketRepository
import javax.inject.Inject

class GetActiveTicketUseCase @Inject constructor(
    private val repository: TicketRepository
) {
    suspend operator fun invoke(vehicleNumber: String): TicketEntity? {
        return repository.getActiveTicketByVehicleNumber(vehicleNumber)
    }
}
