/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary

import javafx.scene.canvas.Canvas
import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle

/**
 * Clears a given canvas.
 */
fun Canvas.clear() = graphicsContext2D.clearRect(0.0, 0.0, width, height)

/**
 * Returns a command for drawing the given rectangle on the caller canvas.
 */
fun Canvas.createDrawRectangleCommand(rectangle: Rectangle, colour: Paint): () -> Unit = {
    graphicsContext2D.fill = colour
    graphicsContext2D.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height)
}
