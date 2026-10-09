package com.example.smartagriculture.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ImageStorageManagerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
    }

    @Test
    fun testGetDirectories_existOrCreated() {
        val capturesDir = ImageStorageManager.getCapturesDir(context)
        val historyDir = ImageStorageManager.getHistoryImagesDir(context)

        assertTrue(capturesDir.exists())
        assertTrue(capturesDir.isDirectory)
        assertTrue(historyDir.exists())
        assertTrue(historyDir.isDirectory)
    }

    @Test
    fun testPersistScanImage_fromFilePath_copiesToHistoryDir() {
        // Create a temporary source file (e.g. in cacheDir)
        val sourceFile = File(context.cacheDir, "temp_capture_123.jpg")
        sourceFile.writeText("fake image bytes")

        val persistedPath = ImageStorageManager.persistScanImage(context, sourceFile.absolutePath)

        assertTrue("Persisted path must not be empty", persistedPath.isNotBlank())
        assertTrue("Must be stored in history directory", persistedPath.contains("scan_images"))

        val targetFile = File(persistedPath)
        assertTrue("Target file must exist on disk", targetFile.exists())
        assertEquals("File content must match", "fake image bytes", targetFile.readText())
    }

    @Test
    fun testPersistScanImage_alreadyInHistoryDir_doesNotDuplicate() {
        val historyDir = ImageStorageManager.getHistoryImagesDir(context)
        val existingHistoryFile = File(historyDir, "leaf_scan_test_existing.jpg")
        existingHistoryFile.writeText("existing image bytes")

        val resultPath = ImageStorageManager.persistScanImage(context, existingHistoryFile.absolutePath)

        assertEquals("Should return the existing path without re-copying", existingHistoryFile.absolutePath, resultPath)
    }

    @Test
    fun testPersistScanImage_blankOrNull_returnsEmpty() {
        assertEquals("", ImageStorageManager.persistScanImage(context, null))
        assertEquals("", ImageStorageManager.persistScanImage(context, ""))
        assertEquals("", ImageStorageManager.persistScanImage(context, "   "))
    }

    @Test
    fun testGetImageModel_resolvesAppropriateType() {
        assertNull(ImageStorageManager.getImageModel(null))
        assertNull(ImageStorageManager.getImageModel(""))

        // File path
        val filePath = "/data/user/0/com.example.smartagriculture/files/scan_images/leaf.jpg"
        val fileModel = ImageStorageManager.getImageModel(filePath)
        assertTrue("Model for '/' prefix must be a File", fileModel is File)
        assertEquals(filePath, (fileModel as File).absolutePath)

        // file:// URI
        val fileUriStr = "file:///storage/emulated/0/Download/sample.jpg"
        val fileUriModel = ImageStorageManager.getImageModel(fileUriStr)
        assertTrue("Model for 'file://' prefix must resolve to File or Uri", fileUriModel is File || fileUriModel is Uri)

        // content:// URI
        val contentUriStr = "content://media/external/images/media/42"
        val contentUriModel = ImageStorageManager.getImageModel(contentUriStr)
        assertTrue("Model for 'content://' prefix must be Uri", contentUriModel is Uri)
        assertEquals(Uri.parse(contentUriStr), contentUriModel)
    }

    @Test
    fun testSaveBitmapToFile_createsFileOnDisk() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val targetFile = File(ImageStorageManager.getCapturesDir(context), "bitmap_test.jpg")

        val saved = ImageStorageManager.saveBitmapToFile(bitmap, targetFile)
        assertTrue("saveBitmapToFile must return true", saved)
        assertTrue("Target file must exist", targetFile.exists())
        assertTrue("File must have non-zero length", targetFile.length() > 0)
    }
}
