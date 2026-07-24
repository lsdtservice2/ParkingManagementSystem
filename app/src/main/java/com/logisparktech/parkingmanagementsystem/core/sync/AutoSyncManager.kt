package com.logisparktech.parkingmanagementsystem.core.sync

import com.logisparktech.parkingmanagementsystem.domain.use_case.SyncParkingSalesUseCase
import com.logisparktech.parkingmanagementsystem.domain.use_case.SyncRatesUseCase
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class AutoSyncManager @Inject constructor(
    private val syncParkingSalesUseCase: SyncParkingSalesUseCase,
    private val syncRatesUseCase: SyncRatesUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var syncJob: Job? = null

    fun startAutoSync() {
        if (syncJob?.isActive == true) return

        syncJob = scope.launch {
            while (isActive) {
                try {
                    // Sync tickets/sales
                    syncParkingSalesUseCase()
                    
                    // Sync rates periodically as well
                    syncRatesUseCase("MAIN")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                // Wait for 15 minutes before next sync
                delay((15 * 60 * 1000L).milliseconds)
            }
        }
    }

    fun stopAutoSync() {
        syncJob?.cancel()
    }
}
