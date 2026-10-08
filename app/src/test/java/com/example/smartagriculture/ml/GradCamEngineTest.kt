package com.example.smartagriculture.ml

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class GradCamEngineTest {

    @Test
    fun testGenerateActivationMatrix_healthyLeaf_returnsLowActivations() {
        val bitmap = Bitmap.createBitmap(140, 140, Bitmap.Config.ARGB_8888)
        val healthyGreen = Color.rgb(30, 180, 40)
        val pixels = IntArray(140 * 140) { healthyGreen }
        bitmap.setPixels(pixels, 0, 140, 0, 0, 140, 140)

        val matrix = GradCamEngine.generateActivationMatrix(bitmap)

        assertEquals(7, matrix.size)
        assertEquals(7, matrix[0].size)

        // For a completely healthy leaf, all activation values should be near zero / subtle baseline (< 0.15)
        for (r in 0 until 7) {
            for (c in 0 until 7) {
                assertTrue("Expected subtle/low activation on healthy leaf at ($r, $c)", matrix[r][c] <= 0.15f)
            }
        }
    }

    @Test
    fun testGenerateActivationMatrix_localizedLesion_highlightsLesionRegion() {
        val bitmap = Bitmap.createBitmap(140, 140, Bitmap.Config.ARGB_8888)
        val healthyGreen = Color.rgb(30, 180, 40)
        val lesionBrown = Color.rgb(180, 100, 20) // Necrotic brown: R > B, G > B*0.8, Hue ~25

        // Fill entire leaf with healthy green
        val pixels = IntArray(140 * 140) { healthyGreen }

        // Place a dense lesion patch in bottom-right corner (rows 100..139, cols 100..139) -> grid cells (5..6, 5..6)
        for (y in 100 until 140) {
            for (x in 100 until 140) {
                pixels[y * 140 + x] = lesionBrown
            }
        }
        bitmap.setPixels(pixels, 0, 140, 0, 0, 140, 140)

        val matrix = GradCamEngine.generateActivationMatrix(bitmap)

        // Grid (5, 5) or (6, 6) must have high activation (> 0.70)
        val lesionCellActivation = maxOf(matrix[5][5], matrix[6][6])
        val oppositeCellActivation = matrix[0][0]

        assertTrue("Lesion hotspot should have high activation", lesionCellActivation >= 0.70f)
        assertTrue("Healthy region should have lower activation than lesion spot", oppositeCellActivation < lesionCellActivation)
    }

    @Test
    fun testCreateBlendedHeatmapBitmap_returnsValidBitmap() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val matrix = Array(7) { FloatArray(7) { 0.5f } }

        val blended = GradCamEngine.createBlendedHeatmapBitmap(bitmap, matrix, 0.7f)

        assertNotNull(blended)
        assertEquals(100, blended.width)
        assertEquals(100, blended.height)
    }
}
