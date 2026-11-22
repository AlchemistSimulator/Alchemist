/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.controls

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.view.components.TransportButtonSize
import it.unibo.alchemist.boundary.composeui.view.components.componentShape
import it.unibo.alchemist.boundary.composeui.view.theme.Outline
import it.unibo.alchemist.boundary.composeui.view.theme.PrimaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.SecondaryAccent
import it.unibo.alchemist.boundary.composeui.view.viewport.clickableKeepingShortcuts

private const val GEAR_TEETH = 8
private const val GEAR_BODY = 0.7f
private const val GEAR_RING = 0.55f
private const val GEAR_TOOTH_WIDTH = 0.3f
private const val FULL_TURN = 360f
private const val SETTINGS_LABEL = "Display settings"

private val gearSize = 20.dp

/**
 * The ⚙ button, which shows or hides the [SettingsPanel]; its border is highlighted while the panel is [open].
 */
@Composable
internal fun SettingsSection(open: Boolean, actions: DockActions) {
    Box(
        modifier = Modifier
            .size(TransportButtonSize)
            .clip(componentShape)
            .border(width = 1.dp, color = if (open) PrimaryAccent else Outline, shape = componentShape)
            .semantics { contentDescription = SETTINGS_LABEL }
            .clickableKeepingShortcuts(onClickLabel = SETTINGS_LABEL, onClick = actions.onToggleSettings),
        contentAlignment = Alignment.Center,
    ) {
        Gear()
    }
}

@Composable
private fun Gear() {
    Canvas(modifier = Modifier.size(gearSize)) {
        val outer = size.minDimension / 2
        val body = outer * GEAR_BODY
        val ring = body * GEAR_RING
        val toothWidth = outer * GEAR_TOOTH_WIDTH
        repeat(GEAR_TEETH) { tooth ->
            rotate(FULL_TURN / GEAR_TEETH * tooth) {
                drawRect(
                    color = SecondaryAccent,
                    topLeft = Offset(center.x - toothWidth / 2, center.y - outer),
                    size = Size(toothWidth, outer - body + ring / 2),
                )
            }
        }
        drawCircle(color = SecondaryAccent, radius = body - ring / 2, style = Stroke(width = ring))
    }
}
