/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.properties

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.properties.OccupiesSpaceProperty
import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * The [node] occupies a rectangular width x height area.
 */
class RectangularArea<T>(
    /**
     * The environment in which [node] is moving.
     */
    val environment: Physics2DEnvironment<T>,
    override val node: Node<T>,
    /**
     * The rectangle width.
     */
    val width: Double,
    /**
     * The rectangle height.
     */
    val height: Double,
) : AbstractNodeProperty<T>(node), OccupiesSpaceProperty<T, Euclidean2DPosition, Euclidean2DTransformation> {

    override val shape: Euclidean2DShape = environment.shapeFactory.rectangle(width, height)

    override fun cloneOnNewNode(node: Node<T>) = RectangularArea(environment, node, width, height)

    override fun toString() = "${super.toString()}[width=$width, height=$height]"
}
