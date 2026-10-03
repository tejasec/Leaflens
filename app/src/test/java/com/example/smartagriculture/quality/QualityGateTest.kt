package com.example.smartagriculture.quality

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class QualityGateTest {

    @Test
    fun testEvaluateDetailedQuality_uniformColorBitmap_returnsBlurryStatus() {
        // Create 400x400 uniform green bitmap (no high frequency edges)
        val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(400 * 400) { Color.rgb(50, 180, 50) }
        bitmap.setPixels(pixels, 0, 400, 0, 0, 400, 400)

        val result = QualityGate.evaluateDetailedQuality(bitmap)

        // Uniform image has 0 Laplacian variance, so sharpness status should be Blurry
        assertTrue("Expected sharpness status to indicate Blurry", result.sharpnessStatus.contains("Blurry"))
        assertFalse("Uniform image should not pass all quality gates due to blur threshold", result.isValid)
    }

    @Test
    fun testEvaluateDetailedQuality_highContrastPattern_returnsGoodSharpnessStatus() {
        // Create 400x400 checkerboard pattern bitmap to ensure high Laplacian variance
        val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(400 * 400)
        val green = Color.rgb(40, 180, 40)
        val darkGreen = Color.rgb(10, 80, 10)

        for (y in 0 until 400) {
            for (x in 0 until 400) {
                pixels[y * 400 + x] = if (((x / 4) + (y / 4)) % 2 == 0) green else darkGreen
            }
        }
        bitmap.setPixels(pixels, 0, 400, 0, 0, 400, 400)

        val result = QualityGate.evaluateDetailedQuality(bitmap)

        assertEquals("Good ✓", result.sharpnessStatus)
        assertTrue("Foliage leaf should be detected", result.leafDetected)
        assertTrue("High contrast foliage image should be valid", result.isValid)
    }

    @Test
    fun testEvaluateDetailedQuality_greenFoliageBitmap_detectsFoliageCoverage() {
        val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(400 * 400) { Color.rgb(60, 200, 60) }
        bitmap.setPixels(pixels, 0, 400, 0, 0, 400, 400)

        val result = QualityGate.evaluateDetailedQuality(bitmap)

        assertTrue(result.leafDetected)
        assertEquals(100, result.foliageCoveragePct)
    }

    @Test
    fun testValidateImage_returnsQualityResultObject() {
        val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(400 * 400) { Color.rgb(50, 180, 50) }
        bitmap.setPixels(pixels, 0, 400, 0, 0, 400, 400)

        val qualityResult = QualityGate.validateImage(bitmap)

        assertTrue(qualityResult.isBlurry)
        assertFalse(qualityResult.isExtremeLighting)
    }
}
