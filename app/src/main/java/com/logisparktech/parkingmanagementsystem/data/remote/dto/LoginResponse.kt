package com.logisparktech.parkingmanagementsystem.data.remote.dto

data class LoginResponse(
    val success: String,
    val message: String,
    val data: UserData?
)

data class UserData(
    val name: String,
    val contact: String,
    val branch: String,
    val code: String
)
