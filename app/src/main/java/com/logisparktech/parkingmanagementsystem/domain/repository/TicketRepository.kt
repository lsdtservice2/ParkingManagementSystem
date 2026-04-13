package com.logisparktech.parkingmanagementsystem.domain.repository

import com.logisparktech.parkingmanagementsystem.data.local.entities.TicketEntity

interface TicketRepository {
    suspend fun insertTicket(ticket: TicketEntity)
    suspend fun getTicketById(ticketId: String): TicketEntity?
    suspend fun getActiveTicketByVehicleNumber(vehicleNumber: String): TicketEntity?
    suspend fun closeTicket(ticketId: String, exitTime: Long, amount: Double)
    suspend fun getUnsyncedClosedTickets(): List<TicketEntity>
    suspend fun markTicketAsSynced(ticketId: String)
    suspend fun syncTicketsToServer(): Result<Unit>
    suspend fun getAllTickets(): List<TicketEntity>
    suspend fun deleteClosedAndSyncedTickets()
}
