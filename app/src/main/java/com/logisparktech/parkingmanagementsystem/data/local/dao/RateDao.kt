package com.logisparktech.parkingmanagementsystem.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.logisparktech.parkingmanagementsystem.data.local.entities.RateEntity

@Dao
interface RateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRate(rate: RateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRates(rates: List<RateEntity>)

    @Update
    suspend fun updateRate(rate: RateEntity)

    @Query("SELECT * FROM rates WHERE rateId = :rateId")
    suspend fun getRateById(rateId: String): RateEntity?

    @Query("SELECT * FROM rates")
    suspend fun getAllRates(): List<RateEntity>

    @Query("DELETE FROM rates")
    suspend fun clearAllRates()

    @Query("DELETE FROM rates WHERE rateId = :rateId")
    suspend fun deleteRateById(rateId: String)

    @androidx.room.Transaction
    suspend fun refreshRates(rates: List<RateEntity>) {
        clearAllRates()
        insertAllRates(rates)
    }

}
