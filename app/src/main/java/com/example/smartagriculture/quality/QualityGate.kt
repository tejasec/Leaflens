package com.example.smartagriculture.quality

import android.graphics.Bitmap
import kotlin.math.abs

/**
 * Pre-inference image validator for crop health analysis (MOD-01, Feature 9).
 * Runs sequential quality gating before running deep learning inference.
 */
data class QualityResult(
    val isValid: Boolean,
    val feedbackMessage: String,
)

object QualityGate {

    /**
     * Validates an input bitmap image frame sequentially:
     * 1. Motion Blur Check (Variance of Laplacian >= 100.0)
     * 2. Luminance Gating (Mean luminance >= 40 and overexposure <= 15%)
     * 3. Foliage Presence (Green spectrum pixels >= 20%)
     */
    fun validateImage(bitmap: Bitmap): QualityResult {
        val safeBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            bitmap
        }

        val width = safeBitmap.width
        val height = safeBitmap.height
        val totalPixels = width * height

        if (totalPixels == 0) {
            return QualityResult(isValid = false, feedbackMessage = "Invalid image frame: Image dimensions are zero.")
        }

        // Extract ARGB pixels array for high-performance memory traversal
        val pixels = IntArray(totalPixels)
        safeBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Pre-calculate grayscale luminance and color stats
        val gray = FloatArray(totalPixels)
        var luminanceSum = 0.0
        var overexposedCount = 0
        var greenPixelCount = 0
        var skinPixelCount = 0
        var leafMatterPixelCount = 0

        for (i in 0 until totalPixels) {
            val pixel = pixels[i]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            // Grayscale (ITU-R BT.601 standard luminance)
            val y = (0.299f * r) + (0.587f * g) + (0.114f * b)
            gray[i] = y
            luminanceSum += y.toDouble()

            // Luminance Overexposure Check
            if (y > 250f) {
                overexposedCount++
            }

            // 1. Foliage Check: Green spectrum dominant in RGB
            val isGreenFoliage = (g > r) && (g > b) && (g > 25)
            if (isGreenFoliage) {
                greenPixelCount++
                leafMatterPixelCount++
            }

            // 2. Human Skin Tone Detection (Face, Hand, Skin)
            val maxC = maxOf(r, maxOf(g, b))
            val minC = minOf(r, minOf(g, b))
            val isSkinTone = (r > 95) && (g > 40) && (b > 20) && ((maxC - minC) > 15) && (abs(r - g) > 15) && (r > g) && (r > b)
            if (isSkinTone) {
                skinPixelCount++
            } else if ((r > b) && (g > (b * 0.8f)) && (y in 30f..220f)) {
                // Lesion / Diseased foliage tissue
                leafMatterPixelCount++
            }
        }

        // --- Non-Leaf Check A: Human Face / Skin / Hand Detection ---
        val skinRatio = skinPixelCount.toDouble() / totalPixels
        if (skinRatio > 0.25) {
            return QualityResult(
                isValid = false,
                feedbackMessage = "Non-leaf object detected: Human face or hand detected. Please capture or select a clear image of a crop leaf."
            )
        }

        // --- Non-Leaf Check B: Crop Foliage / Leaf Matter Ratio ---
        val leafRatio = leafMatterPixelCount.toDouble() / totalPixels
        if (leafRatio < 0.18) {
            return QualityResult(
                isValid = false,
                feedbackMessage = "Non-leaf object detected: No crop foliage found in image. Please align a plant leaf within the camera frame."
            )
        }

        // --- Check 1: Motion Blur Check (Variance of Laplacian) ---
        val laplacianVar = computeLaplacianVariance(gray, width, height)
        if (laplacianVar < 100.0) {
            return QualityResult(isValid = false, feedbackMessage = "Hold steady: Image is blurry.")
        }

        // --- Check 2: Luminance Gating ---
        val meanLuminance = luminanceSum / totalPixels
        if (meanLuminance < 40.0) {
            return QualityResult(isValid = false, feedbackMessage = "Image is underexposed. Please ensure sufficient lighting.")
        }

        val overexposedRatio = overexposedCount.toDouble() / totalPixels
        if (overexposedRatio > 0.15) {
            return QualityResult(isValid = false, feedbackMessage = "Image is overexposed. Avoid direct glare or intense reflections.")
        }

        // --- Check 3: Foliage Presence ---
        val greenRatio = greenPixelCount.toDouble() / totalPixels
        if (greenRatio < 0.20) {
            return QualityResult(isValid = false, feedbackMessage = "Foliage presence insufficient: Green crop pixels must constitute at least 20% of image frame.")
        }

        return QualityResult(isValid = true, feedbackMessage = "Image quality optimal for analysis.")
    }

    /**
     * Computes the variance of the 3x3 Laplacian operator over the grayscale image.
     * Kernel: [[0, 1, 0], [1, -4, 1], [0, 1, 0]]
     */
    private fun computeLaplacianVariance(gray: FloatArray, width: Int, height: Int): Double {
        if (width < 3 || height < 3) return 0.0

        var sum = 0.0
        var sumSq = 0.0
        var count = 0

        // Iterate over inner pixels ignoring 1-pixel border
        for (y in 1 until height - 1) {
            val rowOffset = y * width
            val topOffset = (y - 1) * width
            val bottomOffset = (y + 1) * width

            for (x in 1 until width - 1) {
                val center = gray[rowOffset + x]
                val top = gray[topOffset + x]
                val bottom = gray[bottomOffset + x]
                val left = gray[rowOffset + (x - 1)]
                val right = gray[rowOffset + (x + 1)]

                val laplacian = (top + bottom + left + right - 4f * center).toDouble()
                sum += laplacian
                sumSq += laplacian * laplacian
                count++
            }
        }

        if (count == 0) return 0.0

        val mean = sum / count
        return (sumSq / count) - (mean * mean)
    }
}
