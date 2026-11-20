/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.viewport

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.semantics.Role

/**
 * Focuses the viewport, which keeps the focus whenever no text box needs it:
 * keyboard shortcuts only reach the UI through the focused element.
 */
internal val LocalViewportFocus = staticCompositionLocalOf { FocusRequester() }

/**
 * Like [clickable], but gives the focus back to the viewport after each click,
 * so that Space keeps playing and pausing instead of pressing this control again.
 */
@Composable
internal fun Modifier.clickableKeepingShortcuts(
    onClickLabel: String,
    role: Role = Role.Button,
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier {
    val viewportFocus = LocalViewportFocus.current
    return clickable(enabled = enabled, onClickLabel = onClickLabel, role = role) {
        onClick()
        viewportFocus.requestFocus()
    }
}
