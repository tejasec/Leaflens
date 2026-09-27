package com.example.smartagriculture.compose.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

class HeatmapOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var isHeatmapVisible: Boolean = true

    private val hotPaint1 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val hotPaint2 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val coolPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun setHeatmapVisible(visible: Boolean) {
        isHeatmapVisible = visible
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w <= 0 || h <= 0) return

        // High influence hot spot 1 (Red/Yellow core)
        val shader1 = RadialGradient(
            w * 0.45f, h * 0.45f, w * 0.35f,
            intArrayOf(Color.argb(200, 255, 50, 0), Color.argb(160, 255, 200, 0), Color.TRANSPARENT),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        hotPaint1.shader = shader1

        // High influence hot spot 2 (Red/Orange center-right)
        val shader2 = RadialGradient(
            w * 0.65f, h * 0.55f, w * 0.28f,
            intArrayOf(Color.argb(190, 255, 0, 80), Color.argb(140, 255, 150, 0), Color.TRANSPARENT),
            floatArrayOf(0f, 0.6f, 1f),
            Shader.TileMode.CLAMP
        )
        hotPaint2.shader = shader2

        // Cool surrounding influence (Blue/Cyan gradient)
        val shaderCool = LinearGradient(
            0f, 0f, w.toFloat(), h.toFloat(),
            intArrayOf(Color.argb(80, 0, 100, 255), Color.argb(40, 0, 255, 200), Color.TRANSPARENT),
            null,
            Shader.TileMode.CLAMP
        )
        coolPaint.shader = shaderCool
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isHeatmapVisible) return

        val w = width.toFloat()
        val h = height.toFloat()

        canvas.drawRect(0f, 0f, w, h, coolPaint)
        canvas.drawCircle(w * 0.45f, h * 0.45f, w * 0.35f, hotPaint1)
        canvas.drawCircle(w * 0.65f, h * 0.55f, w * 0.28f, hotPaint2)
    }
}
