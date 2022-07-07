/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.properties

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.properties.OccupiesSpaceProperty
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation

/**
 * The [node] occupies a circular space with the provided radius.
 */
class CircularArea<T> @JvmOverloads constructor(
    /**
     * The environment in witch the node moves.
     */
    val environment: Physics2DEnvironment<T>,
    override val node: Node<T>,
    /**
     * The radius of this circular area.
     */
    val radius: Double = 0.3,
) : AbstractNodeProperty<T>(node), OccupiesSpaceProperty<T, Euclidean2DPosition, Euclidean2DTransformation> {

    override val shape: Euclidean2DShape = environment.shapeFactory.circle(radius)

    override fun cloneOnNewNode(node: Node<T>) = CircularArea(environment, node, shape.radius)

    override fun toString() = "${super.toString()}[radius=$radius]"
}
