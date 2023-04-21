/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.geometry.GeometricTransformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory

/**
 * Defines how physical forces and steering actions (which may be seen as intentional forces) are combined to compute
 * the overall next position reached by a physical [node]. The combination of steering actions is delegated to a
 * [nonPhysicalStrategy]. The resulting intentional force is then combined with the physical ones to determine the
 * next position reached by [node].
 */
interface PhysicalSteeringStrategy<T, P, A, F> : SteeringStrategy<T, P>
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P>,
          F : GeometricShapeFactory<P, A> {

    /**
     * The node to be moved.
     */
    val node: Node<T>

    /**
     * The combination of intentional forces (= steering actions) and [computeTarget] are delegated to this strategy.
     */
    val nonPhysicalStrategy: SteeringStrategy<T, P>

    /**
     * Computes the next relative position reached by the node, given the overall intentional force.
     */
    fun computeNextPosition(overallIntentionalForce: P): P

    /**
     * Computes the next relative position reached by the node, taking into account both the intentional and the
     * physical forces acting on [node] (intentional forces = [actions]).
     */
    override fun computeNextPosition(actions: List<SteeringAction<T, P>>): P =
        computeNextPosition(nonPhysicalStrategy.computeNextPosition(actions))

    /**
     * Delegated to [nonPhysicalStrategy].
     */
    override fun computeTarget(actions: List<SteeringAction<T, P>>): P = nonPhysicalStrategy.computeTarget(actions)
}
