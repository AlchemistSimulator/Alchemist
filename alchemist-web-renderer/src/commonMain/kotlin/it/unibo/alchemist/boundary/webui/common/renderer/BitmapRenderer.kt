/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.common.renderer

import it.unibo.alchemist.boundary.webui.common.model.surrogate.EnvironmentSurrogate
import it.unibo.alchemist.boundary.webui.common.model.surrogate.NodeSurrogate
import it.unibo.alchemist.boundary.webui.common.model.surrogate.PositionSurrogate
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.Bitmap32
import korlibs.image.bitmap.context2d
import korlibs.image.color.Colors
import korlibs.image.vector.Context2d
import korlibs.math.geom.Point

/**
 * This implementation renders a [Bitmap].
 * The rendering works on 2D environments only as it uses a [Context2d].
 * @param <TS> the type of the concentration surrogate.
 */
class BitmapRenderer<in TS : Any, in PS : PositionSurrogate> : Renderer<TS, PS, Bitmap> {

    companion object {
        private const val defaultNodeRadius = 0.1f
        private const val defaultHeight = 1000
        private const val defaultWidth = 1000
        private const val defaultScaleFactor = 25
    }

    /**
     * Renders the environment.
     * @param environmentSurrogate the
     * [it.unibo.alchemist.boundary.webui.common.model.surrogate.EnvironmentSurrogate] to render.
     * @return a [Bitmap] representing the environment.
     */
    override fun render(environmentSurrogate: EnvironmentSurrogate<TS, PS>): Bitmap {
        require(environmentSurrogate.dimensions == 2)
        return Bitmap32(defaultWidth, defaultHeight, premultiplied = false).context2d {
            cartesianPlane()
            translate(width / 2.0, height / 2.0)
            scale(defaultScaleFactor, defaultScaleFactor)
            environmentSurrogate.nodes.forEach {
                alchemistNode(it, defaultNodeRadius)
            }
        }
    }

    /**
     * Draws the two cartesian axis on the [Context2d].
     */
    private fun Context2d.cartesianPlane() {
        fill(Colors.WHITE)
        beginPath()
        moveTo((width / 2).toDouble(), 0.0)
        lineTo((width / 2).toDouble(), height.toDouble())
        moveTo(0.0, (height / 2).toDouble())
        lineTo(width.toDouble(), (height / 2).toDouble())
        stroke()
    }

    /**
     * Draws a [it.unibo.alchemist.boundary.webui.common.model.surrogate.NodeSurrogate]
     * on the [Context2d].
     *
     * @param node the node surrogate to draw.
     * @param radius the radius of the node.
     */
    private fun Context2d.alchemistNode(node: NodeSurrogate<TS, PS>, radius: Float) {
        beginPath()
        circle(node.position.toPoint(), radius)
        stroke()
    }

    private fun PositionSurrogate.toPoint(): Point {
        require(dimensions == 2)
        return Point(coordinates[0], coordinates[1])
    }
}
