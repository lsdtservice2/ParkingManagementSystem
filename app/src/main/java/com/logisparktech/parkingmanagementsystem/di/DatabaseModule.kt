package com.logisparktech.parkingmanagementsystem.di

import android.content.Context
import androidx.room.Room
import com.logisparktech.parkingmanagementsystem.data.local.AppDatabase
import com.logisparktech.parkingmanagementsystem.data.local.dao.RateDao
import com.logisparktech.parkingmanagementsystem.data.local.dao.TicketDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "parking_db"
        ).build()
    }

    @Provides
    fun provideTicketDao(database: AppDatabase): TicketDao {
        return database.ticketDao()
    }

    @Provides
    fun provideRateDao(database: AppDatabase): RateDao {
        return database.rateDao()
    }
}
