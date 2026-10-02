package com.example.smartagriculture.quality

import android.graphics.Bitmap
import kotlin.math.abs

/**
 * Standard simple quality result model.
 */
data class QualityResult(
    val isValid: Boolean,
    val feedbackMessage: String,
    val isBlurry: Boolean = false,
    val isExtremeLighting: Boolean = false,
    val laplacianVariance: Double = 0.0,
    val averageLuminance: Double = 0.0
)

/**
 * Detailed pre-analysis validation model providing itemized Quality & Scanner Suitability metrics.
 */
data class DetailedQualityResult(
    val isValid: Boolean,
    val resolutionStatus: String,
    val lightingStatus: String,
    val sharpnessStatus: String,
    val leafVisibilityStatus: String,
    val leafDetected: Boolean,
    val foliageCoveragePct: Int,
    val minRequiredPct: Int = 20,
    val overallStatus: String,
    val feedbackMessage: String,
)

object QualityGate {

    fun validateImage(bitmap: Bitmap): QualityResult {
        val detailed = evaluateDetailedQuality(bitmap)
        return QualityResult(
            isValid = detailed.isValid,
            feedbackMessage = detailed.feedbackMessage,
        )
    }

    /**
     * Evaluates full itemized Quality AND Leaf/Scanner Suitability before deep learning inference:
     * A) Image Quality: Resolution, Lighting, Sharpness, Leaf Visibility
     * B) Scanner Check: Leaf Detected, Foliage Coverage (20% minimum threshold)
     */
    fun evaluateDetailedQuality(bitmap: Bitmap): DetailedQualityResult {
        val safeBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            bitmap
        }

        val width = safeBitmap.width
        val height = safeBitmap.height
        val totalPixels = width * height

        if (totalPixels == 0) {
            return DetailedQualityResult(
                isValid = false,
                resolutionStatus = "Invalid (0x0)",
                lightingStatus = "Error",
                sharpnessStatus = "Error",
                leafVisibilityStatus = "None",
                leafDetected = false,
                foliageCoveragePct = 0,
                overallStatus = "This image does not appear suitable for leaf analysis.",
                feedbackMessage = "Invalid image frame: Image dimensions are zero.",
            )
        }

        val pixels = IntArray(totalPixels)
        safeBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

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

            val y = (0.299f * r) + (0.587f * g) + (0.114f * b)
            gray[i] = y
            luminanceSum += y.toDouble()

            if (y > 250f) {
                overexposedCount++
            }

            val isGreenFoliage = (g > r) && (g > b) && (g > 25)
            if (isGreenFoliage) {
                greenPixelCount++
                leafMatterPixelCount++
            }

            val maxC = maxOf(r, maxOf(g, b))
            val minC = minOf(r, minOf(g, b))
            val isSkinTone = (r > 95) && (g > 40) && (b > 20) && ((maxC - minC) > 15) && (abs(r - g) > 15) && (r > g) && (r > b)
            if (isSkinTone) {
                skinPixelCount++
            } else if ((r > b) && (g > (b * 0.8f)) && (y in 30f..220f)) {
                leafMatterPixelCount++
            }
        }

        // 1. Resolution Check
        val resolutionOk = (width >= 400 && height >= 400)
        val resolutionStatus = if (resolutionOk) "Good ✓ (${width}x${height})" else "Low Resolution ⚠"

        // 2. Sharpness / Blur Check (Variance of Laplacian >= 100.0)
        val laplacianVar = computeLaplacianVariance(gray, width, height)
        val sharpnessOk = laplacianVar >= 100.0
        val sharpnessStatus = if (sharpnessOk) "Good ✓" else "Blurry ⚠"

        // 3. Lighting Gating Check
        val meanLuminance = luminanceSum / totalPixels
        val overexposedRatio = overexposedCount.toDouble() / totalPixels
        val lightingOk = (meanLuminance >= 40.0) && (overexposedRatio <= 0.15)
        val lightingStatus = when {
            meanLuminance < 40.0 -> "Underexposed ⚠"
            overexposedRatio > 0.15 -> "Overexposed ⚠"
            else -> "Good ✓"
        }

        // 4. Leaf / Scanner Suitability Check
        val skinRatio = skinPixelCount.toDouble() / totalPixels
        val leafRatio = leafMatterPixelCount.toDouble() / totalPixels
        val foliageCoveragePct = (leafRatio * 100).toInt().coerceIn(0, 100)

        val leafDetected = (skinRatio <= 0.25) && (foliageCoveragePct >= 18)
        val leafVisibilityStatus = if (leafDetected) "Good ✓" else "Low ⚠"

        val foliageThresholdOk = foliageCoveragePct >= 20

        val isValid = resolutionOk && sharpnessOk && lightingOk && leafDetected && foliageThresholdOk

        val overallStatus: String
        val feedbackMessage: String

        if (isValid) {
            overallStatus = "Ready for Leaf Analysis"
            feedbackMessage = "Image quality & scanner suitability optimal."
        } else if (!leafDetected || !foliageThresholdOk) {
            overallStatus = "This image does not appear suitable for leaf analysis."
            feedbackMessage = if (skinRatio > 0.25) {
                "Non-leaf object detected: Human face or hand detected. Please capture a crop leaf."
            } else {
                "Insufficient foliage detected: Foliage coverage is ${foliageCoveragePct}% (Minimum required: 20%)."
            }
        } else {
            overallStatus = "Image Needs Improvement"
            feedbackMessage = if (!sharpnessOk) {
                "Hold steady: Image is blurry."
            } else {
                "Lighting issue: Adjust position to avoid extreme darkness or glare."
            }
        }

        return DetailedQualityResult(
            isValid = isValid,
            resolutionStatus = resolutionStatus,
            lightingStatus = lightingStatus,
            sharpnessStatus = sharpnessStatus,
            leafVisibilityStatus = leafVisibilityStatus,
            leafDetected = leafDetected,
            foliageCoveragePct = foliageCoveragePct,
            minRequiredPct = 20,
            overallStatus = overallStatus,
            feedbackMessage = feedbackMessage,
        )
    }

    private fun computeLaplacianVariance(gray: FloatArray, width: Int, height: Int): Double {
        if (width < 3 || height < 3) return 0.0

        var sum = 0.0
        var sumSq = 0.0
        var count = 0

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
