package com.logisparktech.parkingmanagementsystem.core.printer

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.sunmi.peripheral.printer.InnerPrinterCallback
import com.sunmi.peripheral.printer.InnerPrinterException
import com.sunmi.peripheral.printer.InnerPrinterManager
import com.sunmi.peripheral.printer.InnerResultCallback
import com.sunmi.peripheral.printer.SunmiPrinterService
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrinterManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var sunmiPrinterService: SunmiPrinterService? = null
    private val TAG = "PrinterManager"

    private val innerPrinterCallback = object : InnerPrinterCallback() {
        override fun onConnected(service: SunmiPrinterService) {
            Log.d(TAG, "Printer Service Connected")
            sunmiPrinterService = service
        }

        override fun onDisconnected() {
            Log.d(TAG, "Printer Service Disconnected")
            sunmiPrinterService = null
        }
    }

    init {
        try {
            InnerPrinterManager.getInstance().bindService(context, innerPrinterCallback)
        } catch (e: InnerPrinterException) {
            Log.e(TAG, "Exception binding printer service", e)
        }
    }

    private val resultCallback = object : InnerResultCallback() {
        override fun onRunResult(isSuccess: Boolean) {
            Log.d(TAG, "onRunResult: $isSuccess")
        }

        override fun onReturnString(result: String?) {
            Log.d(TAG, "onReturnString: $result")
        }

        override fun onRaiseException(code: Int, msg: String?) {
            Log.e(TAG, "onRaiseException: $code, $msg")
        }

        override fun onPrintResult(code: Int, msg: String?) {
            Log.d(TAG, "onPrintResult: $code, $msg")
        }
    }

    fun printTicket(
        branchName: String,
        ticketId: String,
        vehicleNumber: String,
        vehicleType: String,
        entryDate: String,
        entryTime: String,
        qrCodeContent: String? = null,
        qrCodeBitmap: Bitmap? = null
    ) {
        val service = sunmiPrinterService
        if (service == null) {
            Log.e(TAG, "Printer service not connected")
            return
        }

        try {
            service.enterPrinterBuffer(true)
            service.printerInit(resultCallback)

//            val safeBranch = branchName.ifBlank { "Parking System" }
            val safeBranch = "PARKING TICKET"

            // 🔷 HEADER
            service.setAlignment(1, resultCallback) // Center
            service.printTextWithFont(safeBranch + "\n", "", 36f, resultCallback)
//            service.printTextWithFont("PARKING TICKET\n", "", 30f, resultCallback)

            service.printText("-------------------------------\n", resultCallback)

            // 🔷 QR CODE (Top center looks better)
            if (!qrCodeContent.isNullOrBlank()) {
                service.printQRCode(qrCodeContent, 6, 3, resultCallback)
            } else if (qrCodeBitmap != null) {
                service.printBitmap(qrCodeBitmap, resultCallback)
            }

            service.printText("\n", resultCallback)

            // 🔷 VEHICLE INFO
            service.setAlignment(0, resultCallback) // Left
            service.printTextWithFont("Ticket No  : $ticketId\n", "", 28f, resultCallback)
            service.printText("-------------------------------\n", resultCallback)
            service.printTextWithFont("Vehicle No : $vehicleNumber\n", "", 28f, resultCallback)
            service.printTextWithFont("Type       : $vehicleType\n", "", 28f, resultCallback)

            service.printText("-------------------------------\n", resultCallback)

            // 🔷 TIME INFO
            service.printTextWithFont("Entry Date : $entryDate\n", "", 26f, resultCallback)
            service.printTextWithFont("Entry Time : $entryTime\n", "", 26f, resultCallback)

            service.printText("-------------------------------\n", resultCallback)

            // 🔷 FOOTER
            service.setAlignment(1, resultCallback)
            service.printTextWithFont("Please keep this ticket safe\n", "", 24f, resultCallback)
            service.printTextWithFont("Thank you!\n", "", 24f, resultCallback)

            // 🔷 FEED PAPER
            service.lineWrap(4, resultCallback)

            service.exitPrinterBufferWithCallback(true, resultCallback)

        } catch (e: Exception) {
            Log.e(TAG, "Error printing ticket", e)
        }
    }

    fun printReceipt(
        branchName: String,
        vehicleNumber: String,
        entryDate: String,
        entryTime: String,
        exitDate: String,
        exitTime: String,
        amount: Double,
        ticketId: String? = null,
        duration: String? = null
    ) {
        val service = sunmiPrinterService
        if (service == null) {
            Log.e(TAG, "Printer service not connected")
            return
        }

        try {
            service.enterPrinterBuffer(true)
            service.printerInit(resultCallback)

//            val safeBranch = branchName.ifBlank { "Parking System" }
            val safeBranch = "PARKING RECEIPT"

            // 🔷 HEADER
            service.setAlignment(1, resultCallback)
            service.printTextWithFont("$safeBranch\n", "", 36f, resultCallback)
//            service.printTextWithFont("PARKING RECEIPT\n", "", 30f, resultCallback)

            service.printText("--------------------------------\n", resultCallback)

            // 🔷 OPTIONAL TICKET ID
            if (!ticketId.isNullOrBlank()) {
                service.printTextWithFont("Ticket ID : $ticketId\n", "", 24f, resultCallback)
                service.printText("--------------------------------\n", resultCallback)
            }

            // 🔷 DETAILS
            service.setAlignment(0, resultCallback)
            service.printTextWithFont("Vehicle No : $vehicleNumber\n", "", 26f, resultCallback)
            service.printTextWithFont("Entry Date : $entryDate\n", "", 26f, resultCallback)
            service.printTextWithFont("Entry Time : $entryTime\n", "", 26f, resultCallback)
            service.printTextWithFont("Exit Date  : $exitDate\n", "", 26f, resultCallback)
            service.printTextWithFont("Exit Time  : $exitTime\n", "", 26f, resultCallback)

            // 🔷 DURATION (if available)
            if (!duration.isNullOrBlank()) {
                service.printTextWithFont("Duration   : $duration\n", "", 26f, resultCallback)
            }

            service.printText("--------------------------------\n", resultCallback)

            // 🔷 TOTAL AMOUNT (HIGHLIGHT)
            service.setAlignment(1, resultCallback)
            service.printTextWithFont("TOTAL AMOUNT\n", "", 26f, resultCallback)
            service.printTextWithFont(
                "रु. ${String.format(Locale.US, "%.2f", amount)}\n",
                "",
                40f,
                resultCallback
            )

            service.printText("--------------------------------\n", resultCallback)

            // 🔷 FOOTER
//            service.printTextWithFont("Paid Successfully\n", "", 24f, resultCallback)
            service.printTextWithFont("Thank you! Visit again\n", "", 24f, resultCallback)

            // 🔷 FEED PAPER
            service.lineWrap(4, resultCallback)

            service.exitPrinterBufferWithCallback(true, resultCallback)

        } catch (e: Exception) {
            Log.e(TAG, "Error printing receipt", e)
        }
    }
//    fun printSaleReport( 
//        date: String,
//        totalBills: Int,
//        totalAmount: Double,
//        vehicleSales: Map<String, Pair<Int, Double>>
//    ) {
//        val service = sunmiPrinterService
//        if (service == null) {
//            Log.e(TAG, "Printer service not connected")
//            return
//        }
//
//        try {
//            service.enterPrinterBuffer(true)
//            service.printerInit(resultCallback)
//            service.setAlignment(1, resultCallback)
//            service.printTextWithFont("SALE REPORT\n", "", 34f, resultCallback)
//
//            service.setAlignment(0, resultCallback)
//            service.printTextWithFont("Date: $date\n", "", 26f, resultCallback)
//            service.printText("--------------------------------\n", resultCallback)
//
//            for ((type, stats) in vehicleSales) {
//                service.printColumnsText(
//                    arrayOf(type, stats.first.toString(), String.format(Locale.US, "%.2f", stats.second)),
//                    intArrayOf(4, 2, 3),
//                    intArrayOf(0, 1, 2),
//                    resultCallback
//                )
//            }
//            service.printText("--------------------------------\n", resultCallback)
//
//            service.printColumnsText(
//                arrayOf("Total", totalBills.toString(), String.format(Locale.US, "%.2f", totalAmount)),
//                intArrayOf(4, 2, 3),
//                intArrayOf(0, 1, 2),
//                resultCallback
//            )
//
//            service.lineWrap(4, resultCallback)
//            service.exitPrinterBufferWithCallback(true, resultCallback)
//        } catch (e: Exception) {
//            Log.e(TAG, "Error printing sale report", e)
//        }
//    }
//
//    fun feedPaper() {
//        try {
//            sunmiPrinterService?.lineWrap(3, resultCallback)
//        } catch (e: Exception) {
//            Log.e(TAG, "Error feeding paper", e)
//        }
//    }
}
