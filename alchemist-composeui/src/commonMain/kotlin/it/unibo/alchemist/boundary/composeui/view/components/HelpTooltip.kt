/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import it.unibo.alchemist.boundary.composeui.view.theme.Outline
import it.unibo.alchemist.boundary.composeui.view.theme.SecondaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.SurfaceStrong
import it.unibo.alchemist.boundary.composeui.view.viewport.clickableKeepingShortcuts

private const val HELP_LABEL = "Help"

private val badgeSize = 18.dp
private val tooltipMaxWidth = 280.dp
private val tooltipPadding = 10.dp

/**
 * A small "?" that shows [text] in a tooltip below it: a click opens it, another closes it,
 * like the shortcuts help.
 */
@Composable
internal fun HelpTooltip(text: String, modifier: Modifier = Modifier) {
    var shown by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .size(badgeSize)
            .clip(CircleShape)
            .border(width = 1.dp, color = Outline, shape = CircleShape)
            .semantics { contentDescription = HELP_LABEL }
            .clickableKeepingShortcuts(onClickLabel = HELP_LABEL) { shown = !shown },
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "?", style = MaterialTheme.typography.caption, color = SecondaryAccent)
        if (shown) {
            // Not dismissed by outside clicks: a click on "?" would close it, then open it again.
            Popup(
                popupPositionProvider = BelowRightAligned,
                properties = PopupProperties(dismissOnClickOutside = false),
            ) {
                Text(
                    text = text,
                    modifier = Modifier
                        .widthIn(max = tooltipMaxWidth)
                        .background(color = SurfaceStrong, shape = componentShape)
                        .border(width = 1.dp, color = Outline, shape = componentShape)
                        .padding(tooltipPadding),
                    style = MaterialTheme.typography.body2,
                )
            }
        }
    }
}

/** Under the anchor, with their right edges aligned, so that it stays inside a panel on the right. */
private object BelowRightAligned : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset = IntOffset(
        x = (anchorBounds.right - popupContentSize.width).coerceAtLeast(0),
        y = anchorBounds.bottom,
    )
}
