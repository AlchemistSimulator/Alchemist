/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.composeui.view.viewport

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.view.components.componentShape
import it.unibo.alchemist.boundary.composeui.view.theme.Danger
import it.unibo.alchemist.boundary.composeui.view.theme.FloatingPanel
import it.unibo.alchemist.boundary.composeui.view.theme.Outline
import it.unibo.alchemist.boundary.composeui.view.theme.SecondaryAccent

/**
 * Draws the buttons of [actions] over the viewport.
 * They take no input, not even through a Material surface, which would block it:
 * the viewport handles presses on them, so that Move and Clone can start a drag.
 */
@Composable
internal fun SelectionActionsOverlay(actions: SelectionActions) {
    val density = LocalDensity.current
    actions.buttons.forEach { (action, rect) ->
        Box(
            modifier = Modifier
                .offset { IntOffset(rect.left.toInt(), rect.top.toInt()) }
                .size(with(density) { rect.width.toDp() }, with(density) { rect.height.toDp() })
                .background(color = FloatingPanel, shape = componentShape)
                .border(width = 1.dp, color = Outline, shape = componentShape)
                .semantics { contentDescription = action.description },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = action.label,
                style = MaterialTheme.typography.caption,
                color = if (action == SelectionAction.DELETE) Danger else SecondaryAccent,
            )
        }
    }
}
