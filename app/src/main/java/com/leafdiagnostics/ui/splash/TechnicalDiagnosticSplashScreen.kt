package com.leafdiagnostics.ui.splash

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first

/**
 * Technical Diagnostic Splash Screen for LeafLens AI.
 *
 * Implements a camera calibration and botanical diagnostic pipeline featuring:
 * - Spatial calibration (0.0s - 0.6s) with grid fade and damped spring reticle targeting.
 * - Spectral inference pass (0.6s - 1.4s) with an emerald laser sweeping top to bottom and
 *   activating vein graph nodes upon contact.
 * - Data telemetry ticker (1.4s - 2.0s) showing diagnostic telemetry sequence and buffer latency.
 * - Hand-off expansion (2.0s+) revealing the underlying camera or dashboard.
 *
 * @param isReady Real app initialization signal. The splash holds on the final telemetry frame
 * until this is true, then executes the expansion hand-off.
 * @param onScanComplete Callback invoked once the full choreography and expansion hand-off finish.
 */
@Composable
fun TechnicalDiagnosticSplashScreen(
    isReady: Boolean = true,
    onScanComplete: () -> Unit
) {
    val context = LocalContext.current
    val isReducedMotion = remember(context) { checkReducedMotion(context) }

    if (isReducedMotion) {
        LaunchedEffect(Unit) {
            onScanComplete()
        }
        TechnicalDiagnosticSplashScreenContent(
            state = DiagnosticTimelineState(
                timeSeconds = DiagnosticTimelineState.TIMELINE_BASELINE,
                isReady = true,
                isReducedMotion = true
            )
        )
        return
    }

    val timeline = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Phase 1 to 3: 0.0s -> 2.0s master timeline
        timeline.animateTo(
            targetValue = DiagnosticTimelineState.TIMELINE_BASELINE,
            animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
        )

        // Hold until app initialization is confirmed
        if (!isReady) {
            snapshotFlow { isReady }.first { it }
        }

        // Phase 4: Hand-off reticle expansion (2.0s -> 2.4s)
        timeline.animateTo(
            targetValue = DiagnosticTimelineState.HANDOFF_END,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )

        onScanComplete()
    }

    // Hoist timeline state with derivedStateOf to prevent unnecessary composable body recompositions
    val currentState by remember(isReady) {
        derivedStateOf {
            DiagnosticTimelineState(
                timeSeconds = timeline.value,
                isReady = isReady,
                isReducedMotion = false
            )
        }
    }

    TechnicalDiagnosticSplashScreenContent(
        stateProvider = { currentState }
    )
}

/**
 * Hoisted content composable supporting time-scrubbed previewing at fixed timestamps.
 */
@Composable
fun TechnicalDiagnosticSplashScreenContent(
    state: DiagnosticTimelineState,
    modifier: Modifier = Modifier
) {
    TechnicalDiagnosticSplashScreenContent(
        stateProvider = { state },
        modifier = modifier
    )
}

@Composable
fun TechnicalDiagnosticSplashScreenContent(
    stateProvider: () -> DiagnosticTimelineState,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DiagnosticSplashTheme.ObsidianCanvas)
            .clipToBounds()
    ) {
        // Fullscreen orthogonal grid (40dp spacing, 1px white lines at 8% alpha)
        GridBackground(stateProvider = stateProvider)

        // Central specimen, reticle, and laser scan line
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Specimen viewport
            SpecimenView(
                stateProvider = stateProvider,
                textMeasurer = textMeasurer,
                modifier = Modifier
                    .size(270.dp)
                    .padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Brand block
            BrandBlock(
                stateProvider = stateProvider
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Data telemetry ticker
            TelemetryTicker(
                stateProvider = stateProvider
            )
        }
    }
}

/**
 * Draws the 40dp orthogonal calibration grid.
 */
@Composable
private fun GridBackground(
    stateProvider: () -> DiagnosticTimelineState,
    modifier: Modifier = Modifier
) {
    val gridSpacing = 40.dp

    Canvas(modifier = modifier.fillMaxSize()) {
        val state = stateProvider()
        if (state.gridAlpha <= 0f) return@Canvas

        val spacingPx = gridSpacing.toPx()
        val lineColor = DiagnosticSplashTheme.GridLineColor.copy(
            alpha = DiagnosticSplashTheme.GridLineColor.alpha * state.gridAlpha
        )

        // Vertical lines
        var x = 0f
        while (x <= size.width) {
            drawLine(
                color = lineColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
            x += spacingPx
        }

        // Horizontal lines
        var y = 0f
        while (y <= size.height) {
            drawLine(
                color = lineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += spacingPx
        }
    }
}

/**
 * Draws the specimen contour, pinnate veins, graph nodes, targeting reticle, and sweeping laser.
 */
@Composable
private fun SpecimenView(
    stateProvider: () -> DiagnosticTimelineState,
    textMeasurer: TextMeasurer,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val reticleStrokeWidth = with(density) { 2.5.dp.toPx() }
    val reticleArmLength = with(density) { 24.dp.toPx() }
    val leafStrokeWidth = with(density) { 1.5.dp.toPx() }
    val veinStrokeWidth = with(density) { 1.2.dp.toPx() }
    val laserStrokeWidth = with(density) { 2.0.dp.toPx() }
    val laserGlowHeight = with(density) { 12.dp.toPx() }

    Canvas(modifier = modifier) {
        val state = stateProvider()
        val specimenSize = size.minDimension
        val originX = (size.width - specimenSize) / 2f
        val originY = (size.height - specimenSize) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // 1. Targeting reticle (4 corner brackets + coordinate labels)
        if (state.reticleAlpha > 0f) {
            scale(scale = state.reticleScale, pivot = center) {
                drawReticle(
                    originX = originX,
                    originY = originY,
                    specimenSize = specimenSize,
                    strokeWidth = reticleStrokeWidth,
                    armLength = reticleArmLength,
                    alpha = state.reticleAlpha,
                    textMeasurer = textMeasurer
                )
            }
        }

        // 2. Leaf silhouette outline wireframe
        if (state.leafWireframeAlpha > 0f) {
            val leafPath = SpecimenGeometry.buildLeafContourPath(originX, originY, specimenSize)

            // Base wireframe path
            drawPath(
                path = leafPath,
                color = DiagnosticSplashTheme.WireframeWhite.copy(alpha = state.leafWireframeAlpha),
                style = Stroke(width = leafStrokeWidth)
            )

            // Dynamic edge highlight along laser scan line
            if (state.isScanLineActive) {
                val scanYPixel = originY + state.scanProgress * specimenSize
                val highlightBandHeight = 28.dp.toPx()

                // Highlight near the laser scan position
                clipRect(
                    left = originX,
                    top = scanYPixel - highlightBandHeight / 2f,
                    right = originX + specimenSize,
                    bottom = scanYPixel + highlightBandHeight / 2f
                ) {
                    drawPath(
                        path = leafPath,
                        color = DiagnosticSplashTheme.ElectricEmerald.copy(
                            alpha = (state.scanLineAlpha * 0.85f).coerceIn(0f, 1f)
                        ),
                        style = Stroke(width = leafStrokeWidth * 1.8f)
                    )
                }
            }
        }

        // 3. Pinnate veins (midrib + 4 lateral pairs)
        if (state.leafWireframeAlpha > 0f) {
            val veinColor = DiagnosticSplashTheme.SteelSlate.copy(
                alpha = (state.leafWireframeAlpha * 2.2f).coerceIn(0f, 1f)
            )
            for (segment in SpecimenGeometry.veins) {
                val start = segment.start.toPixelOffset(originX, originY, specimenSize)
                val end = segment.end.toPixelOffset(originX, originY, specimenSize)
                drawLine(
                    color = veinColor,
                    start = start,
                    end = end,
                    strokeWidth = veinStrokeWidth
                )
            }
        }

        // 4. Graph nodes (inactive vs activated with pulse and soft glow)
        if (state.leafWireframeAlpha > 0f) {
            for (node in SpecimenGeometry.allNodes) {
                val isActivated = state.isNodeActivated(node)
                val pulse = state.nodePulseScale(node)
                val nodeOffset = node.toPixelOffset(originX, originY, specimenSize)

                if (isActivated) {
                    // Soft glow outer halo
                    drawCircle(
                        color = DiagnosticSplashTheme.EmeraldGlow.copy(
                            alpha = (0.45f * state.uiElementsAlpha).coerceIn(0f, 1f)
                        ),
                        radius = 8.dp.toPx() * pulse,
                        center = nodeOffset
                    )
                    // Core activated node
                    drawCircle(
                        color = DiagnosticSplashTheme.ElectricEmerald.copy(
                            alpha = state.uiElementsAlpha
                        ),
                        radius = 4.dp.toPx() * pulse,
                        center = nodeOffset
                    )
                } else {
                    // Inactive slate node
                    drawCircle(
                        color = DiagnosticSplashTheme.SteelSlate.copy(
                            alpha = state.uiElementsAlpha
                        ),
                        radius = 2.5.dp.toPx(),
                        center = nodeOffset
                    )
                }
            }
        }

        // 5. Emerald laser scan line (sweeps top to bottom)
        if (state.isScanLineActive && state.scanLineAlpha > 0f) {
            val laserY = originY + state.scanProgress * specimenSize
            val laserLeft = originX - 12.dp.toPx()
            val laserRight = originX + specimenSize + 12.dp.toPx()

            // Horizontal gradient brush: Transparent -> Emerald -> Transparent
            val laserBrush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    DiagnosticSplashTheme.ElectricEmerald.copy(alpha = state.scanLineAlpha),
                    DiagnosticSplashTheme.ElectricEmerald.copy(alpha = state.scanLineAlpha),
                    Color.Transparent
                ),
                startX = laserLeft,
                endX = laserRight
            )

            val glowBrush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    DiagnosticSplashTheme.EmeraldGlow.copy(alpha = state.scanLineAlpha * 0.45f),
                    Color.Transparent
                ),
                startY = laserY - laserGlowHeight / 2f,
                endY = laserY + laserGlowHeight / 2f
            )

            // Draw soft vertical diffuse glow
            drawRect(
                brush = glowBrush,
                topLeft = Offset(laserLeft, laserY - laserGlowHeight / 2f),
                size = Size(laserRight - laserLeft, laserGlowHeight)
            )

            // Draw crisp horizontal laser line
            drawLine(
                brush = laserBrush,
                start = Offset(laserLeft, laserY),
                end = Offset(laserRight, laserY),
                strokeWidth = laserStrokeWidth
            )
        }
    }
}

/**
 * Draws the targeting reticle brackets and coordinate readouts.
 */
private fun DrawScope.drawReticle(
    originX: Float,
    originY: Float,
    specimenSize: Float,
    strokeWidth: Float,
    armLength: Float,
    alpha: Float,
    textMeasurer: TextMeasurer
) {
    val reticleColor = DiagnosticSplashTheme.MutedCyan.copy(alpha = alpha)
    val left = originX
    val top = originY
    val right = originX + specimenSize
    val bottom = originY + specimenSize

    // Top-Left corner bracket
    drawLine(color = reticleColor, start = Offset(left, top), end = Offset(left + armLength, top), strokeWidth = strokeWidth)
    drawLine(color = reticleColor, start = Offset(left, top), end = Offset(left, top + armLength), strokeWidth = strokeWidth)

    // Top-Right corner bracket
    drawLine(color = reticleColor, start = Offset(right, top), end = Offset(right - armLength, top), strokeWidth = strokeWidth)
    drawLine(color = reticleColor, start = Offset(right, top), end = Offset(right, top + armLength), strokeWidth = strokeWidth)

    // Bottom-Left corner bracket
    drawLine(color = reticleColor, start = Offset(left, bottom), end = Offset(left + armLength, bottom), strokeWidth = strokeWidth)
    drawLine(color = reticleColor, start = Offset(left, bottom), end = Offset(left, bottom - armLength), strokeWidth = strokeWidth)

    // Bottom-Right corner bracket
    drawLine(color = reticleColor, start = Offset(right, bottom), end = Offset(right - armLength, bottom), strokeWidth = strokeWidth)
    drawLine(color = reticleColor, start = Offset(right, bottom), end = Offset(right, bottom - armLength), strokeWidth = strokeWidth)

    // Dynamic coordinate labels derived from actual node positions
    val coordTopLeft = SpecimenGeometry.computeCoordinatesLabel(
        originX, originY, specimenSize, SpecimenGeometry.minX, SpecimenGeometry.minY
    )
    val coordBottomRight = SpecimenGeometry.computeCoordinatesLabel(
        originX, originY, specimenSize, SpecimenGeometry.maxX, SpecimenGeometry.maxY
    )

    val labelStyle = DiagnosticSplashTheme.CoordinateLabelStyle.copy(
        color = DiagnosticSplashTheme.MutedCyan.copy(alpha = alpha * 0.85f)
    )

    // Draw top coordinate label
    val textLayoutTop = textMeasurer.measure(coordTopLeft, labelStyle)
    drawText(
        textLayoutResult = textLayoutTop,
        topLeft = Offset(left + 6.dp.toPx(), top - textLayoutTop.size.height - 4.dp.toPx())
    )

    // Draw bottom coordinate label
    val textLayoutBottom = textMeasurer.measure(coordBottomRight, labelStyle)
    drawText(
        textLayoutResult = textLayoutBottom,
        topLeft = Offset(right - textLayoutBottom.size.width - 6.dp.toPx(), bottom + 4.dp.toPx())
    )
}

/**
 * Brand wordmark and monospace subtitle block.
 */
@Composable
private fun BrandBlock(
    stateProvider: () -> DiagnosticTimelineState,
    modifier: Modifier = Modifier
) {
    val state = stateProvider()
    val alpha = state.uiElementsAlpha

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = SplashTelemetryConstants.WORDMARK_TEXT,
            style = DiagnosticSplashTheme.WordmarkStyle.copy(
                color = Color.White.copy(alpha = alpha)
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = SplashTelemetryConstants.SUBTITLE_TEXT,
            style = DiagnosticSplashTheme.SubtitleStyle.copy(
                color = DiagnosticSplashTheme.MutedCyan.copy(alpha = alpha)
            )
        )
    }
}

/**
 * Sequential monospace telemetry line and progress buffer readout.
 */
@Composable
private fun TelemetryTicker(
    stateProvider: () -> DiagnosticTimelineState,
    modifier: Modifier = Modifier
) {
    val state = stateProvider()
    val alpha = state.uiElementsAlpha

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = state.activeTelemetryText,
            style = DiagnosticSplashTheme.TelemetryPrimaryStyle.copy(
                color = DiagnosticSplashTheme.ElectricEmerald.copy(alpha = alpha)
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = state.bufferLatencyText,
            style = DiagnosticSplashTheme.TelemetrySecondaryStyle.copy(
                color = DiagnosticSplashTheme.SteelSlateLight.copy(alpha = alpha)
            )
        )
    }
}

/**
 * Detects whether the system has reduced motion or animations turned off.
 */
private fun checkReducedMotion(context: Context): Boolean {
    return try {
        val resolver = context.contentResolver
        val durationScale = Settings.Global.getFloat(
            resolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1.0f
        )
        val transitionScale = Settings.Global.getFloat(
            resolver,
            Settings.Global.TRANSITION_ANIMATION_SCALE,
            1.0f
        )
        durationScale == 0f || transitionScale == 0f
    } catch (_: Throwable) {
        false
    }
}

// ======================================================================================
// Jetpack Compose Previews at Fixed Timestamps
// ======================================================================================

@Preview(name = "Phase 1: Spatial Calibration (0.3s)", showBackground = true)
@Composable
fun DiagnosticSplashPreview_0_3s() {
    TechnicalDiagnosticSplashScreenContent(
        state = DiagnosticTimelineState(timeSeconds = 0.3f, isReady = true)
    )
}

@Preview(name = "Phase 2: Spectral Inference Pass (1.0s)", showBackground = true)
@Composable
fun DiagnosticSplashPreview_1_0s() {
    TechnicalDiagnosticSplashScreenContent(
        state = DiagnosticTimelineState(timeSeconds = 1.0f, isReady = true)
    )
}

@Preview(name = "Phase 3: Data Telemetry Ticker (1.7s)", showBackground = true)
@Composable
fun DiagnosticSplashPreview_1_7s() {
    TechnicalDiagnosticSplashScreenContent(
        state = DiagnosticTimelineState(timeSeconds = 1.7f, isReady = true)
    )
}

@Preview(name = "Phase 4: Reticle Hand-off Expansion (2.2s)", showBackground = true)
@Composable
fun DiagnosticSplashPreview_2_2s() {
    TechnicalDiagnosticSplashScreenContent(
        state = DiagnosticTimelineState(timeSeconds = 2.2f, isReady = true)
    )
}

@Preview(name = "Small Screen Layout (360dp width)", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
fun DiagnosticSplashPreview_SmallScreen() {
    TechnicalDiagnosticSplashScreenContent(
        state = DiagnosticTimelineState(timeSeconds = 1.0f, isReady = true)
    )
}
