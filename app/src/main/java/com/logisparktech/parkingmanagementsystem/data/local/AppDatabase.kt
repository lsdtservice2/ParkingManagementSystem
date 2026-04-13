package com.logisparktech.parkingmanagementsystem.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.logisparktech.parkingmanagementsystem.data.local.dao.RateDao
import com.logisparktech.parkingmanagementsystem.data.local.dao.TicketDao
import com.logisparktech.parkingmanagementsystem.data.local.entities.RateEntity
import com.logisparktech.parkingmanagementsystem.data.local.entities.TicketEntity

@Database(entities = [TicketEntity::class, RateEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ticketDao(): TicketDao
    abstract fun rateDao(): RateDao
}
