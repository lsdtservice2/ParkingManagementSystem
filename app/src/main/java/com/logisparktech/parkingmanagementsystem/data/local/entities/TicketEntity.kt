package com.logisparktech.parkingmanagementsystem.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey val ticketId: String,
    val uuid: String,
    val vehicleNumber: String,
    val entryTime: Long,
    val exitTime: Long?,
    val rateId: String,
    val isClosed: Boolean,
    val amount: Double,
    val isSynced: Boolean
)
