package com.example.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

enum class QrStyle {
    CLASSIC,
    ROUNDED_DOT,
    ROCK
}

object QrCodeGenerator {
    fun generateQrCode(
        content: String,
        width: Int = 512,
        height: Int = 512,
        foregroundHexColor: String = "#0A0A0A",
        backgroundHexColor: String = "#FFFFFF",
        style: QrStyle = QrStyle.CLASSIC,
        embedLogo: String = "NONE", // NONE, CRYSTAL, SPARK, DIAMOND
        errorCorrection: ErrorCorrectionLevel = ErrorCorrectionLevel.H
    ): Bitmap? {
        if (content.isEmpty()) return null
        return try {
            val writer = MultiFormatWriter()
            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to errorCorrection,
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 1
            )
            // ZXing encode
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints)

            val matrixWidth = bitMatrix.width
            val matrixHeight = bitMatrix.height
            val blockWidth = width.toFloat() / matrixWidth
            val blockHeight = height.toFloat() / matrixHeight

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply {
                isAntiAlias = true
            }

            // Colors
            val fgColor = try { Color.parseColor(foregroundHexColor) } catch (e: Exception) { Color.parseColor("#0A0A0A") }
            val bgColor = try { Color.parseColor(backgroundHexColor) } catch (e: Exception) { Color.parseColor("#FFFFFF") }

            // Clear / Background
            paint.color = bgColor
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

            paint.color = fgColor

            for (x in 0 until matrixWidth) {
                for (y in 0 until matrixHeight) {
                    if (bitMatrix[x, y]) {
                        val left = x * blockWidth
                        val top = y * blockHeight
                        val right = left + blockWidth
                        val bottom = top + blockHeight

                        // Finder patterns are the three large 7x7 outer square rings
                        val isFinder = (x < 7 && y < 7) || 
                                       (x >= matrixWidth - 7 && y < 7) || 
                                       (x < 7 && y >= matrixHeight - 7)

                        if (isFinder) {
                            // Draw finder pattern strictly solid for bulletproof scanner alignment
                            canvas.drawRect(left, top, right, bottom, paint)
                        } else {
                            when (style) {
                                QrStyle.CLASSIC -> {
                                    canvas.drawRect(left + 0.5f, top + 0.5f, right - 0.5f, bottom - 0.5f, paint)
                                }
                                QrStyle.ROUNDED_DOT -> {
                                    val cx = left + blockWidth / 2f
                                    val cy = top + blockHeight / 2f
                                    val radius = (blockWidth / 2f) * 0.82f
                                    canvas.drawCircle(cx, cy, radius, paint)
                                }
                                QrStyle.ROCK -> {
                                    // Rock Crystal style: rotated rectangles, angled block structures
                                    val margin = blockWidth * 0.1f
                                    val rect = RectF(left + margin, top + margin, right - margin, bottom - margin)
                                    val cornerRadius = if ((x * y) % 3 == 0) blockWidth * 0.4f else blockWidth * 0.15f
                                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
                                }
                            }
                        }
                    }
                }
            }

            // DRAW CENTER BRAND EMBLEM LOGO LAYER
            if (embedLogo != "NONE") {
                val cx = width / 2f
                val cy = height / 2f
                val logoSize = width * 0.22f
                val halfSize = logoSize / 2f

                // Draw background mask/backplate to block out QR dots in error correction zone
                paint.color = bgColor
                paint.style = Paint.Style.FILL
                val backplateRect = RectF(cx - halfSize - 4f, cy - halfSize - 4f, cx + halfSize + 4f, cy + halfSize + 4f)
                canvas.drawRoundRect(backplateRect, logoSize * 0.35f, logoSize * 0.35f, paint)

                // Optional neon stroke outline for the logo backplate
                paint.color = fgColor
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f
                canvas.drawRoundRect(backplateRect, logoSize * 0.35f, logoSize * 0.35f, paint)

                paint.style = Paint.Style.FILL
                
                when (embedLogo) {
                    "CRYSTAL" -> {
                        // Drawing a beautiful faceted gemstone shape using Path
                        val path = android.graphics.Path().apply {
                            moveTo(cx, cy - halfSize * 0.8f) // Top peak
                            lineTo(cx + halfSize * 0.7f, cy - halfSize * 0.2f) // Upper Right
                            lineTo(cx + halfSize * 0.5f, cy + halfSize * 0.6f) // Lower Right
                            lineTo(cx, cy + halfSize * 0.9f) // Bottom point
                            lineTo(cx - halfSize * 0.5f, cy + halfSize * 0.6f) // Lower Left
                            lineTo(cx - halfSize * 0.7f, cy - halfSize * 0.2f) // Upper Left
                            close()
                        }
                        canvas.drawPath(path, paint)
                        
                        // Add some beautiful faceted contrast lines
                        paint.color = bgColor
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = 2f
                        canvas.drawLine(cx, cy - halfSize * 0.8f, cx, cy + halfSize * 0.9f, paint)
                        canvas.drawLine(cx - halfSize * 0.7f, cy - halfSize * 0.2f, cx + halfSize * 0.7f, cy - halfSize * 0.2f, paint)
                    }
                    "SPARK" -> {
                        // Golden aura 4-point star lens flare
                        val path = android.graphics.Path().apply {
                            moveTo(cx, cy - halfSize * 0.9f)
                            quadTo(cx, cy, cx + halfSize * 0.9f, cy)
                            quadTo(cx, cy, cx, cy + halfSize * 0.9f)
                            quadTo(cx, cy, cx - halfSize * 0.9f, cy)
                            quadTo(cx, cy, cx, cy - halfSize * 0.9f)
                            close()
                        }
                        canvas.drawPath(path, paint)
                    }
                    "DIAMOND" -> {
                        // Rotated diamond chiseled box
                        val path = android.graphics.Path().apply {
                            moveTo(cx, cy - halfSize * 0.85f)
                            lineTo(cx + halfSize * 0.85f, cy)
                            lineTo(cx, cy + halfSize * 0.85f)
                            lineTo(cx - halfSize * 0.85f, cy)
                            close()
                        }
                        canvas.drawPath(path, paint)
                        
                        // Internal concentric digital square
                        paint.color = bgColor
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = 3f
                        val pathIn = android.graphics.Path().apply {
                            moveTo(cx, cy - halfSize * 0.45f)
                            lineTo(cx + halfSize * 0.45f, cy)
                            lineTo(cx, cy + halfSize * 0.45f)
                            lineTo(cx - halfSize * 0.45f, cy)
                            close()
                        }
                        canvas.drawPath(pathIn, paint)
                    }
                }
            }

            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
