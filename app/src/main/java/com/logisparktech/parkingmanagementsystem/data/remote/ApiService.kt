package com.logisparktech.parkingmanagementsystem.data.remote

import com.logisparktech.parkingmanagementsystem.data.remote.dto.LoginRequest
import com.logisparktech.parkingmanagementsystem.data.remote.dto.LoginResponse
import com.logisparktech.parkingmanagementsystem.data.remote.dto.ParkingSaleRequest
import com.logisparktech.parkingmanagementsystem.data.remote.dto.RateResponse
import com.logisparktech.parkingmanagementsystem.data.remote.dto.SyncResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/vehicle-rate/{branch}")
    suspend fun getVehicleRates(@Path("branch") branch: String): Response<RateResponse>

    @POST("api/parking-sales/{branch}")
    suspend fun syncParkingSales(
        @Path("branch") branch: String,
        @Body sales: List<ParkingSaleRequest>
    ): Response<SyncResponse>
}
