package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ShareUtils {
    /**
     * Shares a [Bitmap] to other applications via Android FileProvider.
     */
    fun shareBitmap(context: Context, bitmap: Bitmap, fileName: String = "shared_qr_code.png") {
        try {
            // Establish the shared images cache subdirectory
            val cachePath = File(context.cacheDir, "shared_images")
            if (!cachePath.exists()) {
                cachePath.mkdirs()
            } else {
                // Clear older shared frames to remain extremely lightweight and performant
                cachePath.listFiles()?.forEach { oldFile ->
                    if (oldFile.isFile) {
                        oldFile.delete()
                    }
                }
            }

            // Write the bitmap to cache space
            val file = File(cachePath, fileName)
            FileOutputStream(file).use { outStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                outStream.flush()
            }

            // Generate content URI for sharing
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            if (uri != null) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                context.startActivity(Intent.createChooser(intent, "Share QR Code Image"))
            } else {
                Toast.makeText(context, "Failed to prepare sharing content", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error sharing QR code: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
