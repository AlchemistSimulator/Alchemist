/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.capabilities

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.capabilities.PedestrianCognitiveCapability
import it.unibo.alchemist.model.interfaces.capabilities.PedestrianPhysicalCapability
import it.unibo.alchemist.model.interfaces.capabilities.SpatialCapability
import it.unibo.alchemist.model.interfaces.environments.PhysicsEnvironment
import it.unibo.alchemist.model.interfaces.geometry.GeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.nodes.NodeWithShape
import it.unibo.alchemist.nextDouble
import org.apache.commons.math3.random.RandomGenerator
import it.unibo.alchemist.model.interfaces.Node.Companion.asCapability
import it.unibo.alchemist.model.interfaces.capabilities.Pedestrian2DPhysicalCapability
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation

class BasePedestrianPhysicalCapability<T, P, A, F>(
    randomGenerator: RandomGenerator,
    override val node: Node<T>,
    override val comfortArea: GeometricShape<P, A>,
) : PedestrianPhysicalCapability<T, P, A, F>
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P>,
          F : GeometricShapeFactory<P, A> {

    private val desiredSpaceTreshold: Double = randomGenerator.nextDouble(minimumSpaceTreshold, maximumSpaceThreshold)

    override val comfortRay: Double get() =
        if (node.asCapability<T, PedestrianCognitiveCapability<T>>().cognitiveModel.wantsToEscape()) {
            desiredSpaceTreshold / 3
        } else {
            desiredSpaceTreshold
        }

    /*
     * TODO: [other] should be a simple [Node] and this method should retrieve its shape by
     * accessing its [SpatialCapability]
     */
    override fun repulsionForce(other: NodeWithShape<T, P, A>): P {
        val myShape = node.asCapability<T, SpatialCapability<T, P, A>>().shape
        return (myShape.centroid - other.shape.centroid).let {
            val desiredDistance = myShape.radius + comfortRay + other.shape.radius
            /*
             * it is the vector leading from the center of other to the center of this node, it.magnitude is the
             * actual distance between the two nodes.
             */
            it.normalized() * (desiredDistance - it.magnitude).coerceAtLeast(0.0) / it.magnitude
        }
    }

    override fun physicalForces(environment: PhysicsEnvironment<T, P, A, F>): List<P> {
        TODO("Not yet implemented")
    }

    companion object {
        /**
         * Mimimum value for normal state [comfortRay].
         */
        private const val minimumSpaceTreshold = 0.1
        /**
         * Maximum value for normal state [comfortRay].
         */
        private const val maximumSpaceThreshold = 1.0
    }
}

class BasePedestrian2DPhysicalCapability<T>(
    randomGenerator: RandomGenerator,
    node: Node<T>,
    comfortArea: Euclidean2DShape
) : PedestrianPhysicalCapability<T, Euclidean2DPosition, Euclidean2DTransformation, Euclidean2DShapeFactory>
by BasePedestrianPhysicalCapability(randomGenerator, node, comfortArea),
    Pedestrian2DPhysicalCapability<T>
