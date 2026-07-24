package com.logisparktech.parkingmanagementsystem.data.remote.dto

data class ParkingTicketRequest(
    val ticketNumber: String,
    val vehicleType: String,
    val vehicleNumber: String,
    val tokenNumber: String,
    val checkInTime: String,
    val checkOutTime: String,
    val hourlyRate: Double,
    val totalHours: Double,
    val totalAmountPaid: Double
)
