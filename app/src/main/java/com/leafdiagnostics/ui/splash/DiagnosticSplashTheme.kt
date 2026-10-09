package com.leafdiagnostics.ui.splash

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Visual design system constants for the LeafLens Technical Diagnostic splash screen.
 */
object DiagnosticSplashTheme {
    // Canvas: Matte Obsidian
    val ObsidianCanvas = Color(0xFF0B0F10)

    // Primary Accents: Electric Emerald
    val ElectricEmerald = Color(0xFF00E599)
    val ElectricEmeraldDim = Color(0xFF10B981)
    val EmeraldGlow = Color(0x6600E599)

    // Viewfinder & Reticle: Muted Cyan
    val MutedCyan = Color(0xFF38BDF8)
    val CyanGlow = Color(0x4038BDF8)

    // Structural elements & inactive states: Steel Slate
    val SteelSlate = Color(0xFF475569)
    val SteelSlateLight = Color(0xFF94A3B8)
    val WireframeWhite = Color(0x3BFFFFFF) // ~23% opacity white for specimen outline

    // Grid and frames: White at 8% alpha
    val GridLineColor = Color(0x14FFFFFF)

    // Typography
    val WordmarkStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 3.sp,
        color = Color.White
    )

    val SubtitleStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 2.sp,
        color = MutedCyan
    )

    val TelemetryPrimaryStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        letterSpacing = 1.sp,
        color = ElectricEmerald
    )

    val TelemetrySecondaryStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        letterSpacing = 1.sp,
        color = SteelSlateLight
    )

    val CoordinateLabelStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 9.sp,
        letterSpacing = 0.5.sp,
        color = MutedCyan
    )
}
