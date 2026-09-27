package com.example.smartagriculture.analysis

import android.graphics.Bitmap
import android.graphics.Color
import java.util.Locale

/**
 * Infection Severity Grade classification based on affected leaf area.
 */
enum class InfectionGrade(val gradeNumber: Int, val description: String) {
    GRADE_1(1, "Grade 1 (<5% affected) - Minimal infection"),
    GRADE_2(2, "Grade 2 (5–15% affected) - Moderate infection"),
    GRADE_3(3, "Grade 3 (15–35% affected) - Severe infection"),
    GRADE_4(4, "Grade 4 (>35% affected) - Critical / Widespread infection"),
}

/**
 * Output model containing lesion severity analytics and health score.
 */
data class SeverityResult(
    val severityPercentage: Float,
    val healthScore: Float,
    val grade: InfectionGrade,
    val totalLeafPixels: Int,
    val lesionPixels: Int,
    val summary: String,
)

/**
 * Image processing service that isolates leaf foliage, segments chlorotic/necrotic lesions,
 * and computes crop health index & infection grade (Feature 1).
 */
object SeverityAnalyzer {

    /**
     * Analyzes an input leaf Bitmap image frame:
     * 1. Isolates leaf contour from background using green/chlorosis HSV bounds & Otsu-style thresholding.
     * 2. Segments chlorotic and necrotic spot pixels (brown, yellow, rust hues).
     * 3. Computes severity percentage = (lesionPixels / totalLeafPixels) * 100.
     * 4. Classifies into InfectionGrade (Grade 1 to Grade 4).
     * 5. Computes health score = (100 - severity).coerceIn(0.0f, 100.0f).
     */
    fun analyzeSeverity(bitmap: Bitmap): SeverityResult {
        val safeBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            bitmap
        }

        val width = safeBitmap.width
        val height = safeBitmap.height
        val totalPixels = width * height

        if (totalPixels == 0) {
            return SeverityResult(
                severityPercentage = 0f,
                healthScore = 100f,
                grade = InfectionGrade.GRADE_1,
                totalLeafPixels = 0,
                lesionPixels = 0,
                summary = "Empty image frame.",
            )
        }

        val pixels = IntArray(totalPixels)
        safeBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        var totalLeafPixels = 0
        var lesionPixels = 0

        val hsv = FloatArray(3)

        for (i in 0 until totalPixels) {
            val pixel = pixels[i]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            // Convert RGB pixel to HSV color space
            Color.RGBToHSV(r, g, b, hsv)
            val hue = hsv[0]        // 0..360
            val saturation = hsv[1] // 0..1
            val value = hsv[2]      // 0..1

            // 1. Isolate leaf pixels from background:
            // Leaf comprises healthy green foliage (Hue 35-165) + chlorotic/necrotic lesions (Hue 0-35 or 340-360)
            val isHealthyFoliage = ((hue in 35.0f..165.0f) && (saturation >= 0.12f)) && (value >= 0.12f)
            val isLesionColor = (((hue in 0.0f..35.0f) || (hue in 340.0f..360.0f)) && (saturation >= 0.15f)) && (value in 0.10f..0.85f)

            val isLeafPixel = isHealthyFoliage || isLesionColor

            if (isLeafPixel) {
                totalLeafPixels++

                // 2. Segment chlorotic/necrotic spot pixels (brown, yellow, rust hues)
                if (isLesionColor) {
                    // Additional check for yellow chlorosis / brown necrosis
                    // Chlorotic yellow: R and G high, B low; Necrotic brown: R > B, V muted
                    val isChlorosisOrNecrosis = (r > b) && (g > b * 0.8f)
                    if (isChlorosisOrNecrosis) {
                        lesionPixels++
                    }
                }
            }
        }

        if (totalLeafPixels == 0) {
            return SeverityResult(
                severityPercentage = 0f,
                healthScore = 100f,
                grade = InfectionGrade.GRADE_1,
                totalLeafPixels = 0,
                lesionPixels = 0,
                summary = "No leaf foliage detected in image frame.",
            )
        }

        // 3. Compute severity percentage = (lesionPixels / totalLeafPixels) * 100
        val severityPercentage = (lesionPixels.toFloat() / totalLeafPixels.toFloat()) * 100.0f

        // 4. Classify severity into Grade 1 (<5%), Grade 2 (5-15%), Grade 3 (15-35%), Grade 4 (>35%)
        val grade = when {
            severityPercentage < 5.0f -> InfectionGrade.GRADE_1
            severityPercentage < 15.0f -> InfectionGrade.GRADE_2
            severityPercentage < 35.0f -> InfectionGrade.GRADE_3
            else -> InfectionGrade.GRADE_4
        }

        // 5. Output health score out of 100: healthScore = (100 - severity).coerceIn(0f, 100f)
        val healthScore = (100.0f - severityPercentage).coerceIn(0.0f, 100.0f)

        val summary = String.format(
            Locale.US,
            "Health Score: %.1f/100 | Lesion Severity: %.1f%% | %s",
            healthScore,
            severityPercentage,
            grade.description,
        )

        return SeverityResult(
            severityPercentage = severityPercentage,
            healthScore = healthScore,
            grade = grade,
            totalLeafPixels = totalLeafPixels,
            lesionPixels = lesionPixels,
            summary = summary,
        )
    }
}
