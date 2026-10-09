package com.leafdiagnostics.ui.splash

/**
 * Telemetry text constants and default readouts for the LeafLens diagnostic pipeline.
 */
object SplashTelemetryConstants {
    const val WORDMARK_TEXT = "Leaflens AI"
    const val SUBTITLE_TEXT = "Plant Care AI"

    val TELEMETRY_LINES = listOf(
        "> CALIBRATING SENSOR... OK",
        "> VEIN_TOPOLOGY: PINNATE",
        "> NDVI_INDEX: 0.78 [OPTIMAL]",
        "> INFERENCE ENGINE INITIALIZED"
    )

    const val LATENCY_MS = 14
    const val NDVI_VALUE = 0.78f

    fun formatBufferLatency(progressPercent: Int): String {
        val safePercent = progressPercent.coerceIn(0, 100)
        return "BUFFER: [${safePercent.toString().padStart(2, '0')}%] // LATENCY: ${LATENCY_MS}ms"
    }
}
