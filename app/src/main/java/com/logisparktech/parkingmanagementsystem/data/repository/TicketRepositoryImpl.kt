package com.logisparktech.parkingmanagementsystem.data.repository

import com.logisparktech.parkingmanagementsystem.data.local.dao.RateDao
import com.logisparktech.parkingmanagementsystem.data.local.dao.TicketDao
import com.logisparktech.parkingmanagementsystem.data.local.entities.TicketEntity
import com.logisparktech.parkingmanagementsystem.data.local.preferences.PreferenceManager
import com.logisparktech.parkingmanagementsystem.data.remote.ApiService
import com.logisparktech.parkingmanagementsystem.data.remote.dto.ParkingTicketRequest
import com.logisparktech.parkingmanagementsystem.domain.repository.TicketRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class TicketRepositoryImpl @Inject constructor(
    private val ticketDao: TicketDao,
    private val apiService: ApiService,
    private val preferenceManager: PreferenceManager,
    private val rateDao: RateDao
) : TicketRepository {

    override suspend fun insertTicket(ticket: TicketEntity) {
        ticketDao.insertTicket(ticket)
    }

    override suspend fun getTicketById(ticketId: String): TicketEntity? {
        return ticketDao.getTicketById(ticketId)
    }

    override suspend fun getActiveTicketByVehicleNumber(vehicleNumber: String): List<TicketEntity> {
        return ticketDao.getActiveTicketByVehicleNumber(vehicleNumber)
    }

    override suspend fun closeTicket(ticketId: String, exitTime: Long, amount: Double) {
        ticketDao.closeTicket(ticketId, exitTime, amount)
    }

    override suspend fun getUnsyncedClosedTickets(): List<TicketEntity> {
        return ticketDao.getUnsyncedClosedTickets()
    }

    override suspend fun getUnsyncedTickets(): List<TicketEntity> {
        return ticketDao.getUnsyncedTickets()
    }

    override suspend fun markTicketAsSynced(ticketId: String) {
        ticketDao.markTicketAsSynced(ticketId)
    }

    override suspend fun syncTicketsToServer(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val unsyncedTickets = ticketDao.getUnsyncedTickets()
//            val unsyncedTickets = ticketDao.getUnsyncedClosedTickets()
            if (unsyncedTickets.isEmpty()) return@withContext Result.success(Unit)

            // OPTIMIZATION: Fetch all rates once into a map
            val allRates = rateDao.getAllRates().associateBy { it.rateId }

            // Use consistent TimeZone (Kathmandu) and ISO format
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("Asia/Kathmandu")
            }

            var lastError: String? = null

            for (entity in unsyncedTickets) {
                val rateEntity = allRates[entity.rateId]
                val entryDate = Date(entity.entryTime)
                val exitDate = entity.exitTime?.let { Date(it) }

                val durationMillis =
                    (entity.exitTime ?: System.currentTimeMillis()) - entity.entryTime
                val totalHours = durationMillis.toDouble() / (1000.0 * 60.0 * 60.0)

                val request = ParkingTicketRequest(
                    ticketNumber = entity.ticketId,
                    vehicleType = rateEntity?.vehicleType ?: "",
                    vehicleNumber = entity.vehicleNumber,
                    tokenNumber = entity.ticketId.substringAfterLast("-"),
                    checkInTime = isoFormat.format(entryDate),
                    checkOutTime = exitDate?.let { isoFormat.format(it) } ?: "",
                    hourlyRate = rateEntity?.pricePerHour ?: 0.0,
                    totalHours = String.format(Locale.US, "%.2f", totalHours).toDouble(),
                    totalAmountPaid = entity.amount
                )

                val response = apiService.syncParkingTicket(request)
                val body = response.body()
                if (response.isSuccessful && body != null && body.success) {
                    ticketDao.markTicketAsSynced(entity.ticketId)
                } else {
                    lastError = body?.message ?: "Sync failed for ticket ${entity.ticketId}: ${response.code()}"
                }
            }

            if (lastError != null) {
                Result.failure(Exception(lastError))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllTickets(): List<TicketEntity> {
        return ticketDao.getAllTickets()
    }

    override suspend fun deleteClosedAndSyncedTickets() {
        ticketDao.deleteClosedAndSyncedTickets()
    }

    override suspend fun getUnsyncedCount(): Int {
        return ticketDao.getUnsyncedCount()
    }

}
