package com.example.smartagriculture.quality

import android.graphics.Bitmap
import java.util.Locale

/**
 * Pre-Inference Quality Checker for Crop Disease Analysis Pipeline.
 *
 * Replicates OpenCV `cv2.Laplacian().var()` focus detection and intensity histogram
 * analysis using direct, high-performance pixel buffer algorithms in Kotlin.
 */
object ImageQualityChecker {

    // Threshold constants matching OpenCV / Mobile Computer Vision standards
    private const val LAPLACIAN_BLUR_THRESHOLD = 100.0 // Variance below 100 indicates severe blur
    private const val MIN_LUMINANCE_THRESHOLD = 40.0   // Luminance below 40 indicates severe underexposure
    private const val MAX_LUMINANCE_THRESHOLD = 215.0  // Luminance above 215 indicates severe overexposure
    private const val OVEREXPOSURE_RATIO_LIMIT = 0.15  // >15% clipped white pixels indicates extreme glare

    /**
     * Validates input leaf image bitmap before running TFLite inference.
     *
     * Performs two core OpenCV computer vision checks:
     * 1. Blur Detection via Laplacian Kernel Variance.
     * 2. Exposure & Glare Detection via Average Pixel Luminance & Clipping Ratio.
     *
     * @param bitmap Raw image captured from camera or gallery.
     * @return [QualityResult] indicating whether image passes pre-analysis quality gate.
     */
    fun validateImage(bitmap: Bitmap): QualityResult {
        // Step 1: Ensure bitmap is in software-accessible ARGB_8888 memory buffer
        val safeBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            bitmap
        }

        val width = safeBitmap.width
        val height = safeBitmap.height
        val totalPixels = width * height

        if (totalPixels == 0) {
            return QualityResult(
                isValid = false,
                feedbackMessage = "Invalid image frame: Dimensions are zero.",
                isBlurry = true,
                isExtremeLighting = true,
                laplacianVariance = 0.0,
                averageLuminance = 0.0
            )
        }

        // Extract pixel color array
        val pixels = IntArray(totalPixels)
        safeBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Convert to grayscale luminance float matrix Y = 0.299R + 0.587G + 0.114B
        val grayscale = FloatArray(totalPixels)
        var luminanceSum = 0.0
        var overexposedCount = 0

        for (i in 0 until totalPixels) {
            val pixel = pixels[i]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            val y = (0.299f * r) + (0.587f * g) + (0.114f * b)
            grayscale[i] = y
            luminanceSum += y.toDouble()

            if (y >= 250f) {
                overexposedCount++
            }
        }

        val averageLuminance = luminanceSum / totalPixels
        val overexposedRatio = overexposedCount.toDouble() / totalPixels

        // Step 2: Compute OpenCV Laplacian Variance for Blur Check
        val laplacianVariance = computeLaplacianVariance(grayscale, width, height)

        // Step 3: Evaluate Quality Gates
        val isBlurry = laplacianVariance < LAPLACIAN_BLUR_THRESHOLD
        val isUnderexposed = averageLuminance < MIN_LUMINANCE_THRESHOLD
        val isOverexposed = averageLuminance > MAX_LUMINANCE_THRESHOLD || overexposedRatio > OVEREXPOSURE_RATIO_LIMIT
        val isExtremeLighting = isUnderexposed || isOverexposed

        val isValid = !isBlurry && !isExtremeLighting

        // Construct human-readable diagnostic message
        val feedbackMessage = when {
            isBlurry && isExtremeLighting -> "Image is blurry and has extreme lighting. Please hold camera steady in well-lit conditions."
            isBlurry -> String.format(Locale.US, "Image is blurry (Laplacian Score: %.1f < %.1f). Please hold steady and refocus on leaf.", laplacianVariance, LAPLACIAN_BLUR_THRESHOLD)
            isUnderexposed -> String.format(Locale.US, "Image is underexposed (Avg Luminance: %.1f). Move to a brighter area or turn on flash.", averageLuminance)
            isOverexposed -> String.format(Locale.US, "Image is overexposed with high glare (Avg Luminance: %.1f). Reduce direct glare on leaf.", averageLuminance)
            else -> "Image quality optimal for crop disease diagnosis."
        }

        return QualityResult(
            isValid = isValid,
            feedbackMessage = feedbackMessage,
            isBlurry = isBlurry,
            isExtremeLighting = isExtremeLighting,
            laplacianVariance = laplacianVariance,
            averageLuminance = averageLuminance
        )
    }

    /**
     * Calculates OpenCV-equivalent Laplacian Variance using a 3x3 Discrete Laplacian Kernel:
     *
     * K = [  0,  1,  0 ]
     *     [  1, -4,  1 ]
     *     [  0,  1,  0 ]
     *
     * Variance Var = E[(L - mu)^2] measures high-frequency intensity changes (edges).
     * High variance indicates sharp focus; low variance indicates blur.
     */
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

                // Discrete Laplacian operator convolution
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
