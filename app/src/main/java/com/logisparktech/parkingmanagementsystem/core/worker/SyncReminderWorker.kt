package com.logisparktech.parkingmanagementsystem.core.worker

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.logisparktech.parkingmanagementsystem.domain.repository.TicketRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val ticketRepository: TicketRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
//        val unsyncedTickets = ticketRepository.getUnsyncedClosedTickets()
        val unsyncedTickets = ticketRepository.getUnsyncedTickets()
        if (unsyncedTickets.isNotEmpty()) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    context,
                    "Please sync your data now.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        
        return Result.success()
    }
}
