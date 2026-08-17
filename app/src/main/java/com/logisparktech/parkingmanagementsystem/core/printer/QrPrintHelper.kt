package com.logisparktech.parkingmanagementsystem.core.printer

import android.util.Log
import com.google.zxing.WriterException
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.Encoder
import com.google.zxing.qrcode.encoder.QRCode

/**
 * Helper object to handle QR code printing calculations for Sunmi thermal printers.
 */
object QrPrintHelper {

    private const val TAG = "QrPrintHelper"

    // Sunmi standard dots for common paper widths
    // 8 dots/mm resolution
    const val PAPER_WIDTH_50MM = 320 // 40mm printable area
    const val PAPER_WIDTH_58MM = 384 // 48mm printable area
    const val PAPER_WIDTH_80MM = 576 // 72mm printable area
    
    // Default safe values for Sunmi printQRCode(content, moduleSize, errorLevel)
    private const val MIN_MODULE_SIZE = 3
    private const val MAX_MODULE_SIZE = 16
    private const val DEFAULT_MODULE_SIZE = 4

    /**
     * Calculates the correct moduleSize (dots per module) so the QR code
     * fits appropriately within the printer's paper width.
     *
     * @param data The string content to encode in the QR code.
     * @param errorLevel 0=L, 1=M, 2=Q, 3=H (matches Sunmi's internal mapping).
     * @param paperWidthDots The printable width of the paper in dots.
     * @return Calculated module size (1-16) for Sunmi printer.
     */
    fun calculateModuleSize(data: String, errorLevel: Int, paperWidthDots: Int): Int {
        if (data.isBlank()) return DEFAULT_MODULE_SIZE

        return try {
            val ecLevel = when (errorLevel) {
                0 -> ErrorCorrectionLevel.L
                1 -> ErrorCorrectionLevel.M
                2 -> ErrorCorrectionLevel.Q
                else -> ErrorCorrectionLevel.H
            }

            // Use ZXing to calculate the matrix size of the QR code
            val qrCode: QRCode = Encoder.encode(data, ecLevel)
            val moduleCount = qrCode.matrix.width // Number of modules (grid size)

            // Dots per module = Total Dots / Number of Modules
            var moduleSize = paperWidthDots / moduleCount

            // Constrain module size to Sunmi's supported range and scannability
            if (moduleSize < MIN_MODULE_SIZE) moduleSize = MIN_MODULE_SIZE
            if (moduleSize > MAX_MODULE_SIZE) moduleSize = MAX_MODULE_SIZE

            moduleSize
        } catch (e: WriterException) {
            Log.e(TAG, "Failed to calculate QR module size for data: $data", e)
            DEFAULT_MODULE_SIZE
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in calculateModuleSize", e)
            DEFAULT_MODULE_SIZE
        }
    }

    /**
     * Generates a divider string of dashes that fits the paper width.
     *
     * @param paperWidthDots The printable width in dots.
     * @param charWidth The approximate width of a character in dots (usually ~12 for standard font).
     * @return A string of dashes to act as a separator.
     */
    fun getDivider(paperWidthDots: Int, charWidth: Int = 12): String {
        // Narrow printers (50mm) are very sensitive to character counts.
        // We use a safer character count to ensure no wrapping occurs.
        val count = when (paperWidthDots) {
            PAPER_WIDTH_50MM -> 24 // 50mm usually fits 26-32, 24 is safe
            PAPER_WIDTH_58MM -> 28 // 58mm usually fits 32, 30 is safe
            PAPER_WIDTH_80MM -> 32 // 80mm usually fits 48, 46 is safe
            else -> (paperWidthDots / 13).coerceAtLeast(10)
        }
        return "-".repeat(count) + "\n"
    }
}
