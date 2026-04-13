package com.logisparktech.parkingmanagementsystem.domain.use_case

import kotlin.math.ceil

import javax.inject.Inject

//class CalculateAmountUseCase @Inject constructor() {
//    operator fun invoke(
//        entryTime: Long,
//        exitTime: Long,
//        rate: Double,
//        halfHourCost: Double,
//        exceedingLimitMin: Int,
//        is30MinActivation: Boolean
//    ): Double {
//        val durationMillis = exitTime - entryTime
//        if (durationMillis <= 0) return 0.0
//
//        val totalMinutes = durationMillis / (1000 * 60)
//        val chargeableMinutes = (totalMinutes - exceedingLimitMin).coerceAtLeast(0)
//
//        // Minimum charge rule: if less than 60 minutes -> charge 1 hour
//        if (chargeableMinutes < 60) {
//            return rate
//        }
//
//        return if (is30MinActivation) {
//            val hoursPart = (chargeableMinutes / 60).toInt()
//            val minutesPart = chargeableMinutes % 60
//
//            when {
//                minutesPart == 0L -> hoursPart * rate
//                minutesPart <= 30 -> (hoursPart * rate) + halfHourCost
//                else -> (hoursPart + 1) * rate
//            }
//        } else {
//            val hours = ceil(chargeableMinutes / 60.0).toInt()
//            hours * rate
//        }
//    }
//}



class CalculateAmountUseCase @Inject constructor() {

    operator fun invoke(
        entryTime: Long,
        exitTime: Long,
        rate: Double,
        halfHourCost: Double,
        exceedingLimitMin: Int,
        is30MinActivation: Boolean
    ): Double {

        val durationMillis = exitTime - entryTime
        if (durationMillis <= 0) return 0.0

        val totalMinutes = durationMillis / (1000 * 60)

        // 🔹 Apply grace period
        val chargeableMinutes = (totalMinutes - exceedingLimitMin).coerceAtLeast(0)

        // ✅ No charge inside grace
        if (chargeableMinutes == 0L) return 0.0

        // 🔹 Minimum 1-hour charge
        if (chargeableMinutes < 60L) return rate

        // -------------------------------
        // 🔹 30-MINUTE BILLING LOGIC
        // -------------------------------
        if (is30MinActivation) {

            val hours = (chargeableMinutes / 60).toInt()
            val remainingMinutes = chargeableMinutes % 60L

            return when {
                remainingMinutes == 0L -> {
                    // ✅ Exact hour (e.g., 120 min)
                    hours * rate
                }

                remainingMinutes <= 30L -> {
                    // ✅ Up to 30 mins (e.g., 135 min)
                    (hours * rate) + halfHourCost
                }

                else -> {
                    // ✅ More than 30 mins (e.g., 165 min)
                    (hours + 1) * rate
                }
            }
        }

        // -------------------------------
        // 🔹 NORMAL BILLING (NO 30-MIN MODE)
        // -------------------------------
        val hours = ceil(chargeableMinutes / 60.0).toInt()
        return hours * rate
    }
}