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

    /**
     * Helper to get the current printer's printable width in dots.
     */
    private fun getPaperWidthDots(): Int {
        return try {
            // Sunmi getPrinterPaper() typically returns:
            // 1: 80mm (576 dots)
            // 2: 58mm (384 dots)
            // 3: 50mm (320 dots) - on specific models
            val paperType = sunmiPrinterService?.printerPaper ?: 3 // Default to 50mm per user hardware
            Log.d(TAG, "Detected paper type: $paperType")
            
            when (paperType) {
                1 -> QrPrintHelper.PAPER_WIDTH_80MM
                2 -> QrPrintHelper.PAPER_WIDTH_58MM
                3 -> QrPrintHelper.PAPER_WIDTH_50MM
                else -> {
                    // Default to 50mm as requested by the user
                    QrPrintHelper.PAPER_WIDTH_50MM
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get paper width, defaulting to 50mm", e)
            QrPrintHelper.PAPER_WIDTH_50MM
        }
    }

    fun printTicket(
        ticketId: String,
        vehicleNumber: String,
        vehicleType: String,
        entryDate: String,
        entryTime: String,
        rate: Double? = null,
        qrCodeContent: String? = null,
        qrCodeBitmap: Bitmap? = null
    ) {
        val service = sunmiPrinterService
        if (service == null) {
            Log.e(TAG, "Printer service not connected")
            return
        }

        try {
            val paperWidth = getPaperWidthDots()
            val divider = QrPrintHelper.getDivider(paperWidth)

            service.enterPrinterBuffer(true)
            service.printerInit(resultCallback)

            val safeBranch = "PARKING TICKET"

            // 🔷 HEADER
            service.setAlignment(1, resultCallback) // Center
            val headerSize = if (paperWidth <= QrPrintHelper.PAPER_WIDTH_50MM) 30f else 36f
            service.printTextWithFont(safeBranch + "\n", "", headerSize, resultCallback)

            service.printText(divider, resultCallback)

            // 🔷 QR CODE
            if (!qrCodeContent.isNullOrBlank()) {
                val moduleSize = QrPrintHelper.calculateModuleSize(qrCodeContent, 3, paperWidth)
                service.printQRCode(qrCodeContent, moduleSize, 3, resultCallback)
            } else if (qrCodeBitmap != null) {
                service.printBitmap(qrCodeBitmap, resultCallback)
            }

            service.printText("\n", resultCallback)

            // 🔷 VEHICLE INFO
            service.setAlignment(0, resultCallback) // Left
            service.printTextWithFont("Ticket No  : $ticketId\n", "", 28f, resultCallback)
            service.printText(divider, resultCallback)
            service.printTextWithFont("Vehicle No : $vehicleNumber\n", "", 28f, resultCallback)
            service.printTextWithFont("Type       : $vehicleType\n", "", 28f, resultCallback)

            if (rate != null && rate > 0) {
                service.printTextWithFont("Rate       : रु. ${String.format(Locale.US, "%.2f", rate)} / hr\n", "", 28f, resultCallback)
            }

            service.printText(divider, resultCallback)

            // 🔷 TIME INFO
            service.printTextWithFont("Entry Date : $entryDate\n", "", 26f, resultCallback)
            service.printTextWithFont("Entry Time : $entryTime\n", "", 26f, resultCallback)

            service.printText(divider, resultCallback)

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
            val paperWidth = getPaperWidthDots()
            val divider = QrPrintHelper.getDivider(paperWidth)

            service.enterPrinterBuffer(true)
            service.printerInit(resultCallback)

            val safeBranch = "PARKING RECEIPT"

            // 🔷 HEADER
            service.setAlignment(1, resultCallback)
            val headerSize = if (paperWidth <= QrPrintHelper.PAPER_WIDTH_50MM) 30f else 36f
            service.printTextWithFont("$safeBranch\n", "", headerSize, resultCallback)

            service.printText(divider, resultCallback)

            // 🔷 OPTIONAL TICKET ID
            if (!ticketId.isNullOrBlank()) {
                service.setAlignment(0, resultCallback)
                service.printTextWithFont("Ticket ID : $ticketId\n", "", 24f, resultCallback)
                service.printText(divider, resultCallback)
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

            service.printText(divider, resultCallback)

            // 🔷 TOTAL AMOUNT (HIGHLIGHT)
            service.setAlignment(1, resultCallback)
            service.printTextWithFont("TOTAL AMOUNT\n", "", 26f, resultCallback)
            val amountSize = if (paperWidth <= QrPrintHelper.PAPER_WIDTH_50MM) 34f else 40f
            service.printTextWithFont(
                "रु. ${String.format(Locale.US, "%.2f", amount)}\n",
                "",
                amountSize,
                resultCallback
            )

            service.printText(divider, resultCallback)

            // 🔷 FOOTER
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
