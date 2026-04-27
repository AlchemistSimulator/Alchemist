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
    val colors = accent.toTransportButtonColors(enabled)
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = componentShape,
        elevation = ButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colors.background,
            contentColor = colors.content,
            disabledBackgroundColor = Outline.copy(alpha = 0.65f),
            disabledContentColor = TextSecondary,
        ),
        contentPadding = transportButtonPadding,
    ) {
        Text(text = label)
    }
}

private data class TransportButtonPalette(val background: Color, val content: Color)

private fun Color.toTransportButtonColors(enabled: Boolean): TransportButtonPalette =
    TransportButtonPalette(
        background = copy(alpha = if (enabled) EnabledButtonAlpha else DisabledButtonAlpha),
        content = if (luminance() > ACCENT_LUMINANCE_THRESHOLD) TextPrimary else Surface,
    )

private const val ACCENT_LUMINANCE_THRESHOLD = 0.35f
private const val EnabledButtonAlpha = 0.92f
private const val DisabledButtonAlpha = 0.28f
private val transportButtonPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
