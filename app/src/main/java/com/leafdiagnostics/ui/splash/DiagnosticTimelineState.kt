package com.leafdiagnostics.ui.splash

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Deterministic mathematical representation of the LeafLens diagnostic splash choreography.
 * Allows time-scrubbing and static preview rendering at any arbitrary timestamp.
 */
data class DiagnosticTimelineState(
    val timeSeconds: Float,
    val isReady: Boolean = true,
    val isReducedMotion: Boolean = false
) {
    // Phase durations
    companion object {
        const val CALIBRATION_END = 0.6f
        const val SCAN_START = 0.6f
        const val SCAN_END = 1.4f
        const val TELEMETRY_START = 1.4f
        const val TIMELINE_BASELINE = 2.0f
        const val HANDOFF_END = 2.4f
    }

    val isHandoffActive: Boolean = !isReducedMotion && timeSeconds >= TIMELINE_BASELINE && isReady

    // Hand-off progress: 0.0f to 1.0f during 2.0s -> 2.4s
    val handoffProgress: Float = if (isReducedMotion) {
        1.0f
    } else if (timeSeconds < TIMELINE_BASELINE || !isReady) {
        0.0f
    } else {
        ((timeSeconds - TIMELINE_BASELINE) / (HANDOFF_END - TIMELINE_BASELINE)).coerceIn(0f, 1f)
    }

    val isHandoffComplete: Boolean = if (isReducedMotion) {
        true
    } else {
        timeSeconds >= HANDOFF_END && isReady
    }

    // Grid alpha (0f -> 1f during 0.0 -> 0.5s; fades to 0 during handoff)
    val gridAlpha: Float = if (isReducedMotion) {
        1.0f
    } else {
        val entryAlpha = (timeSeconds / 0.5f).coerceIn(0f, 1f)
        val exitAlpha = (1.0f - handoffProgress).coerceIn(0f, 1f)
        entryAlpha * exitAlpha
    }

    // Reticle scale: snaps from ~1.2x to 1.0x with high damping during 0.0 -> 0.6s.
    // During handoff, expands outward from 1.0x to 4.0x+ to reveal the underlying screen.
    val reticleScale: Float = if (isReducedMotion) {
        1.0f
    } else if (timeSeconds < CALIBRATION_END) {
        val p = (timeSeconds / CALIBRATION_END).coerceIn(0f, 1f)
        // High-damping spring: 1.2 -> 1.0 with subtle overshoot
        1.0f + 0.2f * (1f - p) * cos(p * PI * 1.5).toFloat()
    } else if (isHandoffActive) {
        // Fast cubic expansion outward to screen edges
        1.0f + 3.2f * (handoffProgress * handoffProgress)
    } else {
        1.0f
    }

    // Reticle alpha: visible from start, fades out at the very end of expansion
    val reticleAlpha: Float = if (isReducedMotion) {
        1.0f
    } else if (handoffProgress > 0.85f) {
        ((1.0f - handoffProgress) / 0.15f).coerceIn(0f, 1f)
    } else {
        1.0f
    }

    // Leaf wireframe stroke opacity (fades in 0f -> 0.23f during calibration, fades out during handoff)
    val leafWireframeAlpha: Float = if (isReducedMotion) {
        0.23f
    } else {
        val entry = (timeSeconds / CALIBRATION_END).coerceIn(0f, 1f) * 0.23f
        val exit = (1.0f - handoffProgress).coerceIn(0f, 1f)
        entry * exit
    }

    // Laser scan progress: 0f -> 1f during 0.6s -> 1.4s
    val scanProgress: Float = if (isReducedMotion) {
        1.0f
    } else if (timeSeconds < SCAN_START) {
        0.0f
    } else if (timeSeconds > SCAN_END) {
        1.0f
    } else {
        ((timeSeconds - SCAN_START) / (SCAN_END - SCAN_START)).coerceIn(0f, 1f)
    }

    val isScanLineActive: Boolean = !isReducedMotion && timeSeconds in SCAN_START..SCAN_END

    // Laser line alpha: fades in slightly at start of scan and fades out at end of scan
    val scanLineAlpha: Float = if (!isScanLineActive) {
        0.0f
    } else {
        val progress = (timeSeconds - SCAN_START) / (SCAN_END - SCAN_START)
        when {
            progress < 0.05f -> progress / 0.05f
            progress > 0.95f -> (1.0f - progress) / 0.05f
            else -> 1.0f
        }
    }

    // Node state helpers
    fun isNodeActivated(node: SpecimenPoint): Boolean {
        if (isReducedMotion || timeSeconds >= SCAN_END) return true
        if (timeSeconds < SCAN_START) return false
        return scanProgress >= node.y
    }

    /**
     * Brief pulse scale (1.0 -> 1.6 -> 1.0) when laser scan line crosses node.y
     */
    fun nodePulseScale(node: SpecimenPoint): Float {
        if (isReducedMotion || timeSeconds < SCAN_START || timeSeconds >= SCAN_END) return 1.0f
        val window = 0.10f
        if (scanProgress >= node.y && scanProgress <= node.y + window) {
            val factor = ((scanProgress - node.y) / window).coerceIn(0f, 1f)
            return 1.0f + 0.6f * sin(factor * PI).toFloat()
        }
        return 1.0f
    }

    // General UI fading for brand block and telemetry during hand-off
    val uiElementsAlpha: Float = if (isReducedMotion) {
        1.0f
    } else {
        (1.0f - handoffProgress).coerceIn(0f, 1f)
    }

    // Telemetry ticker sequential lines: 4 lines, ~150ms each (1.40 -> 2.00s)
    val telemetryLineIndex: Int = if (isReducedMotion || timeSeconds >= TIMELINE_BASELINE) {
        3
    } else if (timeSeconds < TELEMETRY_START) {
        0
    } else {
        val elapsed = timeSeconds - TELEMETRY_START
        val step = (elapsed / 0.15f).toInt()
        step.coerceIn(0, 3)
    }

    val activeTelemetryText: String = SplashTelemetryConstants.TELEMETRY_LINES[telemetryLineIndex]

    // Buffer percentage: tied to real progress (0% at 0.0s to 100% at 2.0s)
    val bufferProgressPercent: Int = if (isReducedMotion) {
        100
    } else {
        ((timeSeconds / TIMELINE_BASELINE).coerceIn(0f, 1f) * 100).toInt()
    }

    val bufferLatencyText: String = SplashTelemetryConstants.formatBufferLatency(bufferProgressPercent)
}
