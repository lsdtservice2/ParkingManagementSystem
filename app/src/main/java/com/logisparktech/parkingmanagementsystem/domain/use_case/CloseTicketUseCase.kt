package com.logisparktech.parkingmanagementsystem.domain.use_case

import com.logisparktech.parkingmanagementsystem.domain.repository.RateRepository
import com.logisparktech.parkingmanagementsystem.domain.repository.TicketRepository
import javax.inject.Inject

import com.logisparktech.parkingmanagementsystem.data.local.entities.TicketEntity

class CloseTicketUseCase @Inject constructor(
    private val ticketRepository: TicketRepository,
    private val rateRepository: RateRepository,
    private val calculateAmountUseCase: CalculateAmountUseCase
) {
    suspend operator fun invoke(ticketId: String, exitTime: Long): Result<TicketEntity> {
        val ticket = ticketRepository.getTicketById(ticketId)
            ?: return Result.failure(Exception("Ticket not found"))
        
        val rate = rateRepository.getRateById(ticket.rateId)
            ?: return Result.failure(Exception("Rate not found"))
            
        val amount = calculateAmountUseCase(
            entryTime = ticket.entryTime,
            exitTime = exitTime,
            rate = rate.pricePerHour,
            halfHourCost = rate.halfHourCost,
            exceedingLimitMin = rate.exceedingMin,
            is30MinActivation = rate.active30Min
        )
        
        val updatedTicket = ticket.copy(
            exitTime = exitTime,
            amount = amount,
            isClosed = true
        )
        
        ticketRepository.closeTicket(ticketId, exitTime, amount)
        return Result.success(updatedTicket)
    }
}
