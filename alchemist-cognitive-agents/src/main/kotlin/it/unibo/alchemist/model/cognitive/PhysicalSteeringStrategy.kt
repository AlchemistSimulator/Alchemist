/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector

/**
 * Strategy that combines physical forces with intentional steering actions to compute a node's next position.
 *
 * The aggregation of steering actions and target computation is delegated to [nonPhysicalStrategy]. The resulting
 * intentional force is then combined with the physical forces to compute the final displacement of [node].
 *
 * @param T the concentration type.
 * @param P the [Position] and [Vector] type used by the environment.
 * @param A the transformation type supported by shapes in this space.
 * @param F the geometric shape factory used by the environment.
 */
interface PhysicalSteeringStrategy<T, P, A, F> :
    SteeringStrategy<T, P>
    where P : Position<P>, P : Vector<P>,
          A : Transformation<P>,
          F : GeometricShapeFactory<P, A> {
    /** The node to be moved. */
    val node: Node<T>

    /**
     * Strategy responsible for combining intentional steering actions and computing the target.
     */
    val nonPhysicalStrategy: SteeringStrategy<T, P>

    /**
     * Computes the next relative position given the overall intentional force.
     *
     * @param overallIntentionalForce the combined intentional force as a vector of type [P].
     * @return the next relative position as a [P].
     */
    fun computeNextPosition(overallIntentionalForce: P): P

    /**
     * Computes the next position by delegating the combination of steering actions to [nonPhysicalStrategy]
     * and then applying physical dynamics.
     */
    override fun computeNextPosition(actions: List<SteeringAction<T, P>>): P =
        computeNextPosition(nonPhysicalStrategy.computeNextPosition(actions))

    /** Delegates target computation to [nonPhysicalStrategy]. */
    override fun computeTarget(actions: List<SteeringAction<T, P>>): P = nonPhysicalStrategy.computeTarget(actions)
}
