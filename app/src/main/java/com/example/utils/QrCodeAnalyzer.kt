package com.example.utils

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer

class QrCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        setHints(
            mapOf(
                DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
                DecodeHintType.TRY_HARDER to true,
                DecodeHintType.CHARACTER_SET to "UTF-8"
            )
        )
    }

    override fun analyze(image: ImageProxy) {
        try {
            val luminanceBytes = image.toLuminanceByteArray()
            val source = PlanarYUVLuminanceSource(
                luminanceBytes,
                image.width,
                image.height,
                0,
                0,
                image.width,
                image.height,
                false
            )
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            val result = reader.decodeWithState(bitmap)
            val text = result.text.orEmpty().trim()
            if (text.isNotEmpty()) {
                onQrCodeScanned(text)
            }
        } catch (_: NotFoundException) {
            // No QR code found in this frame.
        } catch (_: Exception) {
            // Keep the camera analyzer alive even when one frame fails.
        } finally {
            reader.reset()
            image.close()
        }
    }

    private fun ImageProxy.toLuminanceByteArray(): ByteArray {
        val plane = planes.first()
        val buffer = plane.buffer
        val width = width
        val height = height
        val rowStride = plane.rowStride
        val pixelStride = plane.pixelStride
        val luminance = ByteArray(width * height)
        val row = ByteArray(rowStride)
        var outputOffset = 0

        buffer.rewind()
        for (rowIndex in 0 until height) {
            val bytesToRead = minOf(rowStride, buffer.remaining())
            if (bytesToRead <= 0) break
            buffer.get(row, 0, bytesToRead)

            var column = 0
            while (column < width) {
                val sourceIndex = column * pixelStride
                if (sourceIndex < bytesToRead && outputOffset < luminance.size) {
                    luminance[outputOffset++] = row[sourceIndex]
                }
                column++
            }
        }

        return luminance
    }
}
