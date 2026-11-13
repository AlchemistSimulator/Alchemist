/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.viewport

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.view.theme.SurfaceStrong

/** A short guide to the mouse controls. */
@Composable
internal fun ViewportGestureHint(hasNodes: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = SurfaceStrong.copy(alpha = 0.88f),
        shape = RoundedCornerShape(8.dp),
        elevation = 0.dp,
    ) {
        Text(
            text = if (hasNodes) {
                "Click to inspect · drag to select · Ctrl-drag selected nodes · right-drag to pan · wheel to zoom"
            } else {
                "No nodes to display"
            },
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.caption,
        )
    }
}
