package com.logisparktech.parkingmanagementsystem.domain.use_case

import com.logisparktech.parkingmanagementsystem.data.local.entities.TicketEntity
import com.logisparktech.parkingmanagementsystem.domain.repository.TicketRepository
import javax.inject.Inject

class GetAllTicketsUseCase @Inject constructor(
    private val repository: TicketRepository
) {
    suspend operator fun invoke(): List<TicketEntity> {
        return repository.getAllTickets()
    }
}
