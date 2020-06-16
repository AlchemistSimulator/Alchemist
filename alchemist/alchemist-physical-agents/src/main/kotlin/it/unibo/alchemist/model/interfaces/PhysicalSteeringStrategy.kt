/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * Defines how physical forces and steering actions (which may be seen as intentional forces) are combined to compute
 * the overall next position reached by a [physicalNode].
 * The combination of steering actions is delegated to a [nonPhysicalStrategy]. The resulting intentional force is
 * then combined with the physical forces acting on [physicalNode] to determine the next position to move on.
 */
interface PhysicalSteeringStrategy<T, P, A, F> : SteeringStrategy<T, P>
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P>,
          F : GeometricShapeFactory<P, A> {

    /**
     * The node this strategy refers to.
     */
    val physicalNode: PhysicalNode<T, P, A, F>

    /**
     * The combination of intentional forces (= steering actions) and the computation of the node's target are
     * delegated to this strategy.
     */
    val nonPhysicalStrategy: SteeringStrategy<T, P>

    /**
     * Computes the next relative position reached by the node, given the resulting intentional force.
     */
    fun computeNextPosition(resultingIntentionalForce: P): P

    /**
     * The combination of intentional [actions] is delegated to [nonPhysicalStrategy], the resulting force is then
     * passed to [computeNextPosition].
     */
    override fun computeNextPosition(actions: List<SteeringAction<T, P>>): P =
        computeNextPosition(nonPhysicalStrategy.computeNextPosition(actions))

    /**
     * Delegated to [nonPhysicalStrategy].
     */
    override fun computeTarget(actions: List<SteeringAction<T, P>>): P = nonPhysicalStrategy.computeTarget(actions)
}
