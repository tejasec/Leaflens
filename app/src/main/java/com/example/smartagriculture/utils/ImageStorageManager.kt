package com.example.smartagriculture.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

/**
 * Utility responsible for permanently storing and resolving leaf scan images.
 * Ensures that captured and gallery-selected photos are safely retained in internal
 * app storage and loaded correctly in History and Details screens without permission expiration.
 */
object ImageStorageManager {

    private const val DIR_CAPTURES = "scan_captures"
    private const val DIR_SCAN_IMAGES = "scan_images"

    /**
     * Directory for temporary scan captures and imported gallery images before saving to history.
     */
    fun getCapturesDir(context: Context): File {
        val dir = File(context.filesDir, DIR_CAPTURES)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Permanent directory for leaf scan images saved in Room DB history.
     */
    fun getHistoryImagesDir(context: Context): File {
        val dir = File(context.filesDir, DIR_SCAN_IMAGES)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Copies an input stream to a target destination file.
     */
    fun copyStreamToFile(inputStream: InputStream, destinationFile: File): Boolean {
        return try {
            destinationFile.parentFile?.mkdirs()
            FileOutputStream(destinationFile).use { output ->
                inputStream.copyTo(output)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Copies a Uri (content:// or file://) to a target destination file.
     */
    fun copyUriToFile(context: Context, sourceUri: Uri, destinationFile: File): Boolean {
        return try {
            val inputStream: InputStream? = when (sourceUri.scheme) {
                "content" -> context.contentResolver.openInputStream(sourceUri)
                "file" -> FileInputStream(File(sourceUri.path ?: sourceUri.toString().removePrefix("file://")))
                else -> {
                    val rawPath = sourceUri.path ?: sourceUri.toString()
                    val candidate = File(rawPath)
                    if (candidate.exists()) FileInputStream(candidate) else null
                }
            }

            if (inputStream == null) return false
            inputStream.use { input ->
                copyStreamToFile(input, destinationFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Saves a bitmap directly to a destination file.
     */
    fun saveBitmapToFile(bitmap: Bitmap, destinationFile: File, quality: Int = 92): Boolean {
        return try {
            destinationFile.parentFile?.mkdirs()
            FileOutputStream(destinationFile).use { out ->
                val safeBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
                    bitmap.copy(Bitmap.Config.ARGB_8888, false)
                } else {
                    bitmap
                }
                safeBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Ingests a newly picked gallery image or capture URI into `filesDir/scan_captures`.
     * Returns the absolute path of the persisted file so subsequent fragments
     * never face transient permission issues.
     */
    fun ingestCaptureUri(context: Context, sourceUri: Uri): String {
        val capturesDir = getCapturesDir(context)
        val file = File(capturesDir, "capture_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")
        val success = copyUriToFile(context, sourceUri, file)
        return if (success) file.absolutePath else sourceUri.toString()
    }

    /**
     * Permanently stores a leaf scan image in `filesDir/scan_images` for Room DB persistence.
     * If the image is already located in the permanent history directory, returns the path as-is.
     * Otherwise, copies the image bytes to a permanent history file and returns its absolute path.
     */
    fun persistScanImage(context: Context, sourceUriOrPath: String?): String {
        if (sourceUriOrPath.isNullOrBlank()) return ""

        val historyDir = getHistoryImagesDir(context)

        // Check if already in permanent storage
        if (sourceUriOrPath.startsWith(historyDir.absolutePath)) {
            val existing = File(sourceUriOrPath)
            if (existing.exists()) {
                return existing.absolutePath
            }
        }

        val destFile = File(
            historyDir,
            "leaf_scan_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
        )

        var copySuccess = false

        if (sourceUriOrPath.startsWith("content://")) {
            val uri = Uri.parse(sourceUriOrPath)
            copySuccess = copyUriToFile(context, uri, destFile)
        } else if (sourceUriOrPath.startsWith("file://")) {
            val uri = Uri.parse(sourceUriOrPath)
            copySuccess = copyUriToFile(context, uri, destFile)
        } else if (sourceUriOrPath.startsWith("/")) {
            val sourceFile = File(sourceUriOrPath)
            if (sourceFile.exists()) {
                copySuccess = try {
                    FileInputStream(sourceFile).use { input ->
                        copyStreamToFile(input, destFile)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
        } else {
            // Attempt generic URI parse fallback
            try {
                val uri = Uri.parse(sourceUriOrPath)
                copySuccess = copyUriToFile(context, uri, destFile)
            } catch (_: Exception) {
                copySuccess = false
            }
        }

        return if (copySuccess) {
            destFile.absolutePath
        } else {
            // Fallback to original string if copying failed
            sourceUriOrPath
        }
    }

    /**
     * Resolves a Glide image model from a stored image path or URI string.
     * Supports absolute file paths, file:// URIs, and content:// URIs.
     */
    fun getImageModel(imagePath: String?): Any? {
        if (imagePath.isNullOrBlank()) return null

        return when {
            imagePath.startsWith("/") -> File(imagePath)
            imagePath.startsWith("file://") -> {
                val parsed = Uri.parse(imagePath)
                val path = parsed.path
                if (!path.isNullOrBlank()) File(path) else parsed
            }
            imagePath.startsWith("content://") -> Uri.parse(imagePath)
            else -> {
                val fileCandidate = File(imagePath)
                if (fileCandidate.exists()) fileCandidate else imagePath
            }
        }
    }
}
