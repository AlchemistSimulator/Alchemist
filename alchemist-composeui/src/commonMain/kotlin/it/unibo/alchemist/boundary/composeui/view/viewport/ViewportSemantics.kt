/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.viewport

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import it.unibo.alchemist.boundary.composeui.util.DisplayValues.formatFixed

/**
 * Describes the viewport to screen readers and exposes its controls as accessibility actions.
 */
internal fun Modifier.viewportSemantics(
    scene: ViewportScene,
    selectedNodeCount: Int,
    zoom: Float,
    onClearSelection: () -> Boolean,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onPan: (Offset) -> Unit,
): Modifier = semantics {
    contentDescription = buildString {
        append("Simulation viewport with ${scene.nodes.size.counted("node")}. ")
        append("${selectedNodeCount.counted("node")} selected. ")
        append(if (scene.showLinks) "Links are visible." else "Links are hidden.")
    }
    stateDescription = "Zoom ${zoom.toDouble().formatFixed(2)}, $selectedNodeCount selected"
    role = Role.Image
    customActions = buildList {
        if (selectedNodeCount > 0) {
            add(CustomAccessibilityAction("Clear node selection", onClearSelection))
        }
        add(alwaysHandled("Zoom in", onZoomIn))
        add(alwaysHandled("Zoom out", onZoomOut))
        PanDirection.entries.forEach { direction -> add(alwaysHandled(direction.label) { onPan(direction.step) }) }
    }
}

private fun alwaysHandled(label: String, run: () -> Unit) = CustomAccessibilityAction(label) {
    run()
    true
}

private fun Int.counted(noun: String): String = if (this == 1) "$this $noun" else "$this ${noun}s"
