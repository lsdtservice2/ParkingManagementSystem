package com.logisparktech.parkingmanagementsystem.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: LoginData?
)

data class LoginData(
    @SerializedName("accessToken")
    val accessToken: String?,
    @SerializedName("user")
    val user: UserDto?
)

data class UserDto(
    @SerializedName("id")
    val id: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("phoneNumber")
    val phoneNumber: String?,
    @SerializedName("role")
    val role: String?
)
