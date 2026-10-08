package com.example.smartagriculture.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import org.robolectric.RuntimeEnvironment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class GradCamViewTest {

    @Test
    fun testSetAlphaOpacity_clampsValuesCorrectly() {
        val context = RuntimeEnvironment.getApplication()
        val gradCamView = GradCamView(context)

        gradCamView.setAlphaOpacity(0.45f)
        assertEquals(0.45f, gradCamView.getAlphaOpacity(), 0.001f)

        gradCamView.setAlphaOpacity(-0.5f)
        assertEquals(0.0f, gradCamView.getAlphaOpacity(), 0.001f)

        gradCamView.setAlphaOpacity(1.5f)
        assertEquals(1.0f, gradCamView.getAlphaOpacity(), 0.001f)
    }

    @Test
    fun testGenerateJetHeatmapBitmap_producesExpectedDimensions() {
        val matrix = Array(7) { FloatArray(7) { (it * 0.1f) } }
        val heatmap = GradCamView.generateJetHeatmapBitmap(matrix)

        assertNotNull(heatmap)
        assertEquals(224, heatmap.width)
        assertEquals(224, heatmap.height)
    }

    @Test
    fun testValueToJetColor_alphaFalloff() {
        // Low activation (< 0.12) must be transparent to avoid blue blanket effect over healthy leaf
        val lowColor = GradCamView.valueToJetColor(0.05f)
        assertEquals(0, Color.alpha(lowColor))

        // High activation (1.0) must have full opacity and vivid red
        val highColor = GradCamView.valueToJetColor(1.0f)
        assertEquals(255, Color.alpha(highColor))
        assertEquals(255, Color.red(highColor))
        assertTrue("Green should be low in pure red peak", Color.green(highColor) < 50)
        assertTrue("Blue should be low in pure red peak", Color.blue(highColor) < 50)

        // Moderate activation (0.50) must have partial opacity
        val midColor = GradCamView.valueToJetColor(0.50f)
        assertTrue("Mid color should have non-zero alpha", Color.alpha(midColor) > 100)
    }

    @Test
    fun testCreateBlendedBitmap_runsSuccessfully() {
        val source = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888)
        val matrix = Array(7) { FloatArray(7) { 0.8f } }

        val blended = GradCamView.createBlendedBitmap(source, matrix, 0.70f)
        assertNotNull(blended)
        assertEquals(120, blended.width)
        assertEquals(120, blended.height)
    }
}
