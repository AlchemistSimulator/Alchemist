/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.physics.environments

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.environments.Euclidean2DEnvironment
import it.unibo.alchemist.model.geometry.Euclidean2DShape
import it.unibo.alchemist.model.geometry.Euclidean2DShapeFactory
import it.unibo.alchemist.model.geometry.Euclidean2DTransformation
import it.unibo.alchemist.model.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.geometry.Segment2D
import it.unibo.alchemist.model.geometry.Segment2DImpl
import it.unibo.alchemist.model.geometry.shapes.AdimensionalShape
import it.unibo.alchemist.model.physics.properties.AreaProperty
import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * Implementation of [Physics2DEnvironment].
 */
open class ContinuousPhysics2DEnvironment<T>(incarnation: Incarnation<T, Euclidean2DPosition>) :
    Continuous2DEnvironment<T>(incarnation),
    Physics2DEnvironment<T> {
    private companion object {
        private val adimensional =
            AdimensionalShape<Euclidean2DPosition, Euclidean2DTransformation>(Euclidean2DEnvironment.origin)
    }

    override val shapeFactory =
        GeometricShapeFactory.getInstance<Euclidean2DPosition, Euclidean2DTransformation, Euclidean2DShapeFactory>()
    private val defaultHeading = Euclidean2DPosition(0.0, 0.0)
    private val nodeToHeading = mutableMapOf<Node<T>, Euclidean2DPosition>()
    private var largestShapeDiameter: Double = 0.0

    private val shapefulNodes: LoadingCache<Node<T>, Euclidean2DShape> =
        Caffeine.newBuilder().weakKeys().build { node ->
            node.asPropertyOrNull<T, AreaProperty<T>>()?.shape ?: adimensional
        }

    override fun getNodesWithin(shape: Euclidean2DShape): List<Node<T>> = when {
        shape.diameter + largestShapeDiameter <= 0 -> emptyList()
        else ->
            nodesAround(shape.centroid, (shape.diameter + largestShapeDiameter) / 2)
                .filter { shape.intersects(getShape(it)) }
    }

    override fun getHeading(node: Node<T>) = nodeToHeading.getOrPut(node) { defaultHeading }

    override fun setHeading(node: Node<T>, direction: Euclidean2DPosition) {
        nodeToHeading[node] = direction
    }

    override fun getShape(node: Node<T>): Euclidean2DShape = shapefulNodes[node].transformed {
        origin(this@ContinuousPhysics2DEnvironment.currentPositionOf(node))
        rotate(getHeading(node))
    }

    override fun nodeAdded(node: Node<T>, position: Euclidean2DPosition, neighborhood: Neighborhood<T>) {
        super.nodeAdded(node, position, neighborhood)
        val shape = getShape(node)
        if (shape != adimensional && shape.diameter > largestShapeDiameter) {
            largestShapeDiameter = shape.diameter
        }
    }

    /**
     * {@inheritDoc}.
     */
    override fun nodeRemoved(node: Node<T>, neighborhood: Neighborhood<T>) {
        super.nodeRemoved(node, neighborhood)
        nodeToHeading.remove(node)
        val occupiesSpaceProperty = node.asPropertyOrNull<T, AreaProperty<T>>()
        if (occupiesSpaceProperty != null && largestShapeDiameter <= occupiesSpaceProperty.shape.diameter) {
            largestShapeDiameter = nodes.current
                .asSequence()
                .filter { getShape(it) != adimensional }
                .map { getShape(it) }
                .map { it.diameter }
                .maxOrNull() ?: 0.0
        }
    }

    /**
     * A node should be added only if it doesn't collide with already existing nodes and fits in the environment's
     * limits.
     */
    override fun nodeShouldBeAdded(node: Node<T>, position: Euclidean2DPosition): Boolean = node.canFitIn(position)

    /**
     * Creates an euclidean position from the given coordinates.
     * @param coordinates coordinates array
     * @return Euclidean2DPosition
     */
    override fun makePosition(vararg coordinates: Number) = with(coordinates) {
        require(size == 2)
        Euclidean2DPosition(coordinates[0].toDouble(), coordinates[1].toDouble())
    }

    /**
     * The farthest position reachable by [node] towards [desired], avoiding node overlapping. Other nodes, considered
     * through circular hitboxes of radius equal to the one of their shape, stop the movement where the hitbox of [node]
     * first touches theirs; nodes already colliding with [node] stop it at its current position. Shapeless nodes do not
     * collide.
     */
    override fun farthestReachablePosition(node: Node<T>, desired: Euclidean2DPosition): Euclidean2DPosition =
        super<Continuous2DEnvironment>.farthestReachablePosition(node, desired).let { reachable ->
            getShape(node).takeIf { it != adimensional }?.let { firstCollision(node, it, reachable) } ?: reachable
        }

    /*
     * The position where node, whose shape is the given one, first collides with another node moving towards
     * reachable, or null if it collides with none.
     */
    private fun firstCollision(
        node: Node<T>,
        shape: Euclidean2DShape,
        reachable: Euclidean2DPosition,
    ): Euclidean2DPosition? {
        val currentPosition = currentPositionOf(node)
        val desiredMovement = Segment2DImpl(currentPosition, reachable)
        val nodesOnPath =
            nodesOnPath(node, desiredMovement)
                .map { getShape(it) }
            /*
             * Considers only nodes in the direction of movement.
             */
                .filter { desiredMovement.toVector.angleBetween(it.centroid - currentPosition) < Math.PI / 2 }
        /*
         * If we're already colliding with someone, just return the current position.
         */
        return when {
            nodesOnPath.any { currentPosition.distanceTo(it.centroid) < it.radius + shape.radius } -> currentPosition
            else ->
                nodesOnPath
                    .flatMap { other ->
                        desiredMovement.intersectCircle(other.centroid, other.radius + shape.radius).asList
                    }.minByOrNull { currentPosition.distanceTo(it) }
        }
    }

    /**
     * @returns all nodes that the given [node] would collide with while performing the [desiredMovement].
     * Such segment should connect the [node]'s current position and its desired position.
     */
    private fun nodesOnPath(node: Node<T>, desiredMovement: Segment2D<*>): List<Node<T>> = with(getShape(node)) {
        shapeFactory
            .rectangle(desiredMovement.length + diameter, diameter)
            .transformed {
                desiredMovement.midPoint.let { origin(it.x, it.y) }
                rotate(desiredMovement.toVector.asAngle)
            }.let { movementArea ->
                getNodesWithin(movementArea)
                    .minusElement(node)
            }
    }

    /**
     * Checks if a node doesn't overlap with any other node in the environment (see [overlappingNodes]). If the
     * node is shapeless, true is returned.
     */
    private fun Node<T>.canFitIn(position: Euclidean2DPosition): Boolean {
        val nodeShape = asPropertyOrNull<T, AreaProperty<T>>()?.shape
        return nodeShape == null || overlappingNodes(nodeShape, position).isEmpty()
    }

    /**
     * @returns the nodes in this environment whose shape intersects this node's shape. The [position] of this
     * node must be specified as it may not have been added in the environment yet.
     */
    private fun Node<T>.overlappingNodes(nodeShape: Euclidean2DShape, position: Euclidean2DPosition): List<Node<T>> =
        getNodesWithin(shapeFactory.requireCompatible(nodeShape).transformed { origin(position) })
            .minusElement(this)
}
