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
    fun shareBitmap(
        context: Context, 
        bitmap: Bitmap, 
        fileName: String = "shared_qr_code.png",
        onShowToast: ((String, com.example.viewmodel.CustomToastType) -> Unit)? = null
    ) {
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
            val isPng = fileName.endsWith(".png", ignoreCase = true)
            val compressFormat = if (isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG

            FileOutputStream(file).use { outStream ->
                bitmap.compress(compressFormat, 100, outStream)
                outStream.flush()
            }

            // Generate content URI for sharing
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            if (uri != null) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = if (isPng) "image/png" else "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                context.startActivity(Intent.createChooser(intent, "Share QR Code Image"))
            } else {
                val errorMsg = "Failed to prepare sharing content"
                if (onShowToast != null) {
                    onShowToast(errorMsg, com.example.viewmodel.CustomToastType.ERROR)
                } else {
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val errorMsg = "Error sharing QR code: ${e.localizedMessage}"
            if (onShowToast != null) {
                onShowToast(errorMsg, com.example.viewmodel.CustomToastType.ERROR)
            } else {
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Saves a [Bitmap] directly to the device photos gallery / downloads.
     */
    fun saveBitmapToGallery(
        context: Context, 
        bitmap: Bitmap, 
        displayName: String = "ChamoQR_", 
        isPng: Boolean = true,
        onShowToast: ((String, com.example.viewmodel.CustomToastType) -> Unit)? = null
    ) {
        val resolver = context.contentResolver
        val ext = if (isPng) "png" else "jpg"
        val mime = if (isPng) "image/png" else "image/jpeg"
        val filename = "$displayName${System.currentTimeMillis()}.$ext"
        
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, mime)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Save to Pictures/ChamoQR folder
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ChamoQR")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (imageUri == null) {
            val errorMsg = "Failed to create save destination in Gallery"
            if (onShowToast != null) {
                onShowToast(errorMsg, com.example.viewmodel.CustomToastType.ERROR)
            } else {
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }
            return
        }
        
        try {
            resolver.openOutputStream(imageUri).use { outStream ->
                if (outStream == null) {
                    throw IOException("Could not open gallery output stream")
                }
                val compressFormat = if (isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                bitmap.compress(compressFormat, 100, outStream)
                outStream.flush()
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
            }
            
            val successMsg = "Downloaded successfully to Gallery (Pictures/ChamoQR)"
            if (onShowToast != null) {
                onShowToast(successMsg, com.example.viewmodel.CustomToastType.SUCCESS)
            } else {
                Toast.makeText(context, successMsg, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            resolver.delete(imageUri, null, null)
            e.printStackTrace()
            val errorMsg = "Error saving QR code: ${e.localizedMessage}"
            if (onShowToast != null) {
                onShowToast(errorMsg, com.example.viewmodel.CustomToastType.ERROR)
            } else {
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
