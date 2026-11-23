/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.inspector

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.InspectorState
import it.unibo.alchemist.boundary.composeui.view.theme.Outline
import it.unibo.alchemist.boundary.composeui.view.theme.PrimaryAccent

private const val MAX_SHARE = 0.7f
private const val RESIZE_LABEL = "Resize the inspector"

private val narrowest = 240.dp
private val handleWidth = 10.dp
private val gripSize = 4.dp
private val gripLength = 40.dp

/**
 * The [NodeInspector], [width] wide, with a handle on its left edge that resizes it.
 * The width stays between 240 dp and 70% of [availableWidth]; dragging asks for a new one through [onWidthChange].
 */
@Composable
internal fun ResizableInspector(
    inspector: InspectorState,
    width: Dp,
    availableWidth: Dp,
    onWidthChange: (Dp) -> Unit,
    actions: InspectorActions,
    modifier: Modifier = Modifier,
) {
    val allowed = allowedWidths(availableWidth)
    val shownWidth = shownInspectorWidth(width, availableWidth)
    val currentWidth by rememberUpdatedState(shownWidth)
    val currentAllowed by rememberUpdatedState(allowed)
    val density = LocalDensity.current
    val interactions = remember { MutableInteractionSource() }
    val hovered by interactions.collectIsHoveredAsState()
    val dragged by interactions.collectIsDraggedAsState()
    Box(modifier = modifier.width(shownWidth)) {
        NodeInspector(inspector = inspector, actions = actions, modifier = Modifier.fillMaxHeight())
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(handleWidth)
                .fillMaxHeight()
                .semantics { contentDescription = RESIZE_LABEL }
                .hoverable(interactions)
                .draggable(
                    orientation = Orientation.Horizontal,
                    interactionSource = interactions,
                    state = rememberDraggableState { deltaPx ->
                        // The handle is on the left, so dragging left widens the inspector.
                        onWidthChange((currentWidth - with(density) { deltaPx.toDp() }).coerceIn(currentAllowed))
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(width = gripSize, height = gripLength)
                    .background(
                        color = if (hovered || dragged) PrimaryAccent else Outline,
                        shape = RoundedCornerShape(gripSize / 2),
                    ),
            )
        }
    }
}

/** The width the inspector takes when asked for [width] in [availableWidth]: between 240 dp and 70% of it. */
internal fun shownInspectorWidth(width: Dp, availableWidth: Dp): Dp = width.coerceIn(allowedWidths(availableWidth))

private fun allowedWidths(availableWidth: Dp): ClosedRange<Dp> {
    val narrowestFitting = minOf(narrowest, availableWidth)
    return narrowestFitting..maxOf(narrowestFitting, availableWidth * MAX_SHARE)
}
