package com.logisparktech.parkingmanagementsystem.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("contact")
    val contact: String,
    @SerializedName("password")
    val password: String
)
