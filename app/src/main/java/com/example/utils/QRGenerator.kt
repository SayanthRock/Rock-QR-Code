package com.example.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.util.EnumMap

object QRGenerator {
    fun generate(
        text: String,
        primaryColor: Int = Color.BLACK,
        secondaryColor: Int = Color.WHITE
    ): Bitmap {
        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        hints[EncodeHintType.MARGIN] = 1 // Compact elegant margin

        val size = 512
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            text, BarcodeFormat.QR_CODE, size, size, hints
        )

        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) primaryColor else secondaryColor)
            }
        }
        return bmp
    }
}
