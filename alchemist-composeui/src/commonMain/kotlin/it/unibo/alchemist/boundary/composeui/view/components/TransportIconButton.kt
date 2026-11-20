/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.view.theme.Outline
import it.unibo.alchemist.boundary.composeui.view.theme.Surface as SurfaceColor
import it.unibo.alchemist.boundary.composeui.view.theme.TextSecondary
import it.unibo.alchemist.boundary.composeui.view.viewport.clickableKeepingShortcuts

private const val DISABLED_ALPHA = 0.5f

/** The size of a transport button. */
internal val TransportButtonSize = 36.dp

private val iconSize = 14.dp

/**
 * A square button showing a transport [icon], described by [label] for accessibility.
 */
@Composable
internal fun TransportIconButton(
    icon: TransportIcon,
    label: String,
    enabled: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .size(TransportButtonSize)
            .semantics { contentDescription = label }
            .clickableKeepingShortcuts(enabled = enabled, onClickLabel = label, onClick = onClick),
        color = if (enabled) accent else Outline.copy(alpha = DISABLED_ALPHA),
        shape = componentShape,
        elevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(iconSize)) {
                icon.draw(this, if (enabled) SurfaceColor else TextSecondary)
            }
        }
    }
}
