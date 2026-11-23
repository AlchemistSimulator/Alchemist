/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.viewport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.view.theme.HelpPanel
import it.unibo.alchemist.boundary.composeui.view.theme.SecondaryAccent

private val shortcuts = listOf(
    "Click" to "Inspect a node",
    "Drag" to "Select an area",
    "Ctrl + click" to "Add/remove a node to/from the selection",
    "Shift + drag" to "Add an area to the selection",
    "Ctrl + drag" to "Move the selected nodes",
    "Right-drag" to "Pan",
    "Wheel" to "Zoom",
    "WASD / arrows" to "Pan",
    "Enter / E" to "Inspect the node closest to the pointer",
    "Backspace" to "Clear the selection",
    "Space" to "Play / pause",
    "C" to "Show or hide the controls",
    "H" to "Show or hide this help",
    "Ctrl + Q" to "Quit",
)

private val keyColumnWidth = 110.dp

/**
 * The help panels: the list of shortcuts if [expanded], and a note if there are no nodes.
 */
@Composable
internal fun ViewportGestureHint(hasNodes: Boolean, expanded: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (expanded) {
            HintPanel {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    shortcuts.forEach { (keys, action) ->
                        Row {
                            Text(
                                text = keys,
                                modifier = Modifier.width(keyColumnWidth),
                                style = MaterialTheme.typography.caption,
                                color = SecondaryAccent,
                            )
                            Text(text = action, style = MaterialTheme.typography.caption)
                        }
                    }
                }
            }
        }
        if (!hasNodes) {
            HintPanel {
                Text(text = "No nodes to display", style = MaterialTheme.typography.caption)
            }
        }
    }
}

@Composable
private fun HintPanel(content: @Composable () -> Unit) {
    Surface(color = HelpPanel, shape = RoundedCornerShape(8.dp), elevation = 0.dp) {
        Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            content()
        }
    }
}
