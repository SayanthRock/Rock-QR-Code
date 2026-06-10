package com.example.utils

import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer

class QrCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    // Cache hints for faster/restricted barcode searching
    private val reader = MultiFormatReader().apply {
        val hints = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE)
        )
        setHints(hints)
    }

    private var lastScannedTime = 0L

    override fun analyze(image: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        // Limit decoding attempts to once per 500ms to save CPU
        if (currentTime - lastScannedTime < 500) {
            image.close()
            return
        }

        val format = image.format
        if (format == ImageFormat.YUV_420_888 || format == 35) { // 35 is YUV_420_888
            val buffer = image.planes[0].buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)

            val width = image.width
            val height = image.height

            // Crop or use full frame (full frame is easiest)
            val source = PlanarYUVLuminanceSource(
                data,
                width,
                height,
                0,
                0,
                width,
                height,
                false
            )

            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            try {
                val result = reader.decode(binaryBitmap)
                val text = result.text ?: ""
                if (text.isNotEmpty()) {
                    lastScannedTime = currentTime
                    onQrCodeScanned(text)
                }
            } catch (e: NotFoundException) {
                // No QR code found in this frame, try rotated image if needed or just skip
                tryRotated(data, width, height, currentTime)
            } catch (e: Exception) {
                // Other barcode scan errors
            } finally {
                image.close()
            }
        } else {
            image.close()
        }
    }

    private fun tryRotated(data: ByteArray, width: Int, height: Int, currentTime: Long) {
        // Rotated YUV decoding helper (important for portrait-held cameras)
        val rotatedData = ByteArray(data.size)
        for (y in 0 until height) {
            for (x in 0 until width) {
                rotatedData[x * height + height - y - 1] = data[x + y * width]
            }
        }
        val source = PlanarYUVLuminanceSource(
            rotatedData,
            height,
            width,
            0,
            0,
            height,
            width,
            false
        )
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        try {
            val result = reader.decode(binaryBitmap)
            val text = result.text ?: ""
            if (text.isNotEmpty()) {
                lastScannedTime = currentTime
                onQrCodeScanned(text)
            }
        } catch (e: Exception) {
            // Keep silent
        }
    }
}
