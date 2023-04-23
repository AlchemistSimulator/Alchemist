/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geometry.euclidean2d

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.InfluenceSphere
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShape

/**
 * A sphere of influence in the Euclidean world.
 *
 * @param environment
 *          the environment where this sphere of influence is.
 * @param owner
 *          the node who owns this sphere of influence.
 * @param shape
 *          the shape of this sphere of influence
 */
open class InfluenceSphere2D<T>(
    private val environment: Physics2DEnvironment<T>,
    private val owner: Node<T>,
    private val shape: Euclidean2DShape,
) : InfluenceSphere<T> {
    override fun influentialNodes(): List<Node<T>> = environment.getNodesWithin(
        shape.transformed {
            origin(environment.getPosition(owner))
            rotate(environment.getHeading(owner))
        },
    ).minusElement(owner)
}
