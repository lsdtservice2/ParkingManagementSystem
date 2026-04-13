package com.logisparktech.parkingmanagementsystem.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rates")
data class RateEntity(
    @PrimaryKey val rateId: String,
    val vehicleType: String,
    val pricePerHour: Double,
    val exceedingMin: Int,
    val active30Min: Boolean,
    val halfHourCost: Double
)
