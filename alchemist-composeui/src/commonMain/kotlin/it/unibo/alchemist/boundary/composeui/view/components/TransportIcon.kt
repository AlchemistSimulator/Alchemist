/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope

private const val NEAR = 0.15f
private const val FAR = 0.85f
private const val MIDDLE = 0.5f
private const val BAR_WIDTH = 0.22f
private const val STEP_TIP = 0.66f

/**
 * The icons of the transport buttons, drawn as plain shapes.
 */
internal enum class TransportIcon {
    PLAY,
    PAUSE,
    STEP,
    ;

    /** Draws this icon with [color], filling [scope]. */
    fun draw(scope: DrawScope, color: Color): Unit = with(scope) {
        when (this@TransportIcon) {
            PLAY -> drawPath(triangle(tip = FAR), color)
            PAUSE -> {
                drawRect(color, topLeft = at(NEAR, NEAR), size = sized(BAR_WIDTH, FAR - NEAR))
                drawRect(color, topLeft = at(FAR - BAR_WIDTH, NEAR), size = sized(BAR_WIDTH, FAR - NEAR))
            }
            STEP -> {
                drawPath(triangle(tip = STEP_TIP), color)
                drawRect(color, topLeft = at(FAR - BAR_WIDTH / 2, NEAR), size = sized(BAR_WIDTH / 2, FAR - NEAR))
            }
        }
    }

    private fun DrawScope.at(x: Float, y: Float): Offset = Offset(size.width * x, size.height * y)

    private fun DrawScope.sized(width: Float, height: Float): Size = Size(size.width * width, size.height * height)

    private fun DrawScope.triangle(tip: Float): Path = Path().apply {
        moveTo(size.width * NEAR, size.height * NEAR)
        lineTo(size.width * NEAR, size.height * FAR)
        lineTo(size.width * tip, size.height * MIDDLE)
        close()
    }
}
