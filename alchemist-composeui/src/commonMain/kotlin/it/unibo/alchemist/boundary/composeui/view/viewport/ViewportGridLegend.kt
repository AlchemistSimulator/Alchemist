/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.viewport

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.util.DisplayValues.formatFixed
import it.unibo.alchemist.boundary.composeui.view.theme.SurfaceStrong
import it.unibo.alchemist.boundary.composeui.view.theme.TextPrimary

/** The world length of one grid step, with a scale bar. Hidden until the viewport has a size and a projection. */
@Composable
internal fun ViewportGridLegend(
    viewportSize: IntSize,
    zoom: Float,
    projection: ViewportProjection?,
    modifier: Modifier = Modifier,
) {
    val spacing = remember(viewportSize, zoom) {
        GridSpacing.of(viewportSize.width.toFloat(), viewportSize.height.toFloat(), zoom)
    }
    if (projection != null && viewportSize.width > 0 && viewportSize.height > 0) {
        Surface(
            modifier = modifier,
            color = SurfaceStrong.copy(alpha = 0.88f),
            shape = RoundedCornerShape(8.dp),
            elevation = 0.dp,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(12.dp),
            ) {
                Text(
                    text = spacing.worldStep(projection).formatFixed(2),
                    style = MaterialTheme.typography.caption,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                val scaleWidth = with(LocalDensity.current) { spacing.pixels.toDp().coerceIn(20.dp, 120.dp) }
                Canvas(modifier = Modifier.size(scaleWidth, 10.dp)) {
                    drawScale()
                }
            }
        }
    }
}

private fun DrawScope.drawScale() {
    val strokeWidth = 1.5.dp.toPx()
    val color = TextPrimary.copy(alpha = 0.7f)
    val centerY = size.height / 2f
    val tickHeight = 4.dp.toPx()
    val left = strokeWidth / 2
    val right = size.width - strokeWidth / 2
    listOf(
        Offset(0f, centerY) to Offset(size.width, centerY),
        Offset(left, centerY - tickHeight) to Offset(left, centerY + tickHeight),
        Offset(right, centerY - tickHeight) to Offset(right, centerY + tickHeight),
    ).forEach { (start, end) ->
        drawLine(color = color, start = start, end = end, strokeWidth = strokeWidth, cap = StrokeCap.Round)
    }
}
