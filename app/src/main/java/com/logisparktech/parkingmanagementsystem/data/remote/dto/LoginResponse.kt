package com.logisparktech.parkingmanagementsystem.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success")
    val success: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: UserData?
)

data class UserData(
    @SerializedName("name")
    val name: String,
    @SerializedName("contact")
    val contact: String,
    @SerializedName("branch")
    val branch: String,
    @SerializedName("code")
    val code: String
)
