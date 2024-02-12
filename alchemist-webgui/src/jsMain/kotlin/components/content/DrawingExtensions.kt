/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package components.content

import components.content.shared.CommonProperties
import org.w3c.dom.CanvasRenderingContext2D
import stores.EnvironmentStore
import kotlin.math.PI

private object GridProperties {
    const val LINE_WIDTH = 1.0
    const val STROKE_COLOR = "#ADADAD"
    const val CELL_SIZE = 10
}

fun CanvasRenderingContext2D.drawNode(
    position: Pair<Double, Double>,
    color: String = CommonProperties.RenderProperties.DEFAULT_NODE_COLOR,
) {
    beginPath()
    arc(
        position.first,
        position.second,
        CommonProperties.Observables.nodesRadius.value * 1 /
            CommonProperties.Observables.scaleTranslationStore.getState().scale,
        0.0,
        2 * PI,
        false,
    )
    fillStyle = color
    fill()
    closePath()
}

fun CanvasRenderingContext2D.redrawNodes(
    scale: Double = CommonProperties.Observables.scaleTranslationStore.getState().scale,
    translation: Pair<Double, Double> = CommonProperties.Observables.scaleTranslationStore.getState().translate,
) {
    println("Function[Redraw]: Redrawing function")
    val translationScaled = Pair(
        translation.first * 1 / scale,
        translation.second * 1 / scale,
    )

    scale(scale, scale)
    translate(translationScaled.first, translationScaled.second)

    clearRect(
        -translationScaled.first,
        -translationScaled.second,
        CommonProperties.RenderProperties.DEFAULT_WIDTH,
        CommonProperties.RenderProperties.DEFAULT_HEIGHT,
    )

    drawGrid()

    beginPath()
    moveTo(0.0, 0.0)
    arc(
        0.0,
        0.0,
        CommonProperties.Observables.nodesRadius.value * 1 /
            CommonProperties.Observables.scaleTranslationStore.getState().scale,
        0.0,
        2 * PI,
        false,
    )
    fillStyle = "#00ff00"
    fill()
    closePath()

    EnvironmentStore.store.getState().nodes.forEach {
        drawNode(Pair(it.position.coordinates[0], it.position.coordinates[1])) // randomColor())
    }

    setTransform(1.0, 0.0, 0.0, 1.0, 0.0, 0.0)
}

fun CanvasRenderingContext2D.drawGrid() {
    lineWidth = GridProperties.LINE_WIDTH / CommonProperties.Observables.scaleTranslationStore.getState().scale
    strokeStyle = GridProperties.STROKE_COLOR

    val left = -CommonProperties.RenderProperties.DEFAULT_WIDTH
    val top = -CommonProperties.RenderProperties.DEFAULT_HEIGHT
    val right = CommonProperties.RenderProperties.DEFAULT_WIDTH
    val bottom = CommonProperties.RenderProperties.DEFAULT_HEIGHT

    clearRect(left, top, right - left, bottom - top)
    beginPath()

    generateSequence(left.toInt()) { it + GridProperties.CELL_SIZE }
        .takeWhile { it < right.toInt() + GridProperties.CELL_SIZE }
        .map(Int::toDouble)
        .forEach {
            moveTo(it, top)
            lineTo(it, bottom)
        }

    generateSequence(top.toInt()) { it + GridProperties.CELL_SIZE }
        .takeWhile { it < bottom.toInt() + GridProperties.CELL_SIZE }
        .map(Int::toDouble)
        .forEach {
            moveTo(left, it)
            lineTo(right, it)
        }

    stroke()
    closePath()
}
