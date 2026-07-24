package com.logisparktech.parkingmanagementsystem.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RateResponse(
    val success: Boolean,
    val message: String?,
    val data: List<RateDto>
)

data class RateDto(
    @SerializedName("_id")
    val id: String,
    @SerializedName("vehicleType")
    val name: String,
    @SerializedName("rate")
    val rate: Double,
    @SerializedName("exceedingMinutes")
    val exceedingMin: String?,
    @SerializedName("activateHalfHour")
    val active30Min: Boolean,
    @SerializedName("halfHourCost")
    val halfHourCost: Double?
)
