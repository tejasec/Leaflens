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
    val isSyntheticOrScreen: Boolean = false,
    val laplacianVariance: Double = 0.0,
    val averageLuminance: Double = 0.0,
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
    val isSyntheticOrScreen: Boolean = false,
    val overallStatus: String,
    val feedbackMessage: String,
)

object QualityGate {

    fun validateImage(bitmap: Bitmap): QualityResult {
        val detailed = evaluateDetailedQuality(bitmap)
        return QualityResult(
            isValid = detailed.isValid,
            feedbackMessage = detailed.feedbackMessage,
            isBlurry = detailed.sharpnessStatus.contains("Blurry"),
            isExtremeLighting = detailed.lightingStatus.contains("⚠"),
            isSyntheticOrScreen = detailed.isSyntheticOrScreen,
        )
    }

    /**
     * Evaluates full itemized Quality AND Leaf/Scanner Suitability before deep learning inference:
     * A) Image Quality: Resolution, Lighting, Sharpness, Leaf Visibility, Synthetic / Screen Check
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
                isSyntheticOrScreen = false,
                overallStatus = "This image does not appear suitable for leaf analysis.",
                feedbackMessage = "Invalid image frame: Image dimensions are zero.",
            )
        }

        val pixels = IntArray(totalPixels)
        safeBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val gray = FloatArray(totalPixels)
        var luminanceSum = 0.0
        var overexposedCount = 0
        var skinPixelCount = 0
        var neutralPixelCount = 0
        var leafMatterPixelCount = 0

        val hsv = FloatArray(3)

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

            // Convert RGB to HSV
            rgbToHsv(r, g, b, hsv)
            val h = hsv[0]
            val s = hsv[1]
            val v = hsv[2]

            val maxCInt = maxOf(r, maxOf(g, b))
            val minCInt = minOf(r, minOf(g, b))
            val delta = maxCInt - minCInt

            // Check for neutral/monochrome surfaces (keyboards, laptops, grey/black/white plastics, metals, paper)
            val isNeutral = (s < 0.15f) || (delta < 18)
            if (isNeutral) {
                neutralPixelCount++
            }

            // Check for human skin tone
            val isSkinTone = (r > 95) && (g > 40) && (b > 20) && (delta > 15) && (abs(r - g) > 15) && (r > g) && (r > b)
            if (isSkinTone) {
                skinPixelCount++
            } else if (!isNeutral && (v >= 0.10f)) {
                // Check if pixel falls into valid plant/leaf chromaticity profiles (Green, Chlorotic Yellow, or Lesion Brown)
                val isGreenPlant = (h in 35.0f..170.0f && s >= 0.15f) ||
                        (g > (r * 1.05f) && g > (b * 1.05f) && g >= 25)

                val isYellowPlant = (h in 20.0f..35.0f && s >= 0.22f && v >= 0.20f) ||
                        (r >= 50 && g >= 50 && r > (b * 1.25f) && g > (b * 1.15f) && abs(r - g) <= 55)

                val isBrownLesionPlant = (h in 10.0f..25.0f && s >= 0.25f && v in 0.12f..0.75f) ||
                        (r > (g * 1.08f) && g > (b * 1.15f) && r in 45..210 && b < 120)

                if (isGreenPlant || isYellowPlant || isBrownLesionPlant) {
                    leafMatterPixelCount++
                }
            }
        }

        // 1. Resolution Check
        val resolutionOk = (width >= 400 && height >= 400)
        val resolutionStatus = if (resolutionOk) "Good ✓ (${width}x$height)" else "Low Resolution ⚠"

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
        val neutralRatio = neutralPixelCount.toDouble() / totalPixels
        val leafRatio = leafMatterPixelCount.toDouble() / totalPixels
        val foliageCoveragePct = (leafRatio * 100).toInt().coerceIn(0, 100)

        // Leaf is detected if foliage coverage >= 20%, human skin <= 25%, and neutral non-plant matter < 75%
        val leafDetected = (skinRatio <= 0.25) && (foliageCoveragePct >= 20) && (neutralRatio < 0.75)
        val leafVisibilityStatus = if (leafDetected) "Good ✓" else "Low ⚠"

        // 5. Synthetic / Screen Photo Pattern Check
        val isSyntheticOrScreen = detectScreenMoireOrSynthetic(pixels)

        val isValid = resolutionOk && sharpnessOk && lightingOk && leafDetected && !isSyntheticOrScreen

        val overallStatus: String
        val feedbackMessage: String

        if (isValid) {
            overallStatus = "Ready for Leaf Analysis"
            feedbackMessage = "Image quality & scanner suitability optimal."
        } else if (isSyntheticOrScreen) {
            overallStatus = "Synthetic / Screen Photo Detected"
            feedbackMessage = "Synthetic or digital screen photo detected. Please capture a real plant leaf."
        } else if (!leafDetected) {
            overallStatus = "This image does not appear suitable for leaf analysis."
            feedbackMessage = if (skinRatio > 0.25) {
                "Non-leaf object detected: Human face or hand detected. Please capture a crop leaf."
            } else if (neutralRatio >= 0.75 || foliageCoveragePct < 20) {
                "Non-leaf subject detected: Keyboard, screen, or non-plant object detected. Please position a crop leaf in front of the camera."
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
            isSyntheticOrScreen = isSyntheticOrScreen,
            overallStatus = overallStatus,
            feedbackMessage = feedbackMessage,
        )
    }

    private fun rgbToHsv(r: Int, g: Int, b: Int, hsv: FloatArray) {
        val rf = r / 255.0f
        val gf = g / 255.0f
        val bf = b / 255.0f

        val maxC = maxOf(rf, maxOf(gf, bf))
        val minC = minOf(rf, minOf(gf, bf))
        val delta = maxC - minC

        val s = if (maxC > 0.0f) delta / maxC else 0.0f

        var h = 0.0f
        if (delta > 0.0001f) {
            h = when (maxC) {
                rf -> (60.0f * ((gf - bf) / delta) + 360.0f) % 360.0f
                gf -> 60.0f * ((bf - rf) / delta) + 120.0f
                else -> 60.0f * ((rf - gf) / delta) + 240.0f
            }
        }

        hsv[0] = h
        hsv[1] = s
        hsv[2] = maxC
    }

    private fun detectScreenMoireOrSynthetic(pixels: IntArray): Boolean {
        var rbRatioVariance = 0.0
        var count = 0
        for (i in pixels.indices step 8) {
            val r = ((pixels[i] shr 16) and 0xFF) + 1
            val b = (pixels[i] and 0xFF) + 1
            val ratio = r.toDouble() / b.toDouble()
            rbRatioVariance += ratio
            count++
        }
        if (count == 0) return false
        val avgRatio = rbRatioVariance / count
        return avgRatio !in 0.25..4.5
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
