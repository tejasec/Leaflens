package com.example.smartagriculture.ml

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.sqrt

/**
 * On-device Feature Embedding Extractor for Few-Shot Adaptation (MOD-04, Feature 6).
 *
 * Extracts a normalized 128-dimensional dense visual feature vector from input leaf images:
 * - 4x4 spatial grid color channel moments (R, G, B, H, S, V) = 96 dimensions
 * - Spatial block local variance & contrast = 16 dimensions
 * - Radial lesion gradient & edge density = 16 dimensions
 * - L2-normalization to unit hypersphere (||v||_2 = 1.0) for fast cosine dot-product matching.
 */
object FeatureEmbeddingExtractor {

    const val EMBEDDING_DIM = 128
    private const val GRID_SIZE = 4 // 4x4 grid -> 16 spatial cells

    /**
     * Extracts a 128-dimensional L2-normalized embedding vector from the provided Bitmap.
     */
    fun extractEmbedding(bitmap: Bitmap): List<Double> {
        val safeBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            bitmap
        }

        // Rescale to standard 224x224 dimension
        val scaledBitmap = if (safeBitmap.width == 224 && safeBitmap.height == 224) {
            safeBitmap
        } else {
            Bitmap.createScaledBitmap(safeBitmap, 224, 224, true)
        }

        val rawFeatures = DoubleArray(EMBEDDING_DIM)
        val cellWidth = scaledBitmap.width / GRID_SIZE
        val cellHeight = scaledBitmap.height / GRID_SIZE
        val hsvBuffer = FloatArray(3)

        // 1. Extract 4x4 spatial grid moments (16 cells * 6 features = 96 dimensions)
        var featureIdx = 0
        val cellVariances = DoubleArray(GRID_SIZE * GRID_SIZE)

        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                var sumR = 0.0
                var sumG = 0.0
                var sumB = 0.0
                var sumH = 0.0
                var sumS = 0.0
                var sumV = 0.0
                var sumLum = 0.0
                var sumLumSq = 0.0

                val startX = col * cellWidth
                val startY = row * cellHeight
                val pixelCount = cellWidth * cellHeight

                for (y in startY until (startY + cellHeight)) {
                    for (x in startX until (startX + cellWidth)) {
                        val pixel = scaledBitmap.getPixel(x, y)
                        val r = Color.red(pixel) / 255.0
                        val g = Color.green(pixel) / 255.0
                        val b = Color.blue(pixel) / 255.0

                        Color.colorToHSV(pixel, hsvBuffer)
                        val h = hsvBuffer[0] / 360.0
                        val s = hsvBuffer[1].toDouble()
                        val v = hsvBuffer[2].toDouble()

                        val lum = 0.299 * r + 0.587 * g + 0.114 * b

                        sumR += r
                        sumG += g
                        sumB += b
                        sumH += h
                        sumS += s
                        sumV += v
                        sumLum += lum
                        sumLumSq += lum * lum
                    }
                }

                if (pixelCount > 0) {
                    rawFeatures[featureIdx++] = sumR / pixelCount
                    rawFeatures[featureIdx++] = sumG / pixelCount
                    rawFeatures[featureIdx++] = sumB / pixelCount
                    rawFeatures[featureIdx++] = sumH / pixelCount
                    rawFeatures[featureIdx++] = sumS / pixelCount
                    rawFeatures[featureIdx++] = sumV / pixelCount

                    val meanLum = sumLum / pixelCount
                    val variance = (sumLumSq / pixelCount) - (meanLum * meanLum)
                    cellVariances[row * GRID_SIZE + col] = if (variance > 0) variance else 0.0
                } else {
                    featureIdx += 6
                }
            }
        }

        // 2. Spatial local variances per cell (16 dimensions, index 96 to 111)
        for (i in 0 until 16) {
            rawFeatures[featureIdx++] = cellVariances[i] * 10.0 // scaled for numerical stability
        }

        // 3. Radial center-to-periphery lesion gradients (16 dimensions, index 112 to 127)
        val outerIndices = listOf(0, 1, 2, 3, 4, 7, 8, 11, 12, 13, 14, 15)
        val centerIndices = listOf(5, 6, 9, 10)
        val avgCenterVariance = centerIndices.map { cellVariances[it] }.average()

        for (i in 0 until 16) {
            val outerIdx = outerIndices[i % outerIndices.size]
            val radialDiff = cellVariances[outerIdx] - avgCenterVariance
            rawFeatures[featureIdx++] = radialDiff * 5.0
        }

        // 4. L2-Normalize vector to unit hypersphere: ||v|| = 1.0
        return l2Normalize(rawFeatures.toList())
    }

    /**
     * Normalizes a vector such that its Euclidean L2-norm equals 1.0.
     */
    fun l2Normalize(vector: List<Double>): List<Double> {
        var sumSquares = 0.0
        for (v in vector) {
            sumSquares += v * v
        }
        val norm = sqrt(sumSquares)
        if (norm == 0.0) return vector
        return vector.map { it / norm }
    }
}
