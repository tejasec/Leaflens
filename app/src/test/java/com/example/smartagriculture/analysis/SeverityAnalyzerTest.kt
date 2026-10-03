package com.example.smartagriculture.analysis

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SeverityAnalyzerTest {

    @Test
    fun testAnalyzeSeverity_completelyHealthyLeaf_returnsZeroSeverityAndGrade1() {
        // Create a 100x100 bitmap filled with healthy green foliage
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val healthyGreen = Color.rgb(30, 180, 40)
        val pixels = IntArray(100 * 100) { healthyGreen }
        bitmap.setPixels(pixels, 0, 100, 0, 0, 100, 100)

        val result = SeverityAnalyzer.analyzeSeverity(bitmap)

        assertEquals(10000, result.totalLeafPixels)
        assertEquals(0, result.lesionPixels)
        assertEquals(0.0f, result.severityPercentage, 0.01f)
        assertEquals(100.0f, result.healthScore, 0.01f)
        assertEquals(InfectionGrade.GRADE_1, result.grade)
    }

    @Test
    fun testAnalyzeSeverity_halfLesionLeaf_returnsFiftyPercentSeverityAndGrade4() {
        // Create 100x100 bitmap: 50% healthy green, 50% lesion brown
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(100 * 100)
        val healthyGreen = Color.rgb(30, 180, 40)
        val lesionBrown = Color.rgb(180, 100, 20) // Hue ~25, Sat ~0.88, Val ~0.71

        for (i in 0 until 5000) {
            pixels[i] = healthyGreen
        }
        for (i in 5000 until 10000) {
            pixels[i] = lesionBrown
        }
        bitmap.setPixels(pixels, 0, 100, 0, 0, 100, 100)

        val result = SeverityAnalyzer.analyzeSeverity(bitmap)

        assertEquals(10000, result.totalLeafPixels)
        assertEquals(5000, result.lesionPixels)
        assertEquals(50.0f, result.severityPercentage, 0.1f)
        assertEquals(50.0f, result.healthScore, 0.1f)
        assertEquals(InfectionGrade.GRADE_4, result.grade)
    }

    @Test
    fun testAnalyzeSeverity_moderateLesionLeaf_returnsGrade2() {
        // 10% lesion pixels (1000 px), 90% healthy green (9000 px)
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(100 * 100)
        val healthyGreen = Color.rgb(30, 180, 40)
        val lesionBrown = Color.rgb(180, 100, 20)

        for (i in 0 until 9000) {
            pixels[i] = healthyGreen
        }
        for (i in 9000 until 10000) {
            pixels[i] = lesionBrown
        }
        bitmap.setPixels(pixels, 0, 100, 0, 0, 100, 100)

        val result = SeverityAnalyzer.analyzeSeverity(bitmap)

        assertEquals(10.0f, result.severityPercentage, 0.1f)
        assertEquals(InfectionGrade.GRADE_2, result.grade)
        assertEquals(90.0f, result.healthScore, 0.1f)
    }

    @Test
    fun testAnalyzeSeverity_severeLesionLeaf_returnsGrade3() {
        // 25% lesion pixels (2500 px), 75% healthy green (7500 px)
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(100 * 100)
        val healthyGreen = Color.rgb(30, 180, 40)
        val lesionBrown = Color.rgb(180, 100, 20)

        for (i in 0 until 7500) {
            pixels[i] = healthyGreen
        }
        for (i in 7500 until 10000) {
            pixels[i] = lesionBrown
        }
        bitmap.setPixels(pixels, 0, 100, 0, 0, 100, 100)

        val result = SeverityAnalyzer.analyzeSeverity(bitmap)

        assertEquals(25.0f, result.severityPercentage, 0.1f)
        assertEquals(InfectionGrade.GRADE_3, result.grade)
    }

    @Test
    fun testAnalyzeSeverity_nonLeafImage_returnsZeroTotalLeafPixels() {
        // Pure blue background bitmap (no plant hue)
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val blueBackground = Color.rgb(10, 10, 220)
        val pixels = IntArray(100 * 100) { blueBackground }
        bitmap.setPixels(pixels, 0, 100, 0, 0, 100, 100)

        val result = SeverityAnalyzer.analyzeSeverity(bitmap)

        assertEquals(0, result.totalLeafPixels)
        assertEquals(0, result.lesionPixels)
        assertEquals(0.0f, result.severityPercentage, 0.01f)
        assertTrue(result.summary.contains("No leaf foliage detected"))
    }
}
