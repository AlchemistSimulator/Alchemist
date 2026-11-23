/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.composeui.view.viewport

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap

/**
 * Where the [SelectionAction] buttons are on screen: in a row above the top right corner of the selection,
 * or below it if there is no room above, so that they hide no node.
 *
 * @property bounds the box around the selected nodes.
 * @property buttons where each button is.
 */
@Immutable
internal data class SelectionActions(val bounds: Rect, val buttons: ImmutableMap<SelectionAction, Rect>) {
    /** Where the pointer keeps the buttons shown: the selection, the buttons, and the gap between them. */
    val hoverArea: Rect = buttons.values.fold(bounds) { area, button ->
        Rect(
            left = minOf(area.left, button.left),
            top = minOf(area.top, button.top),
            right = maxOf(area.right, button.right),
            bottom = maxOf(area.bottom, button.bottom),
        )
    }

    /** The button at [position], if any. */
    fun hit(position: Offset): SelectionAction? = buttons.entries.firstOrNull { it.value.contains(position) }?.key

    internal companion object {
        /**
         * The buttons for nodes drawn at [nodeCenters], each [buttonSize] large and [gap] apart,
         * around a selection box [margin] larger than the nodes; `null` if no selected node is on screen.
         */
        fun of(nodeCenters: List<Offset>, margin: Float, buttonSize: Size, gap: Float): SelectionActions? =
            nodeCenters.takeIf { it.isNotEmpty() }?.let { centers ->
                val bounds = Rect(
                    left = centers.minOf(Offset::x) - margin,
                    top = centers.minOf(Offset::y) - margin,
                    right = centers.maxOf(Offset::x) + margin,
                    bottom = centers.maxOf(Offset::y) + margin,
                )
                val top = (bounds.top - gap - buttonSize.height).takeIf { it >= 0f } ?: (bounds.bottom + gap)
                val actions = SelectionAction.entries
                val left = bounds.right - actions.size * buttonSize.width - (actions.size - 1) * gap
                val buttons = actions.mapIndexed { index, action ->
                    action to Rect(Offset(left + index * (buttonSize.width + gap), top), buttonSize)
                }
                SelectionActions(bounds, buttons.toMap().toImmutableMap())
            }
    }
}
