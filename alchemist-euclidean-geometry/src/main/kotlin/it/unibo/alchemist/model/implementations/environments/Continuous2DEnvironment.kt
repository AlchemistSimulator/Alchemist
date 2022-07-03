/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.environments

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.Segment2DImpl
import it.unibo.alchemist.model.implementations.geometry.AdimensionalShape
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.Neighborhood
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironment
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Segment2D
import it.unibo.alchemist.model.interfaces.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.interfaces.properties.AreaProperty

/**
 * Implementation of [Physics2DEnvironment].
 */
open class Continuous2DEnvironment<T>(incarnation: Incarnation<T, Euclidean2DPosition>) :
    Euclidean2DEnvironment<T>,
    Abstract2DEnvironment<T, Euclidean2DPosition>(incarnation),
    Physics2DEnvironment<T> {

    companion object {
        @JvmStatic private val serialVersionUID: Long = 1L

        private val adimensional =
            AdimensionalShape<Euclidean2DPosition, Euclidean2DTransformation>(Euclidean2DEnvironment.origin)
    }

    override val shapeFactory =
        GeometricShapeFactory.getInstance<Euclidean2DPosition, Euclidean2DTransformation, Euclidean2DShapeFactory>()
    private val defaultHeading = Euclidean2DPosition(0.0, 0.0)
    private val nodeToHeading = mutableMapOf<Node<T>, Euclidean2DPosition>()
    private var largestShapeDiameter: Double = 0.0

    @Transient
    private val shapefulNodes: LoadingCache<Node<T>, Euclidean2DShape> =
        Caffeine.newBuilder().weakKeys().build { node ->
            node.asPropertyOrNull<T, AreaProperty<T>>()?.shape ?: adimensional
        }

    override fun getNodesWithin(shape: Euclidean2DShape): List<Node<T>> = when {
        shape.diameter + largestShapeDiameter <= 0 -> emptyList()
        else ->
            getNodesWithinRange(shape.centroid, (shape.diameter + largestShapeDiameter) / 2)
                .filter { shape.intersects(getShape(it)) }
    }

    override fun getHeading(node: Node<T>) = nodeToHeading.getOrPut(node) { defaultHeading }

    override fun setHeading(node: Node<T>, direction: Euclidean2DPosition) {
        nodeToHeading[node] = direction
    }

    override fun getShape(node: Node<T>): Euclidean2DShape = shapefulNodes[node].transformed {
        origin(getPosition(node))
        rotate(getHeading(node))
    }

    /**
     * Keeps track of the largest diameter of the shapes. Throws an [IllegalStateException] if the [node] can't fit
     * the [position] (see [canFit]).
     */
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
            largestShapeDiameter = nodes.asSequence()
                .filter { getShape(it) != adimensional }
                .map { getShape(it) }
                .map { it.diameter }
                .maxOrNull() ?: 0.0
        }
    }

    /**
     * Moves the [node] to the [farthestPositionReachable] towards the desired [newPosition]. If the node is shapeless,
     * it is simply moved to [newPosition].
     */
    override fun moveNodeToPosition(node: Node<T>, newPosition: Euclidean2DPosition) =
        if (getShape(node) != adimensional) {
            super.moveNodeToPosition(node, farthestPositionReachable(node, newPosition))
        } else {
            super.moveNodeToPosition(node, newPosition)
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
     * [node].[canFit] must be true for the returned position.
     */
    override fun farthestPositionReachable(
        node: Node<T>,
        desiredPosition: Euclidean2DPosition,
        hitboxRadius: Double
    ): Euclidean2DPosition {
        val currentPosition = getPosition(node)
        val desiredMovement = Segment2DImpl(currentPosition, desiredPosition)
        val nodesOnPath = nodesOnPath(node, desiredMovement)
            .map { getShape(it) }
            /*
             * Considers only nodes in the direction of movement.
             */
            .filter { desiredMovement.toVector.angleBetween(it.centroid - currentPosition) < Math.PI / 2 }
        /*
         * If we're already colliding with someone, just return the current position.
         */
        if (nodesOnPath.any { currentPosition.distanceTo(it.centroid) < it.radius + getShape(node).radius }) {
            return currentPosition
        }
        return nodesOnPath
            .flatMap { other ->
                desiredMovement.intersectCircle(other.centroid, other.radius + hitboxRadius).asList
            }
            .minByOrNull { currentPosition.distanceTo(it) }
            ?: desiredPosition
    }

    /**
     * @returns all nodes that the given [node] would collide with while performing the [desiredMovement].
     * Such segment should connect the [node]'s current position and its desired position.
     */
    private fun nodesOnPath(node: Node<T>, desiredMovement: Segment2D<*>): Nodes<T> =
        with(getShape(node)) {
            shapeFactory.rectangle(desiredMovement.length + diameter, diameter)
                .transformed {
                    desiredMovement.midPoint.let { origin(it.x, it.y) }
                    rotate(desiredMovement.toVector.asAngle)
                }
                .let { movementArea ->
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
    private fun Node<T>.overlappingNodes(nodeShape: Euclidean2DShape, position: Euclidean2DPosition): Nodes<T> =
        getNodesWithin(shapeFactory.requireCompatible(nodeShape).transformed { origin(position) })
            .minusElement(this)
}

private typealias Nodes<T> = List<Node<T>>
