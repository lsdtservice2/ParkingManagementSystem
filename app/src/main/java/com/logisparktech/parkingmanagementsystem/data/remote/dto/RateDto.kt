package com.logisparktech.parkingmanagementsystem.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RateResponse(
    val success: String,
    val message: String,
    val data: List<RateDto>
)

data class RateDto(
    val id: String,
    val name: String,
    val rate: String,
    @SerializedName("exceedingLimit")
    val exceedingMin: String,
    val halfHourCost: String?,
    @SerializedName("is30MinActivation")
    val active30Min: String
)
