package com.logisparktech.parkingmanagementsystem.domain.use_case

import com.logisparktech.parkingmanagementsystem.data.local.entities.TicketEntity
import com.logisparktech.parkingmanagementsystem.domain.repository.TicketRepository
import javax.inject.Inject

class CreateTicketUseCase @Inject constructor(
    private val repository: TicketRepository
) {
    suspend operator fun invoke(ticket: TicketEntity) {
        repository.insertTicket(ticket)
    }
}
