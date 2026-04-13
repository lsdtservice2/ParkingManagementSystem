package com.logisparktech.parkingmanagementsystem.data.remote.dto

data class ParkingSaleRequest(
    val ticketCode: String,
    val vehicleNo: String,
    val tokenNo: String,
    val inTime: String,
    val outTime: String,
    val rate: Double,
    val amount: Double,
    val duration: String,
    val createdAt: String
)
