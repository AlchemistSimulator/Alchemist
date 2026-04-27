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
import androidx.compose.material.Surface as ComposeSurface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.view.theme.Surface as SurfaceColor

@Composable
internal fun ComponentSurface(
    color: Color = SurfaceColor.copy(alpha = componentSurfaceAlpha),
    modifier: Modifier = Modifier,
    shape: Shape = componentShape,
    content: @Composable () -> Unit,
) {
    ComposeSurface(
        modifier = modifier,
        color = color,
        shape = shape,
        elevation = 0.dp,
    ) {
        content()
    }
}

internal val componentShape = RoundedCornerShape(8.dp)
internal val pillShape = RoundedCornerShape(999.dp)
internal val componentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)

private const val componentSurfaceAlpha = 0.78f
