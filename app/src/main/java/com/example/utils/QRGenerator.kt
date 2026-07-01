package com.example.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.EnumMap

object QRGenerator {
    fun generate(
        text: String,
        primaryColor: Int = android.graphics.Color.BLACK,
        secondaryColor: Int = android.graphics.Color.WHITE,
        style: String = "Classic", // "Classic", "Rounded", "Circles", "Thin", "Smooth"
        eyeColor: Int = primaryColor,
        innerEyeColor: Int = eyeColor
    ): Bitmap {
        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
        hints[EncodeHintType.MARGIN] = 1 // Neat compact margin

        val size = 512
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            text, BarcodeFormat.QR_CODE, size, size, hints
        )

        val matrixWidth = bitMatrix.width
        val matrixHeight = bitMatrix.height

        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        // Draw solid background color
        val bgPaint = Paint().apply {
            color = secondaryColor
            this.style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

        val cellSize = size.toFloat() / matrixWidth

        val paint = Paint().apply {
            isAntiAlias = true
            this.style = Paint.Style.FILL
        }

        for (y in 0 until matrixHeight) {
            for (x in 0 until matrixWidth) {
                if (bitMatrix[x, y]) {
                    // Check if coordinate is part of standard Finder Pattern (Eyes)
                    val isTL = x in 0..6 && y in 0..6
                    val isTR = x in (matrixWidth - 7) until matrixWidth && y in 0..6
                    val isBL = x in 0..6 && y in (matrixHeight - 7) until matrixHeight

                    if (isTL || isTR || isBL) {
                        // Finder Pattern eyes are handled separately at the end for high-fidelity drawing
                        continue
                    }

                    // Otherwise, draw standard data modules
                    paint.color = primaryColor
                    val left = x * cellSize
                    val top = y * cellSize
                    val right = (x + 1) * cellSize
                    val bottom = (y + 1) * cellSize

                    when (style) {
                        "Circles" -> {
                            val centerX = left + cellSize / 2f
                            val centerY = top + cellSize / 2f
                            canvas.drawCircle(centerX, centerY, cellSize * 0.42f, paint)
                        }
                        "Thin" -> {
                            val inset = cellSize * 0.18f
                            canvas.drawRect(
                                left + inset,
                                top + inset,
                                right - inset,
                                bottom - inset,
                                paint
                            )
                        }
                        "Rounded" -> {
                            val rect = RectF(
                                left + cellSize * 0.05f,
                                top + cellSize * 0.05f,
                                right - cellSize * 0.05f,
                                bottom - cellSize * 0.05f
                            )
                            canvas.drawRoundRect(rect, cellSize * 0.35f, cellSize * 0.35f, paint)
                        }
                        "Smooth" -> {
                            // Smoothly connects with neighboring dark modules
                            val hasLeft = x > 0 && bitMatrix[x - 1, y] && !isFinder(x - 1, y, matrixWidth, matrixHeight)
                            val hasRight = x < matrixWidth - 1 && bitMatrix[x + 1, y] && !isFinder(x + 1, y, matrixWidth, matrixHeight)
                            val hasTop = y > 0 && bitMatrix[x, y - 1] && !isFinder(x, y - 1, matrixWidth, matrixHeight)
                            val hasBottom = y < matrixHeight - 1 && bitMatrix[x, y + 1] && !isFinder(x, y + 1, matrixWidth, matrixHeight)

                            val rx = cellSize * 0.4f
                            val ry = cellSize * 0.4f
                            val rect = RectF(left, top, right, bottom)
                            val path = Path()
                            path.addRoundRect(
                                rect,
                                floatArrayOf(
                                    if (hasLeft || hasTop) 0f else rx, if (hasLeft || hasTop) 0f else ry,
                                    if (hasRight || hasTop) 0f else rx, if (hasRight || hasTop) 0f else ry,
                                    if (hasRight || hasBottom) 0f else rx, if (hasRight || hasBottom) 0f else ry,
                                    if (hasLeft || hasBottom) 0f else rx, if (hasLeft || hasBottom) 0f else ry
                                ),
                                Path.Direction.CW
                            )
                            canvas.drawPath(path, paint)
                        }
                        else -> { // "Classic"
                            canvas.drawRect(left, top, right, bottom, paint)
                        }
                    }
                }
            }
        }

        // Draw custom-styled high-fidelity Finder Patterns (Eyes)
        drawPremiumFinderPattern(canvas, 0f, 0f, cellSize, eyeColor, innerEyeColor)
        drawPremiumFinderPattern(canvas, (matrixWidth - 7) * cellSize, 0f, cellSize, eyeColor, innerEyeColor)
        drawPremiumFinderPattern(canvas, 0f, (matrixHeight - 7) * cellSize, cellSize, eyeColor, innerEyeColor)

        return bmp
    }

    private fun isFinder(x: Int, y: Int, width: Int, height: Int): Boolean {
        val isTL = x in 0..6 && y in 0..6
        val isTR = x in (width - 7) until width && y in 0..6
        val isBL = x in 0..6 && y in (height - 7) until height
        return isTL || isTR || isBL
    }

    private fun drawPremiumFinderPattern(
        canvas: Canvas,
        left: Float,
        top: Float,
        cellSize: Float,
        eyeColor: Int,
        innerEyeColor: Int
    ) {
        val outerPaint = Paint().apply {
            isAntiAlias = true
            color = eyeColor
            style = Paint.Style.STROKE
            strokeWidth = cellSize
        }

        val innerPaint = Paint().apply {
            isAntiAlias = true
            color = innerEyeColor
            style = Paint.Style.FILL
        }

        // Outer Ring: 7x7 modules. Border is half module width from edge.
        val halfCell = cellSize / 2f
        val outerRect = RectF(
            left + halfCell,
            top + halfCell,
            left + 7 * cellSize - halfCell,
            top + 7 * cellSize - halfCell
        )
        // Draw smooth rounded ring for Finder Pattern
        canvas.drawRoundRect(outerRect, cellSize * 1.5f, cellSize * 1.5f, outerPaint)

        // Inner solid core: 3x3 modules centered
        val innerRect = RectF(
            left + 2 * cellSize,
            top + 2 * cellSize,
            left + 5 * cellSize,
            top + 5 * cellSize
        )
        canvas.drawRoundRect(innerRect, cellSize * 0.8f, cellSize * 0.8f, innerPaint)
    }
}
