/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.nodes.NodeWithShape

/**
 * A [it.unibo.alchemist.model.interfaces.Node] with a circle shape meant to be added to a
 * [it.unibo.alchemist.model.interfaces.environments.PhysicsEnvironment].
 */
open class CircleNode<T> @JvmOverloads constructor(
    env: Physics2DEnvironment<T>,
    radius: Double = 5.0
) : AbstractNode<T>(env), NodeWithShape<T, Euclidean2DPosition, Euclidean2DTransformation> {

    /**
     * {@inheritDoc}.
     */
    final override val shape = env.shapeFactory.circle(radius)

    /**
     * Returns null because T is unknown.
     */
    override fun createT() = TODO()
}
