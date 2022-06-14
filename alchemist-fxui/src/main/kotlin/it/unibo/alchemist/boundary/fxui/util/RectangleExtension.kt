/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.util

import it.unibo.alchemist.boundary.ui.api.Wormhole2D
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position2D
import javafx.scene.shape.Rectangle
import java.awt.Point

/**
 * Utilities for [Rectangle].
 */
object RectangleExtension {

    /**
     * Returns the nodes intersecting with the caller rectangle.
     */
    fun <T, P : Position2D<P>> Rectangle.intersectingNodes(
        nodes: Map<Node<T>, P>,
        wormhole: Wormhole2D<P>
    ): Map<Node<T>, P> = nodes.filterValues { wormhole.getViewPoint(it) in this }

    /**
     * Returns whether the [Rectangle] [this] contains [point].
     */
    operator fun Rectangle.contains(point: Point): Boolean =
        x <= point.x.toDouble() && point.x.toDouble() <= x + width &&
            y <= point.y.toDouble() && point.y.toDouble() <= y + height
}
