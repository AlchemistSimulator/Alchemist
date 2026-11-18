/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.root

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

private const val MIN_SCALE = 0.5f
private const val MAX_SCALE = 3f

private val referenceWidth = 1920.dp
private val referenceHeight = 1080.dp

/**
 * Shows [content] as if the window were 1920×1080 dp, scaled to fit the actual window.
 * The UI then keeps its proportions at any window size and screen density.
 */
@Composable
internal fun ScaledToWindow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    BoxWithConstraints(modifier) {
        val density = LocalDensity.current
        val scale = minOf(maxWidth / referenceWidth, maxHeight / referenceHeight).coerceIn(MIN_SCALE, MAX_SCALE)
        CompositionLocalProvider(LocalDensity provides Density(density.density * scale, density.fontScale)) {
            content()
        }
    }
}
