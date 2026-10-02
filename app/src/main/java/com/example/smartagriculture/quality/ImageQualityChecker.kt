package com.example.smartagriculture.quality

import android.graphics.Bitmap

/**
 * Pre-Inference Quality Checker for Crop Disease Analysis Pipeline.
 * Delegates to [QualityGate] for comprehensive OpenCV focus, exposure, and leaf/plant colorimetry validation.
 */
object ImageQualityChecker {

    /**
     * Validates input leaf image bitmap before running TFLite inference.
     *
     * Performs computer vision and colorimetry checks:
     * 1. Blur Detection via Laplacian Kernel Variance.
     * 2. Exposure & Glare Detection via Average Pixel Luminance & Clipping Ratio.
     * 3. Plant Foliage & Non-Leaf (Keyboard/Screen/Human) Subject Detection via HSV Color Space Analysis.
     *
     * @param bitmap Raw image captured from camera or gallery.
     * @return [QualityResult] indicating whether image passes pre-analysis quality gate.
     */
    fun validateImage(bitmap: Bitmap): QualityResult {
        return QualityGate.validateImage(bitmap)
    }
}
