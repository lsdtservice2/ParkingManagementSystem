package com.logisparktech.parkingmanagementsystem.di

import com.logisparktech.parkingmanagementsystem.data.repository.AuthRepositoryImpl
import com.logisparktech.parkingmanagementsystem.data.repository.RateRepositoryImpl
import com.logisparktech.parkingmanagementsystem.data.repository.TicketRepositoryImpl
import com.logisparktech.parkingmanagementsystem.domain.repository.AuthRepository
import com.logisparktech.parkingmanagementsystem.domain.repository.RateRepository
import com.logisparktech.parkingmanagementsystem.domain.repository.TicketRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindRateRepository(
        rateRepositoryImpl: RateRepositoryImpl
    ): RateRepository

    @Binds
    @Singleton
    abstract fun bindTicketRepository(
        ticketRepositoryImpl: TicketRepositoryImpl
    ): TicketRepository
}
