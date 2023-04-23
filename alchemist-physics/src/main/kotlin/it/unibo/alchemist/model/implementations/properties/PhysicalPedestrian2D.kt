/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.properties

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.PhysicsEnvironment
import it.unibo.alchemist.model.environments.Physics2DEnvironment
import it.unibo.alchemist.model.geometry.shapes.AdimensionalShape
import it.unibo.alchemist.model.interfaces.environments.Dynamics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.properties.AreaProperty
import it.unibo.alchemist.model.interfaces.properties.CognitiveProperty
import it.unibo.alchemist.model.interfaces.properties.PedestrianProperty
import it.unibo.alchemist.model.interfaces.properties.PhysicalPedestrian2D
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.properties.AbstractNodeProperty
import it.unibo.alchemist.util.RandomGenerators.nextDouble
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import org.apache.commons.math3.random.RandomGenerator

/**
 * Base implementation of a pedestrian's capability to experience physical interactions in a 2D space.
 */
class PhysicalPedestrian2D<T>(
    private val randomGenerator: RandomGenerator,
    /**
     * The environment in which the node is moving.
     */
    val environment: Physics2DEnvironment<T>,
    override val node: Node<T>,
) : AbstractNodeProperty<T>(node), PhysicalPedestrian2D<T> {

    private val pedestrian by lazy { node.asProperty<T, PedestrianProperty<T>>() }

    override var isFallen: Boolean = false
        private set

    private val nodeShape by lazy { node.asProperty<T, AreaProperty<T>>().shape }

    private val desiredSpaceTreshold: Double = randomGenerator.nextDouble(minimumSpaceTreshold, maximumSpaceThreshold)

    private val cognitiveModel = node.asPropertyOrNull<T, CognitiveProperty<T>>()?.cognitiveModel

    override val comfortRay: Double get() {
        return if (cognitiveModel?.wantsToEscape() == true) {
            desiredSpaceTreshold / 3
        } else {
            desiredSpaceTreshold
        }
    }

    private var fallenAgentListeners: List<(Node<T>) -> Unit> = listOf()

    override val comfortArea: Euclidean2DShape get() = environment
        .shapeFactory
        .circle(nodeShape.radius + comfortRay)
        .transformed { origin(environment.getPosition(node)) }

    override val rectangleOfInfluence: Euclidean2DShape get() = environment
        .shapeFactory
        .rectangle(rectangleOfInfluenceDimensions.first, rectangleOfInfluenceDimensions.second)
        .transformed {
            rotate(environment.getHeading(node))
            origin(node.position + environment.getHeading(node) * (rectangleOfInfluenceDimensions.first / 2.0))
        }

    private val fallenAgentPerceptionArea
        get() = environment.shapeFactory.circle(fallenAgentPerceptionRadius).transformed { origin(node.position) }

    private val Node<T>.position get() = environment.getPosition(this)

    override fun checkAndPossiblyFall() {
        if (!isFallen && shouldFall(repulsionForces())) {
            isFallen = true
            fallenAgentListeners.forEach { hasFallen -> hasFallen(node) }
        }
    }

    override fun shouldFall(pushingForces: List<Euclidean2DPosition>) =
        pushingForces.fold(Euclidean2DPosition.zero) { acc, f -> acc + f }.magnitude > pedestrian.runningSpeed

    override fun repulsionForces(): List<Euclidean2DPosition> = collectForces(::repulse, comfortArea) {
        !it.asProperty<T, PhysicalPedestrian2D<T>>().isFallen
    }

    override fun repulse(other: Node<T>): Euclidean2DPosition {
        val myShape = nodeShape.transformed { origin(environment.getPosition(node)) }
        val otherShape = environment.getShape(other)
        return (myShape.centroid - otherShape.centroid).let {
            val desiredDistance = myShape.radius + comfortRay + otherShape.radius
            /*
             * it is the vector leading from the center of other to the center of this node, it.magnitude is the
             * actual distance between the two nodes.
             */
            it * (desiredDistance - it.magnitude).coerceAtLeast(0.0) / it.magnitude
        }
    }

    override fun avoidanceForces() = collectForces(::avoid, rectangleOfInfluence)

    override fun avoid(other: Node<T>): Euclidean2DPosition {
        if (environment is Dynamics2DEnvironment) {
            val distanceVector = (node.position - other.position).let { Vector3D(it.x, it.y, 0.0) }
            val velocity = environment.getVelocity(node).let { Vector3D(it.x, it.y, 0.0) }
            val tangentialForce = Vector3D
                .crossProduct(distanceVector, velocity)
                .crossProduct(distanceVector)
                .let {
                    check(it.z == 0.0) { "The cross product result should live on the 2D-plane" }
                    Euclidean2DPosition(it.x, it.y)
                }
            val weightFactor = avoidanceDistanceWeight(other) * avoidanceDirectionWeight(
                environment.getVelocity(node),
                environment.getVelocity(other),
            )
            if (tangentialForce.magnitude > 0) {
                return tangentialForce.normalized() * weightFactor
            }
        }
        return Euclidean2DPosition.zero
    }

    private fun avoidanceDistanceWeight(other: Node<T>): Double {
        val weight = environment.getDistanceBetweenNodes(node, other) - rectangleOfInfluenceDimensions.first / 2.0
        return weight * weight
    }

    private fun avoidanceDirectionWeight(thisVelocity: Euclidean2DPosition, otherVelocity: Euclidean2DPosition) =
        when {
            thisVelocity.dot(otherVelocity) > 0 -> directionWeight
            else -> directionWeight * 2
        }

    override fun fallenAgentAvoidanceForces() =
        collectForces(::avoid, fallenAgentPerceptionArea) {
            it.asProperty<T, PhysicalPedestrian2D<T>>().isFallen
        }

    override fun onFall(listener: (Node<T>) -> Unit) {
        fallenAgentListeners += listener
    }

    private fun collectForces(
        force: (node: Node<T>) -> Euclidean2DPosition,
        influenceArea: Euclidean2DShape,
        nodeFilter: (node: Node<T>) -> Boolean = { true },
    ) = environment.getNodesWithin(influenceArea)
        .asSequence()
        .minusElement(node)
        .filter { environment.getShape(it) !is AdimensionalShape }
        .filter(nodeFilter)
        .map(force)
        .filter { it.magnitude > Double.MIN_VALUE }
        .toList()

    override fun physicalForces(
        environment: PhysicsEnvironment<T, Euclidean2DPosition, Euclidean2DTransformation, Euclidean2DShapeFactory>,
    ): List<Euclidean2DPosition> = avoidanceForces()

    override fun cloneOnNewNode(node: Node<T>) = PhysicalPedestrian2D(randomGenerator, environment, node)

    override fun toString() = "${super.toString()}[desiredSpaceThreshold=$desiredSpaceTreshold, " +
        "comfortRay=$comfortRay, isFallen=$isFallen]"

    companion object {
        /**
         * Minimum value for normal state [comfortRay] (in meters).
         */
        private const val minimumSpaceTreshold = 0.5

        /**
         * Maximum value for normal state [comfortRay] (in meters).
         */
        private const val maximumSpaceThreshold = 1.0

        /**
         * Dimension (in meters) of the rectangle of influence (width, height).
         * This dimensions have been set according to the work of [Pelechano et al](https://bit.ly/3e3C7Tb)
         */
        private val rectangleOfInfluenceDimensions: Pair<Double, Double> = Pair(3.0, 1.0)

        /**
         * Direction tangential force weight factor for when two nodes are moving in the same direction.
         * See the work of [Pelechano et al](https://bit.ly/3e3C7Tb).
         */
        private const val directionWeight = 1.2

        /**
         * Fallen agent perception radius (in meters).
         * For further information please refer to the work of [Pelechano et al](https://bit.ly/3e3C7Tb).
         */
        private const val fallenAgentPerceptionRadius = 1.5
    }
}
