/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.view.theme.Outline
import it.unibo.alchemist.boundary.composeui.view.theme.Surface
import it.unibo.alchemist.boundary.composeui.view.theme.TextPrimary
import it.unibo.alchemist.boundary.composeui.view.theme.TextSecondary

@Composable
internal fun TransportButton(label: String, enabled: Boolean, accent: Color, onClick: () -> Unit) {
    val buttonContentColor = if (accent.luminance() > ACCENT_LUMINANCE_THRESHOLD) TextPrimary else Surface
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        elevation = ButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = accent.copy(alpha = if (enabled) 0.92f else 0.28f),
            contentColor = buttonContentColor,
            disabledBackgroundColor = Outline.copy(alpha = 0.65f),
            disabledContentColor = TextSecondary,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(text = label)
    }
}

private const val ACCENT_LUMINANCE_THRESHOLD = 0.35f
