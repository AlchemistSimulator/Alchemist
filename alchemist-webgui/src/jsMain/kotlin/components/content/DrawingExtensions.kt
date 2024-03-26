/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package components.content

import components.content.shared.CommonProperties.Observables.nodesRadius
import components.content.shared.CommonProperties.Observables.scaleTranslationStore
import components.content.shared.CommonProperties.RenderProperties.DEFAULT_HEIGHT
import components.content.shared.CommonProperties.RenderProperties.DEFAULT_NODE_COLOR
import components.content.shared.CommonProperties.RenderProperties.DEFAULT_WIDTH
import org.w3c.dom.CanvasRenderingContext2D
import kotlin.math.PI

/**
 * Object containing properties related to grid rendering.
 */
private object GridProperties {
    const val LINE_WIDTH = 1.0
    const val STROKE_COLOR = "#ADADAD"
    const val ORIGIN_COLOR = "#C22F2F"
    const val CELL_SIZE = 10
}

/**
 * Draws a node on the canvas at the specified position with the given color.
 *
 * @param position the position of the node on the canvas
 * @param color the color of the node
 */
fun CanvasRenderingContext2D.drawNode(
    position: Pair<Double, Double>,
    color: String = DEFAULT_NODE_COLOR,
) {
    // Begin drawing a path for the node
    beginPath()
    // Draw a circle at the specified position with radius based on the node radius and scale
    arc(
        position.first,
        position.second,
        nodesRadius.value * 1 / scaleTranslationStore.getState().scale,
        0.0,
        2 * PI,
        false,
    )
    // Set the fill style to the specified color and fill the node
    fillStyle = color
    fill()
    // Close the path
    closePath()
}

/**
 * Redraws the nodes on the canvas with the specified positions.
 *
 * @param nodes the list of node positions
 * @param scale the scale of the canvas
 * @param translation the translation of the canvas
 */
fun CanvasRenderingContext2D.redrawNodes(
    nodes: List<Pair<Double, Double>>,
    scale: Double = scaleTranslationStore.getState().scale,
    translation: Pair<Double, Double> = scaleTranslationStore.getState().translate,
) {
    // Calculate the scaled translation
    val translationScaled = Pair(
        translation.first * 1 / scale,
        translation.second * 1 / scale,
    )

    // Scale and translate the canvas
    scale(scale, scale)
    translate(translationScaled.first, translationScaled.second)

    // Clear the canvas
    clearRect(
        -translationScaled.first,
        -translationScaled.second,
        DEFAULT_WIDTH,
        DEFAULT_HEIGHT,
    )

    // Draw the grid
    drawGrid()
    // Draw the origin of the canvas
    drawNode(Pair(0.0, 0.0), GridProperties.ORIGIN_COLOR)

    // Draw each node at its position
    nodes.forEach {
        drawNode(Pair(it.first, it.second))
    }

    // Reset the canvas transformation
    setTransform(1.0, 0.0, 0.0, 1.0, 0.0, 0.0)
}

/**
 * Draws the grid lines on the canvas.
 */
fun CanvasRenderingContext2D.drawGrid() {
    // Set the line width and stroke color
    lineWidth = GridProperties.LINE_WIDTH / scaleTranslationStore.getState().scale
    strokeStyle = GridProperties.STROKE_COLOR

    // Define the boundaries of the grid
    val left = -DEFAULT_WIDTH
    val top = -DEFAULT_HEIGHT
    val right = DEFAULT_WIDTH
    val bottom = DEFAULT_HEIGHT

    // Clear the grid area
    clearRect(left, top, right - left, bottom - top)
    // Begin drawing the grid
    beginPath()

    // Draw vertical grid lines
    generateSequence(left.toInt()) { it + GridProperties.CELL_SIZE }
        .takeWhile { it < right.toInt() + GridProperties.CELL_SIZE }
        .map(Int::toDouble)
        .forEach {
            moveTo(it, top)
            lineTo(it, bottom)
        }

    // Draw horizontal grid lines
    generateSequence(top.toInt()) { it + GridProperties.CELL_SIZE }
        .takeWhile { it < bottom.toInt() + GridProperties.CELL_SIZE }
        .map(Int::toDouble)
        .forEach {
            moveTo(left, it)
            lineTo(right, it)
        }

    // Stroke the grid lines
    stroke()
    closePath()
}
