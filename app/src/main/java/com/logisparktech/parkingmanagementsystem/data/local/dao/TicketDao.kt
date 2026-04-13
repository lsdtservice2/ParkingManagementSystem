package com.logisparktech.parkingmanagementsystem.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.logisparktech.parkingmanagementsystem.data.local.entities.TicketEntity

@Dao
interface TicketDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketEntity)

    @Update
    suspend fun updateTicket(ticket: TicketEntity)

    @Query("SELECT * FROM tickets WHERE ticketId = :ticketId")
    suspend fun getTicketById(ticketId: String): TicketEntity?

    @Query("SELECT * FROM tickets WHERE vehicleNumber = :vehicleNumber AND isClosed = 0")
    suspend fun getActiveTicketByVehicleNumber(vehicleNumber: String): TicketEntity?

    @Query("UPDATE tickets SET exitTime = :exitTime, amount = :amount, isClosed = 1, isSynced = 0 WHERE ticketId = :ticketId")
    suspend fun closeTicket(ticketId: String, exitTime: Long, amount: Double)

    @Query("SELECT * FROM tickets WHERE isClosed = 1 AND isSynced = 0")
    suspend fun getUnsyncedClosedTickets(): List<TicketEntity>

    @Query("UPDATE tickets SET isSynced = 1 WHERE ticketId = :ticketId")
    suspend fun markTicketAsSynced(ticketId: String)

    @Query("SELECT * FROM tickets")
    suspend fun getAllTickets(): List<TicketEntity>

    @Query("DELETE FROM tickets WHERE ticketId = :ticketId")
    suspend fun deleteTicketById(ticketId: String)

    @Query("DELETE FROM tickets WHERE isClosed = 1 AND isSynced = 1")
    suspend fun deleteClosedAndSyncedTickets()
}
