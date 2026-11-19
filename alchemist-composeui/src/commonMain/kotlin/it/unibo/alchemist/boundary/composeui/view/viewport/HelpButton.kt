/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.viewport

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.view.theme.HelpPanel

/** The size of the help button. */
internal val HelpButtonSize = 32.dp

/** A round **?** button that shows or hides the shortcuts. */
@Composable
internal fun HelpButton(expanded: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .size(HelpButtonSize)
            .clickableKeepingShortcuts(
                onClickLabel = if (expanded) "Hide shortcuts (H)" else "Show shortcuts (H)",
                onClick = onToggle,
            ),
        color = HelpPanel,
        shape = CircleShape,
        elevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = "?", fontWeight = FontWeight.Bold)
        }
    }
}
