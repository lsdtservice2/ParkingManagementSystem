package com.logisparktech.parkingmanagementsystem.data.repository

import com.logisparktech.parkingmanagementsystem.data.local.dao.RateDao
import com.logisparktech.parkingmanagementsystem.data.local.dao.TicketDao
import com.logisparktech.parkingmanagementsystem.data.local.entities.TicketEntity
import com.logisparktech.parkingmanagementsystem.data.local.preferences.PreferenceManager
import com.logisparktech.parkingmanagementsystem.data.remote.ApiService
import com.logisparktech.parkingmanagementsystem.data.remote.dto.ParkingSaleRequest
import com.logisparktech.parkingmanagementsystem.domain.repository.TicketRepository
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

    override suspend fun getActiveTicketByVehicleNumber(vehicleNumber: String): TicketEntity? {
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

    override suspend fun syncTicketsToServer(): Result<Unit> {
        return try {
       //     // Now fetches Closed unsynced tickets
//            val unsyncedTickets = ticketDao.getUnsyncedClosedTickets()
//            if (unsyncedTickets.isEmpty()) return Result.success(Unit)
//
            // Now fetches both Active and Closed unsynced tickets
            val unsyncedTickets = ticketDao.getUnsyncedTickets()
            if (unsyncedTickets.isEmpty()) return Result.success(Unit)

            val branch = preferenceManager.getBranch()
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val shortDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val requests = unsyncedTickets.map { entity ->
                val rateEntity = rateDao.getRateById(entity.rateId)

                val entryDate = Date(entity.entryTime)
//                val exitDate = entity.exitTime?.let { Date(it) } ?: Date()

                val durationMillis = (entity.exitTime ?: System.currentTimeMillis()) - entity.entryTime
                val hours = durationMillis / (1000 * 60 * 60)
                val minutes = (durationMillis / (1000 * 60)) % 60
                val durationStr = String.format("%d.%02d", hours, minutes)

                ParkingSaleRequest(
                    ticketCode = entity.ticketId,
                    vehicleNo = entity.vehicleNumber,
                    tokenNo = entity.ticketId.substringAfterLast("-"),
                    inTime = isoFormat.format(entryDate),
                    outTime = entity.exitTime?.let { isoFormat.format(Date(it)) } ?: "",
                    rate = rateEntity?.pricePerHour ?: 0.0,
                    amount = entity.amount,
                    duration = durationStr,
                    createdAt = shortDateFormat.format(entryDate)
                )
            }

            val response = apiService.syncParkingSales(branch, requests)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == "true") {
                    unsyncedTickets.forEach {
                        ticketDao.markTicketAsSynced(it.ticketId)
                    }
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(body?.message ?: "Sync failed: server returned success=false"))
                }
            } else {
                Result.failure(Exception(response.message()))
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
