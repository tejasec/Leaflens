package com.example.smartagriculture.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * Custom View that renders Grad-CAM saliency maps over a leaf photo (Feature 8).
 * Supports bilinear upscaling of 7x7 or 14x14 activation maps, Jet colormap rendering,
 * and real-time opacity blending slider integration.
 */
class GradCamView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private var leafBitmap: Bitmap? = null
    private var heatmapBitmap: Bitmap? = null

    private var opacityAlpha: Float = 0.7f // Default 70% opacity
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * Updates the opacity alpha blending factor (0.0 to 1.0) dynamically from slider.
     */
    fun setAlphaOpacity(alpha: Float) {
        this.opacityAlpha = alpha.coerceIn(0.0f, 1.0f)
        invalidate()
    }

    /**
     * Sets the leaf background photo and raw Grad-CAM 2D activation matrix (7x7 or 14x14).
     */
    fun setGradCamData(photo: Bitmap?, activationMatrix: Array<FloatArray>?) {
        this.leafBitmap = photo
        if (!activationMatrix.isNullOrEmpty() && activationMatrix[0].isNotEmpty()) {
            this.heatmapBitmap = generateJetHeatmapBitmap(activationMatrix)
        } else {
            this.heatmapBitmap = null
        }
        invalidate()
    }

    /**
     * Normalizes 2D activation matrix between 0.0 and 1.0, applies Jet colormap,
     * and performs bilinear upscaling to target dimensions.
     */
    private fun generateJetHeatmapBitmap(matrix: Array<FloatArray>): Bitmap {
        val srcRows = matrix.size
        val srcCols = matrix[0].size

        // 1. Find min and max for normalization
        var minVal = Float.MAX_VALUE
        var maxVal = -Float.MAX_VALUE
        for (r in 0 until srcRows) {
            for (c in 0 until srcCols) {
                val v = matrix[r][c]
                if (v < minVal) minVal = v
                if (v > maxVal) maxVal = v
            }
        }

        val range = if (maxVal - minVal > 1e-6f) maxVal - minVal else 1f

        // Upscale matrix to high-resolution heatmap bitmap (e.g., 224x224 or 512x512)
        val targetWidth = 224
        val targetHeight = 224
        val pixels = IntArray(targetWidth * targetHeight)

        for (y in 0 until targetHeight) {
            val srcY = (y.toFloat() / (targetHeight - 1)) * (srcRows - 1)
            val y0 = srcY.toInt().coerceIn(0, srcRows - 1)
            val y1 = (y0 + 1).coerceIn(0, srcRows - 1)
            val yLerp = srcY - y0

            for (x in 0 until targetWidth) {
                val srcX = (x.toFloat() / (targetWidth - 1)) * (srcCols - 1)
                val x0 = srcX.toInt().coerceIn(0, srcCols - 1)
                val x1 = (x0 + 1).coerceIn(0, srcCols - 1)
                val xLerp = srcX - x0

                // Bilinear interpolation
                val v00 = (matrix[y0][x0] - minVal) / range
                val v01 = (matrix[y0][x1] - minVal) / range
                val v10 = (matrix[y1][x0] - minVal) / range
                val v11 = (matrix[y1][x1] - minVal) / range

                val top = v00 + xLerp * (v01 - v00)
                val bottom = v10 + xLerp * (v11 - v10)
                val normalizedValue = top + yLerp * (bottom - top)

                // 2. Apply Jet colormap mapping
                pixels[y * targetWidth + x] = valueToJetColor(normalizedValue)
            }
        }

        return Bitmap.createBitmap(pixels, targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
    }

    /**
     * Converts a normalized activation value (0.0 .. 1.0) to Jet Colormap ARGB.
     * Low = Blue, Mid = Cyan/Green/Yellow, High = Red.
     */
    private fun valueToJetColor(value: Float): Int {
        val v = value.coerceIn(0.0f, 1.0f)
        val fourV = 4.0f * v

        val r = (minOf(fourV - 1.5f, -fourV + 4.5f)).coerceIn(0.0f, 1.0f)
        val g = (minOf(fourV - 0.5f, -fourV + 3.5f)).coerceIn(0.0f, 1.0f)
        val b = (minOf(fourV + 0.5f, -fourV + 2.5f)).coerceIn(0.0f, 1.0f)

        return Color.argb(
            255,
            (r * 255f).toInt(),
            (g * 255f).toInt(),
            (b * 255f).toInt(),
        )
    }

    private val drawRect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        if (viewWidth <= 0 || viewHeight <= 0) return

        drawRect.set(0f, 0f, viewWidth, viewHeight)

        // 1. Draw original leaf photo
        leafBitmap?.let { bmp ->
            paint.alpha = 255
            canvas.drawBitmap(
                bmp,
                null,
                drawRect,
                paint,
            )
        }

        // 3. Blend Grad-CAM heatmap canvas overlay using interactive opacity alpha slider
        heatmapBitmap?.let { heatmap ->
            paint.alpha = (opacityAlpha * 255f).toInt().coerceIn(0, 255)
            canvas.drawBitmap(
                heatmap,
                null,
                drawRect,
                paint,
            )
        }
    }
}
