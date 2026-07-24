package com.logisparktech.parkingmanagementsystem.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ParkingTicketResponse(
    val success: Boolean,
    val message: String?,
    val data: ParkingTicketData?
)

data class ParkingTicketData(
    val ticketNumber: String?,
    val vehicleType: String?,
    val vehicleNumber: String?,
    val tokenNumber: String?,
    val checkInTime: String?,
    val checkOutTime: String?,
    val hourlyRate: Double?,
    val totalHours: Double?,
    val totalAmountPaid: Double?,
    val status: String?,
    @SerializedName("_id")
    val id: String?
)
