@file:Suppress(
    "ReturnCount",
    "TopLevelPropertyNaming",
    "ktlint:standard:property-naming",
    "ktlint:standard:function-naming",
)
/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.viewport

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import it.unibo.alchemist.boundary.composeui.model.ViewportNode
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import kotlin.math.max
import kotlin.math.min

/**
 * Maps world coordinates to pixels.
 * The viewport keeps the first valid one, so the mapping never changes under the user.
 */
internal data class ViewportProjection(val worldCenterX: Double, val worldCenterY: Double, val pixelsPerUnit: Float) {
    init {
        require(pixelsPerUnit > 0f) {
            "Viewport projection requires a positive pixels-per-unit ratio."
        }
    }
}

internal fun ViewportScene.createViewportProjection(viewportSize: IntSize): ViewportProjection? {
    if (nodes.isEmpty() || viewportSize.width <= 0 || viewportSize.height <= 0) {
        return null
    }
    val bounds = worldBounds ?: return null
    val minX = bounds.minX
    val maxX = bounds.maxX
    val minY = bounds.minY
    val maxY = bounds.maxY
    val xSpan = max(MIN_WORLD_SPAN, maxX - minX)
    val ySpan = max(MIN_WORLD_SPAN, maxY - minY)
    val safeWidth = viewportSize.width.toFloat()
    val safeHeight = viewportSize.height.toFloat()
    val availableWidth = safeWidth * (1f - VIEWPORT_HORIZONTAL_MARGIN * 2)
    val availableHeight = safeHeight * (1f - VIEWPORT_VERTICAL_MARGIN * 2)
    val pixelsPerUnit = min(availableWidth / xSpan.toFloat(), availableHeight / ySpan.toFloat())
        .coerceAtLeast(MIN_PIXELS_PER_UNIT)
    return ViewportProjection(
        worldCenterX = (minX + maxX) / 2,
        worldCenterY = (minY + maxY) / 2,
        pixelsPerUnit = pixelsPerUnit,
    )
}

internal fun ViewportNode.toViewportPosition(viewportSize: IntSize, projection: ViewportProjection): Offset {
    val center = Offset(viewportSize.width / 2f, viewportSize.height / 2f)
    return Offset(
        x = center.x + ((coordinates[0] - projection.worldCenterX) * projection.pixelsPerUnit).toFloat(),
        y = center.y - ((coordinates[1] - projection.worldCenterY) * projection.pixelsPerUnit).toFloat(),
    )
}

private const val VIEWPORT_HORIZONTAL_MARGIN = 0.12f
private const val VIEWPORT_VERTICAL_MARGIN = 0.14f
private const val MIN_WORLD_SPAN = 1.0
private const val MIN_PIXELS_PER_UNIT = 1f
