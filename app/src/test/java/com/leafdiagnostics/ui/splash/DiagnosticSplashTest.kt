package com.leafdiagnostics.ui.splash

import org.junit.Assert.*
import org.junit.Test

class DiagnosticSplashTest {

    @Test
    fun testSpecimenGeometryIntegrity() {
        // Must contain 15 graph nodes
        assertEquals(15, SpecimenGeometry.allNodes.size)

        // All normalized coordinates must be strictly within 0.0..1.0
        for (node in SpecimenGeometry.allNodes) {
            assertTrue("Node ${node.id} X out of bounds: ${node.x}", node.x in 0.0f..1.0f)
            assertTrue("Node ${node.id} Y out of bounds: ${node.y}", node.y in 0.0f..1.0f)
        }

        // Verify nodes are ordered by non-decreasing Y coordinate for top-to-bottom laser alignment
        for (i in 0 until SpecimenGeometry.allNodes.size - 1) {
            val current = SpecimenGeometry.allNodes[i]
            val next = SpecimenGeometry.allNodes[i + 1]
            assertTrue(
                "Nodes out of Y sequence: ${current.id} (${current.y}) > ${next.id} (${next.y})",
                current.y <= next.y
            )
        }

        // Veins must connect valid nodes and contain midrib plus 4 lateral pairs (total 14 segments)
        assertEquals(14, SpecimenGeometry.veins.size)

        // Dynamic coordinate label generation
        val label = SpecimenGeometry.computeCoordinatesLabel(
            originX = 50f,
            originY = 100f,
            size = 200f,
            normalizedX = 0.5f,
            normalizedY = 0.25f
        )
        assertEquals("[X: 150.0, Y: 150.0]", label)
    }

    @Test
    fun testSplashTelemetryConstants() {
        assertEquals("Leaflens AI", SplashTelemetryConstants.WORDMARK_TEXT)
        assertEquals("Plant Care AI", SplashTelemetryConstants.SUBTITLE_TEXT)
        assertEquals(4, SplashTelemetryConstants.TELEMETRY_LINES.size)

        assertEquals("> CALIBRATING SENSOR... OK", SplashTelemetryConstants.TELEMETRY_LINES[0])
        assertEquals("> VEIN_TOPOLOGY: PINNATE", SplashTelemetryConstants.TELEMETRY_LINES[1])
        assertEquals("> NDVI_INDEX: 0.78 [OPTIMAL]", SplashTelemetryConstants.TELEMETRY_LINES[2])
        assertEquals("> INFERENCE ENGINE INITIALIZED", SplashTelemetryConstants.TELEMETRY_LINES[3])

        val bufferText = SplashTelemetryConstants.formatBufferLatency(75)
        assertEquals("BUFFER: [75%] // LATENCY: 14ms", bufferText)
    }

    @Test
    fun testTimelinePhase1SpatialCalibration() {
        val state0 = DiagnosticTimelineState(timeSeconds = 0.0f, isReady = true)
        assertEquals(0.0f, state0.gridAlpha, 0.001f)
        assertTrue(state0.reticleScale > 1.15f)
        assertEquals(0.0f, state0.leafWireframeAlpha, 0.001f)
        assertFalse(state0.isScanLineActive)

        val stateMid = DiagnosticTimelineState(timeSeconds = 0.3f, isReady = true)
        assertTrue(stateMid.gridAlpha > 0.5f)
        assertTrue(stateMid.leafWireframeAlpha > 0.05f)
        assertFalse(stateMid.isNodeActivated(SpecimenGeometry.nodeTip))

        val stateEnd = DiagnosticTimelineState(timeSeconds = 0.6f, isReady = true)
        assertEquals(1.0f, stateEnd.reticleScale, 0.01f)
        assertEquals(0.23f, stateEnd.leafWireframeAlpha, 0.01f)
    }

    @Test
    fun testTimelinePhase2SpectralInferencePass() {
        val stateStart = DiagnosticTimelineState(timeSeconds = 0.6f, isReady = true)
        assertTrue(stateStart.isScanLineActive)
        assertEquals(0.0f, stateStart.scanProgress, 0.001f)

        // At halfway point (1.0s), scanProgress is 0.5
        val stateMid = DiagnosticTimelineState(timeSeconds = 1.0f, isReady = true)
        assertTrue(stateMid.isScanLineActive)
        assertEquals(0.5f, stateMid.scanProgress, 0.01f)

        // Tip node (y = 0.12) and upper lateral nodes should be active
        assertTrue(stateMid.isNodeActivated(SpecimenGeometry.nodeTip))
        assertTrue(stateMid.isNodeActivated(SpecimenGeometry.nodeV4Left))

        // Lower nodes (y > 0.50) should still be inactive
        assertFalse(stateMid.isNodeActivated(SpecimenGeometry.nodeV1Left))
        assertFalse(stateMid.isNodeActivated(SpecimenGeometry.nodeBase))

        // At scan end (1.4s), all nodes must be active
        val stateEnd = DiagnosticTimelineState(timeSeconds = 1.4f, isReady = true)
        assertEquals(1.0f, stateEnd.scanProgress, 0.001f)
        for (node in SpecimenGeometry.allNodes) {
            assertTrue(stateEnd.isNodeActivated(node))
        }
    }

    @Test
    fun testTimelinePhase3TelemetryTicker() {
        val t0 = DiagnosticTimelineState(timeSeconds = 1.45f, isReady = true)
        assertEquals(0, t0.telemetryLineIndex)
        assertEquals("> CALIBRATING SENSOR... OK", t0.activeTelemetryText)

        val t1 = DiagnosticTimelineState(timeSeconds = 1.60f, isReady = true)
        assertEquals(1, t1.telemetryLineIndex)
        assertEquals("> VEIN_TOPOLOGY: PINNATE", t1.activeTelemetryText)

        val t2 = DiagnosticTimelineState(timeSeconds = 1.75f, isReady = true)
        assertEquals(2, t2.telemetryLineIndex)
        assertEquals("> NDVI_INDEX: 0.78 [OPTIMAL]", t2.activeTelemetryText)

        val t3 = DiagnosticTimelineState(timeSeconds = 1.90f, isReady = true)
        assertEquals(3, t3.telemetryLineIndex)
        assertEquals("> INFERENCE ENGINE INITIALIZED", t3.activeTelemetryText)
    }

    @Test
    fun testTimelinePhase4HandoffAndReadyHold() {
        // When not ready at 2.0s+, it holds on the final telemetry frame
        val holdState = DiagnosticTimelineState(timeSeconds = 2.0f, isReady = false)
        assertFalse(holdState.isHandoffActive)
        assertFalse(holdState.isHandoffComplete)
        assertEquals(3, holdState.telemetryLineIndex)
        assertEquals(100, holdState.bufferProgressPercent)
        assertEquals(1.0f, holdState.reticleScale, 0.01f)

        // When ready at 2.2s (mid handoff)
        val handoffMid = DiagnosticTimelineState(timeSeconds = 2.2f, isReady = true)
        assertTrue(handoffMid.isHandoffActive)
        assertFalse(handoffMid.isHandoffComplete)
        assertEquals(0.5f, handoffMid.handoffProgress, 0.01f)
        assertTrue("Reticle should expand: ${handoffMid.reticleScale}", handoffMid.reticleScale > 1.0f)
        assertTrue("UI elements should fade: ${handoffMid.uiElementsAlpha}", handoffMid.uiElementsAlpha < 1.0f)

        // When ready at 2.4s (handoff complete)
        val handoffComplete = DiagnosticTimelineState(timeSeconds = 2.4f, isReady = true)
        assertTrue(handoffComplete.isHandoffComplete)
        assertEquals(0.0f, handoffComplete.uiElementsAlpha, 0.01f)
        assertTrue("Reticle must expand outward past screen bounds: ${handoffComplete.reticleScale}", handoffComplete.reticleScale >= 4.0f)
    }

    @Test
    fun testReducedMotion() {
        val reduced = DiagnosticTimelineState(timeSeconds = 0.0f, isReducedMotion = true)
        assertTrue(reduced.isHandoffComplete)
        assertEquals(1.0f, reduced.reticleScale, 0.01f)
        assertEquals(1.0f, reduced.gridAlpha, 0.01f)
        assertEquals(3, reduced.telemetryLineIndex)
        for (node in SpecimenGeometry.allNodes) {
            assertTrue(reduced.isNodeActivated(node))
        }
    }
}
