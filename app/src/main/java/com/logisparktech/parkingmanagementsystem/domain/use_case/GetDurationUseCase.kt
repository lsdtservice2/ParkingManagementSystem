package com.logisparktech.parkingmanagementsystem.domain.use_case

import javax.inject.Inject

class GetDurationUseCase @Inject constructor() {
    operator fun invoke(entryTime: Long, exitTime: Long, exceedingLimitMin: Int = 0): String {
        val durationMillis = exitTime - entryTime
        if (durationMillis <= 0) return "0 hour 0 min"

        // Calculate total minutes, rounding up if there are remaining seconds
        // to show "actual duration" as requested.
        val totalSeconds = durationMillis / 1000
        val totalMinutes = if (totalSeconds % 60 > 0) (totalSeconds / 60) + 1 else totalSeconds / 60
        
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return "$hours hour $minutes min"
    }
}
