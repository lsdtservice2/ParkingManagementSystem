package com.logisparktech.parkingmanagementsystem.data.remote

import com.logisparktech.parkingmanagementsystem.data.remote.dto.LoginRequest
import com.logisparktech.parkingmanagementsystem.data.remote.dto.LoginResponse
import com.logisparktech.parkingmanagementsystem.data.remote.dto.ParkingTicketRequest
import com.logisparktech.parkingmanagementsystem.data.remote.dto.ParkingTicketResponse
import com.logisparktech.parkingmanagementsystem.data.remote.dto.RateResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("vehicle-rates")
    suspend fun getVehicleRates(
        @Query("isActive") isActive: Boolean = true
    ): Response<RateResponse>

    @POST("parking-tickets")
    suspend fun syncParkingTicket(
        @Body ticket: ParkingTicketRequest
    ): Response<ParkingTicketResponse>
}
