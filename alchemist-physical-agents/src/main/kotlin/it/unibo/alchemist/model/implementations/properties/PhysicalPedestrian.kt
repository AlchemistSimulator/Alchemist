/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.properties

import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.properties.CognitiveProperty
import it.unibo.alchemist.model.interfaces.properties.PhysicalPedestrian
import it.unibo.alchemist.model.interfaces.properties.OccupiesSpaceProperty
import it.unibo.alchemist.model.interfaces.environments.PhysicsEnvironment
import it.unibo.alchemist.model.interfaces.geometry.GeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.nextDouble
import org.apache.commons.math3.random.RandomGenerator
import it.unibo.alchemist.model.interfaces.Node.Companion.asProperty
import it.unibo.alchemist.model.interfaces.Node.Companion.asPropertyOrNull

/**
 * Base implementation of a pedestrian's capability to experience physical interactions.
 */
abstract class PhysicalPedestrian<T, P, A, F>(
    /**
     * The simulation's [RandomGenerator].
     */
    randomGenerator: RandomGenerator,
    /**
     * The environment in which the pedestrian is moving.
     */
    open val environment: PhysicsEnvironment<T, P, A, F>,
    override val node: Node<T>,
) : PhysicalPedestrian<T, P, A, F>
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P>,
          F : GeometricShapeFactory<P, A> {

    private val desiredSpaceTreshold: Double = randomGenerator.nextDouble(minimumSpaceTreshold, maximumSpaceThreshold)

    override val comfortRay: Double get() {
        val cognitiveModel = node.asPropertyOrNull<T, CognitiveProperty<T>>()?.cognitiveModel
        return if (cognitiveModel?.wantsToEscape() == true) {
            desiredSpaceTreshold / 3
        } else {
            desiredSpaceTreshold
        }
    }

    private fun getShapeInNodePosition(node: Node<T>) = node.asProperty<T, OccupiesSpaceProperty<T, P, A>>()
        .shape
        .transformed { origin(environment.getPosition(node)) }

    override fun repulsionForce(other: Node<T>): P {
        val myShape = getShapeInNodePosition(node)
        val otherShape = getShapeInNodePosition(other)
        return (myShape.centroid - otherShape.centroid).let {
            val desiredDistance = myShape.radius + comfortRay + otherShape.radius
            /*
             * it is the vector leading from the center of other to the center of this node, it.magnitude is the
             * actual distance between the two nodes.
             */
            it.normalized() * (desiredDistance - it.magnitude).coerceAtLeast(0.0) / it.magnitude
        }
    }

    abstract override fun physicalForces(environment: PhysicsEnvironment<T, P, A, F>): List<P>

    companion object {
        /**
         * Minimum value for normal state [comfortRay].
         */
        private const val minimumSpaceTreshold = 0.1
        /**
         * Maximum value for normal state [comfortRay].
         */
        private const val maximumSpaceThreshold = 1.0
    }

    abstract override val comfortArea: GeometricShape<P, A>
}
