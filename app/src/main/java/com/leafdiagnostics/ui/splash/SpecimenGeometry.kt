package com.leafdiagnostics.ui.splash

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path

/**
 * Normalized 2D coordinate in the specimen unit space [0.0..1.0].
 */
data class SpecimenPoint(
    val id: String,
    val x: Float,
    val y: Float
) {
    fun toPixelOffset(originX: Float, originY: Float, size: Float): Offset {
        return Offset(originX + x * size, originY + y * size)
    }
}

/**
 * Directed connection between two specimen graph nodes.
 */
data class VeinSegment(
    val start: SpecimenPoint,
    val end: SpecimenPoint
)

/**
 * Cubic bezier definition for smooth wireframe leaf contour.
 */
data class ContourBezier(
    val startX: Float,
    val startY: Float,
    val control1X: Float,
    val control1Y: Float,
    val control2X: Float,
    val control2Y: Float,
    val endX: Float,
    val endY: Float
)

/**
 * Centralized geometry specification for the LeafLens botanical specimen.
 * All outline curves, pinnate veins, graph nodes, and coordinate labels derive from this source.
 */
object SpecimenGeometry {
    // 15 Graph nodes ordered by Y coordinate for exact laser scan synchronization
    val nodeTip = SpecimenPoint("node_tip", 0.50f, 0.12f)
    val nodeV4Left = SpecimenPoint("node_v4_l", 0.40f, 0.18f)
    val nodeV4Right = SpecimenPoint("node_v4_r", 0.60f, 0.18f)
    val nodeMidrib4 = SpecimenPoint("node_mb_4", 0.50f, 0.24f)

    val nodeV3Left = SpecimenPoint("node_v3_l", 0.31f, 0.30f)
    val nodeV3Right = SpecimenPoint("node_v3_r", 0.69f, 0.30f)
    val nodeMidrib3 = SpecimenPoint("node_mb_3", 0.50f, 0.38f)

    val nodeV2Left = SpecimenPoint("node_v2_l", 0.26f, 0.44f)
    val nodeV2Right = SpecimenPoint("node_v2_r", 0.74f, 0.44f)
    val nodeMidrib2 = SpecimenPoint("node_mb_2", 0.50f, 0.52f)

    val nodeV1Left = SpecimenPoint("node_v1_l", 0.32f, 0.60f)
    val nodeV1Right = SpecimenPoint("node_v1_r", 0.68f, 0.60f)
    val nodeMidrib1 = SpecimenPoint("node_mb_1", 0.50f, 0.68f)

    val nodeBase = SpecimenPoint("node_base", 0.50f, 0.82f)
    val nodePetiole = SpecimenPoint("node_petiole", 0.50f, 0.90f)

    val allNodes: List<SpecimenPoint> = listOf(
        nodeTip,
        nodeV4Left,
        nodeV4Right,
        nodeMidrib4,
        nodeV3Left,
        nodeV3Right,
        nodeMidrib3,
        nodeV2Left,
        nodeV2Right,
        nodeMidrib2,
        nodeV1Left,
        nodeV1Right,
        nodeMidrib1,
        nodeBase,
        nodePetiole
    )

    // Midrib segments and pinnate secondary vein pairs
    val veins: List<VeinSegment> = listOf(
        // Central Midrib
        VeinSegment(nodePetiole, nodeBase),
        VeinSegment(nodeBase, nodeMidrib1),
        VeinSegment(nodeMidrib1, nodeMidrib2),
        VeinSegment(nodeMidrib2, nodeMidrib3),
        VeinSegment(nodeMidrib3, nodeMidrib4),
        VeinSegment(nodeMidrib4, nodeTip),

        // Lateral Vein Pair 1 (lowest)
        VeinSegment(nodeMidrib1, nodeV1Left),
        VeinSegment(nodeMidrib1, nodeV1Right),

        // Lateral Vein Pair 2
        VeinSegment(nodeMidrib2, nodeV2Left),
        VeinSegment(nodeMidrib2, nodeV2Right),

        // Lateral Vein Pair 3
        VeinSegment(nodeMidrib3, nodeV3Left),
        VeinSegment(nodeMidrib3, nodeV3Right),

        // Lateral Vein Pair 4 (highest)
        VeinSegment(nodeMidrib4, nodeV4Left),
        VeinSegment(nodeMidrib4, nodeV4Right)
    )

    // Leaf outline curves (pointed-oval contour + petiole)
    val leftContour = ContourBezier(
        startX = 0.50f, startY = 0.82f,
        control1X = 0.20f, control1Y = 0.70f,
        control2X = 0.22f, control2Y = 0.28f,
        endX = 0.50f, endY = 0.12f
    )

    val rightContour = ContourBezier(
        startX = 0.50f, startY = 0.12f,
        control1X = 0.78f, control1Y = 0.28f,
        control2X = 0.80f, control2Y = 0.70f,
        endX = 0.50f, endY = 0.82f
    )

    /**
     * Constructs a closed Compose Path in pixel space for the specimen leaf silhouette.
     */
    fun buildLeafContourPath(originX: Float, originY: Float, size: Float): Path {
        return Path().apply {
            // Start at petiole base
            moveTo(originX + nodePetiole.x * size, originY + nodePetiole.y * size)
            lineTo(originX + nodeBase.x * size, originY + nodeBase.y * size)

            // Left curved lamina to tip
            cubicTo(
                originX + leftContour.control1X * size, originY + leftContour.control1Y * size,
                originX + leftContour.control2X * size, originY + leftContour.control2Y * size,
                originX + leftContour.endX * size, originY + leftContour.endY * size
            )

            // Right curved lamina to base
            cubicTo(
                originX + rightContour.control1X * size, originY + rightContour.control1Y * size,
                originX + rightContour.control2X * size, originY + rightContour.control2Y * size,
                originX + rightContour.endX * size, originY + rightContour.endY * size
            )

            // Back to petiole base
            lineTo(originX + nodePetiole.x * size, originY + nodePetiole.y * size)
            close()
        }
    }

    /**
     * Bounding box derived from specimen nodes for reticle coordinate calculations.
     */
    val minX: Float = allNodes.minOf { it.x }
    val maxX: Float = allNodes.maxOf { it.x }
    val minY: Float = allNodes.minOf { it.y }
    val maxY: Float = allNodes.maxOf { it.y }

    fun computeCoordinatesLabel(
        originX: Float,
        originY: Float,
        size: Float,
        normalizedX: Float,
        normalizedY: Float
    ): String {
        val pixelX = originX + normalizedX * size
        val pixelY = originY + normalizedY * size
        return "[X: ${"%.1f".format(pixelX)}, Y: ${"%.1f".format(pixelY)}]"
    }
}
