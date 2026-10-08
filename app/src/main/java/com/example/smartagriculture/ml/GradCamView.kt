package com.example.smartagriculture.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * Custom View that renders Grad-CAM saliency heatmaps over a leaf photo (Feature 8).
 * Supports bilinear upscaling of 7x7 or 14x14 activation matrices, Jet colormap with
 * transparent background falloff, aspect-ratio preserved center-crop rendering,
 * and continuous real-time opacity blending (0% to 100%).
 */
class GradCamView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private var leafBitmap: Bitmap? = null
    private var heatmapBitmap: Bitmap? = null

    private var opacityAlpha: Float = 0.70f // Default 70% opacity
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        isDither = true
    }

    private val leafMatrix = Matrix()
    private val heatmapMatrixTransform = Matrix()
    private val viewRect = RectF()

    /**
     * Updates the opacity alpha blending factor (0.0 to 1.0) dynamically from slider.
     */
    fun setAlphaOpacity(alpha: Float) {
        val clamped = alpha.coerceIn(0.0f, 1.0f)
        if (this.opacityAlpha != clamped) {
            this.opacityAlpha = clamped
            postInvalidate()
        }
    }

    /**
     * Gets the current opacity factor (0.0 to 1.0).
     */
    fun getAlphaOpacity(): Float = opacityAlpha

    /**
     * Sets the leaf background photo and raw Grad-CAM 2D activation matrix (7x7 or 14x14).
     */
    fun setGradCamData(photo: Bitmap?, activationMatrix: Array<FloatArray>?) {
        this.leafBitmap = ensureSoftwareBitmap(photo)
        if (!activationMatrix.isNullOrEmpty() && activationMatrix[0].isNotEmpty()) {
            this.heatmapBitmap = generateJetHeatmapBitmap(activationMatrix)
        } else {
            this.heatmapBitmap = null
        }
        postInvalidate()
    }

    /**
     * Sets a pre-generated heatmap bitmap directly (e.g. from cloud fallback or cache).
     */
    fun setHeatmapBitmap(photo: Bitmap?, heatmap: Bitmap?) {
        this.leafBitmap = ensureSoftwareBitmap(photo)
        this.heatmapBitmap = ensureSoftwareBitmap(heatmap)
        postInvalidate()
    }

    /**
     * Returns the currently active heatmap bitmap.
     */
    fun getHeatmapBitmap(): Bitmap? = heatmapBitmap

    /**
     * Returns the currently active leaf bitmap.
     */
    fun getLeafBitmap(): Bitmap? = leafBitmap

    private fun ensureSoftwareBitmap(bmp: Bitmap?): Bitmap? {
        if (bmp == null) return null
        return if (bmp.config == Bitmap.Config.HARDWARE) {
            bmp.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            bmp
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        if (viewWidth <= 0 || viewHeight <= 0) return

        viewRect.set(0f, 0f, viewWidth, viewHeight)

        // 1. Draw original leaf photo with center-crop aspect ratio
        leafBitmap?.let { bmp ->
            paint.alpha = 255
            configureCenterCropMatrix(bmp, viewWidth, viewHeight, leafMatrix)
            canvas.drawBitmap(bmp, leafMatrix, paint)
        }

        // 2. Blend Grad-CAM heatmap canvas overlay using interactive opacity alpha slider
        if (opacityAlpha > 0.001f) {
            heatmapBitmap?.let { heatmap ->
                paint.alpha = (opacityAlpha * 255f).toInt().coerceIn(0, 255)
                configureCenterCropMatrix(heatmap, viewWidth, viewHeight, heatmapMatrixTransform)
                canvas.drawBitmap(heatmap, heatmapMatrixTransform, paint)
            }
        }
    }

    private fun configureCenterCropMatrix(bitmap: Bitmap, viewW: Float, viewH: Float, matrix: Matrix) {
        val bw = bitmap.width.toFloat()
        val bh = bitmap.height.toFloat()
        if (bw <= 0 || bh <= 0) return

        val scale = maxOf(viewW / bw, viewH / bh)
        val dx = (viewW - bw * scale) * 0.5f
        val dy = (viewH - bh * scale) * 0.5f

        matrix.setScale(scale, scale)
        matrix.postTranslate(dx, dy)
    }

    companion object {
        /**
         * Normalizes 2D activation matrix, applies Jet colormap with alpha falloff,
         * and performs bilinear upscaling to target dimensions (224x224).
         */
        fun generateJetHeatmapBitmap(matrix: Array<FloatArray>): Bitmap {
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

            // Upscale matrix to target dimensions
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
                    val normalizedValue = (top + yLerp * (bottom - top)).coerceIn(0.0f, 1.0f)

                    // 2. Apply Jet colormap mapping with transparent background falloff
                    pixels[y * targetWidth + x] = valueToJetColor(normalizedValue)
                }
            }

            return Bitmap.createBitmap(pixels, targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        }

        /**
         * Converts a normalized activation value (0.0 .. 1.0) to Jet Colormap ARGB.
         * Low = Transparent Blue/Cyan, Mid = Green/Yellow, High = Vivid Red.
         * Activations below threshold fade to transparent so unaffected leaf tissue shows naturally.
         */
        fun valueToJetColor(value: Float): Int {
            val v = value.coerceIn(0.0f, 1.0f)

            // Transparent falloff: low attention (< 0.12) is completely transparent
            val alpha = if (v < 0.12f) {
                0
            } else {
                val normalizedActive = (v - 0.12f) / (1.0f - 0.12f)
                // Smooth ramp from 80 to 255 alpha
                (80 + (normalizedActive * 175f)).toInt().coerceIn(0, 255)
            }

            val r: Float
            val g: Float
            val b: Float

            when {
                v < 0.25f -> {
                    r = 0.0f
                    g = 4.0f * v
                    b = 1.0f
                }
                v < 0.50f -> {
                    r = 0.0f
                    g = 1.0f
                    b = 4.0f * (0.50f - v)
                }
                v < 0.75f -> {
                    r = 4.0f * (v - 0.50f)
                    g = 1.0f
                    b = 0.0f
                }
                else -> {
                    r = 1.0f
                    g = 4.0f * (1.0f - v)
                    b = 0.0f
                }
            }

            return Color.argb(
                alpha,
                (r * 255f).toInt().coerceIn(0, 255),
                (g * 255f).toInt().coerceIn(0, 255),
                (b * 255f).toInt().coerceIn(0, 255),
            )
        }

        /**
         * Creates a blended bitmap combining the source leaf photo and activation heatmap.
         * Ideal for PDF dossiers or image saving.
         */
        fun createBlendedBitmap(
            source: Bitmap,
            activationMatrix: Array<FloatArray>,
            opacityAlpha: Float = 0.70f
        ): Bitmap {
            val safeSource = if (source.config == Bitmap.Config.HARDWARE) {
                source.copy(Bitmap.Config.ARGB_8888, false)
            } else {
                source
            }

            val outBitmap = safeSource.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(outBitmap)
            val heatmap = generateJetHeatmapBitmap(activationMatrix)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                alpha = (opacityAlpha.coerceIn(0.0f, 1.0f) * 255f).toInt()
                isDither = true
            }

            val destRect = RectF(0f, 0f, outBitmap.width.toFloat(), outBitmap.height.toFloat())
            canvas.drawBitmap(heatmap, null, destRect, paint)
            return outBitmap
        }
    }
}
