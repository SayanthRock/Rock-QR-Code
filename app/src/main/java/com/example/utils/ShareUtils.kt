package com.example.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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

    /**
     * Saves a [Bitmap] directly to the device photos gallery / downloads.
     */
    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, displayName: String = "RockQR_") {
        val resolver = context.contentResolver
        val filename = "$displayName${System.currentTimeMillis()}.png"
        
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Save to Pictures/RockQR folder
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/RockQR")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (imageUri == null) {
            Toast.makeText(context, "Failed to create save destination in Gallery", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            resolver.openOutputStream(imageUri).use { outStream ->
                if (outStream == null) {
                    throw IOException("Could not open gallery output stream")
                }
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                outStream.flush()
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
            }
            
            Toast.makeText(context, "Downloaded successfully to Gallery (Pictures/RockQR)", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            resolver.delete(imageUri, null, null)
            e.printStackTrace()
            Toast.makeText(context, "Error saving QR code: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
