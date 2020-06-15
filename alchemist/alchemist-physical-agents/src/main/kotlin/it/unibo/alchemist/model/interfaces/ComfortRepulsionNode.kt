/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.interfaces.environments.PhysicsEnvironment
import it.unibo.alchemist.model.interfaces.geometry.GeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.nodes.NodeWithShape

/**
 * A [PhysicalNode] defining a [comfortArea]. When another node enters such area, this one is subject to a repulsion
 * force (hence the name). This is derived from [the work of Pelechano et al](https://bit.ly/3e3C7Tb).
 * [ComfortRepulsionNode]s don't actively push each other, but pushing behavior emerges from the interaction of nodes
 * with different comfort areas (see the article linked above).
 */
interface ComfortRepulsionNode<T, P, A, F> : PhysicalNode<T, P, A, F>
    where P : Vector<P>, P : Position<P>,
          A : GeometricTransformation<P>,
          F : GeometricShapeFactory<P, A> {

    /**
     * The comfort area of this node, when someone else enters this area this node is subject to a repulsion force.
     */
    val comfortArea: GeometricShape<P, A>

    /**
     * Computes the repulsion force caused by a node that entered the [comfortArea].
     */
    fun repulsionForce(other: NodeWithShape<T, P, *>): P

    override fun physicalForces(environment: PhysicsEnvironment<T, P, A, F>): List<P> =
        environment.getNodesWithin(comfortArea)
            .minus(this)
            .filterIsInstance<NodeWithShape<T, P, A>>()
            .map { repulsionForce(it) }
            /*
             * Discard infinitesimal forces.
             */
            .filter { it.magnitude > Double.MIN_VALUE }
}
