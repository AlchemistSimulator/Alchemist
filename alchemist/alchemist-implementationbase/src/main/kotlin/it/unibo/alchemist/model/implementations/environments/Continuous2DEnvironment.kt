package it.unibo.alchemist.model.implementations.environments

import it.unibo.alchemist.model.implementations.geometry.intersect
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Neighborhood
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Segment2D

/**
 * Implementation of [Physics2DEnvironment].
 */
open class Continuous2DEnvironment<T> :
    Abstract2DEnvironment<T, Euclidean2DPosition>(), Physics2DEnvironment<T> {

    companion object {
        @JvmStatic private val serialVersionUID: Long = 1L
    }

    override val shapeFactory: Euclidean2DShapeFactory = GeometricShapeFactory.getInstance()
    private val defaultHeading = Euclidean2DPosition(0.0, 0.0)
    private val nodeToHeading = mutableMapOf<Node<T>, Euclidean2DPosition>()
    private var largestShapeDiameter: Double = 0.0

    override fun getNodesWithin(shape: Euclidean2DShape): List<Node<T>> =
        if (shape.diameter + largestShapeDiameter <= 0) emptyList()
        else getNodesWithinRange(shape.centroid, (shape.diameter + largestShapeDiameter) / 2)
            .filter { shape.intersects(getShape(it)) }

    override fun getHeading(node: Node<T>) =
        nodeToHeading.getOrPut(node, { defaultHeading })

    override fun setHeading(node: Node<T>, direction: Euclidean2DPosition) {
        val oldHeading = getHeading(node)
        getPosition(node)?.also {
            nodeToHeading[node] = direction
            if (!canNodeFitPosition(node, it)) {
                nodeToHeading[node] = oldHeading
            }
        }
    }

    override fun getShape(node: Node<T>): Euclidean2DShape =
        shapeFactory.requireCompatible(node.shape)
            .transformed {
                origin(getPosition(node))
                rotate(getHeading(node))
            }

    /**
     * Keeps track of the largest diameter of the shapes.
     */
    override fun nodeAdded(node: Node<T>, position: Euclidean2DPosition, neighborhood: Neighborhood<T>) {
        super.nodeAdded(node, position, neighborhood)
        if (node.shape.diameter > largestShapeDiameter) {
            largestShapeDiameter = node.shape.diameter
        }
    }

    /**
     * {@inheritDoc}.
     */
    override fun nodeRemoved(node: Node<T>, neighborhood: Neighborhood<T>) =
        super.nodeRemoved(node, neighborhood)
            .also {
                nodeToHeading.remove(node)
                if (largestShapeDiameter >= node.shape.diameter) {
                    largestShapeDiameter = nodes.map { it.shape.diameter }.max() ?: 0.0
                }
            }

    /**
     * Moves the node to the [farthestPositionReachable] towards the desired [newpos]. Note that circular hitboxes
     * are used (see [farthestPositionReachable]).
     */
    override fun moveNodeToPosition(node: Node<T>, newpos: Euclidean2DPosition): Unit =
        super.moveNodeToPosition(node, farthestPositionReachable(node, newpos))

    /**
     * A node should be added only if it doesn't collide with already existing nodes and fits in the environment's
     * limits.
     */
    override fun nodeShouldBeAdded(node: Node<T>, position: Euclidean2DPosition): Boolean =
        getNodesWithin(shapeFactory.requireCompatible(node.shape).transformed { origin(position) })
            .isEmpty()

    /**
     * Creates an euclidean position from the given coordinates.
     * @param coordinates coordinates array
     * @return Euclidean2DPosition
     */
    override fun makePosition(vararg coordinates: Number) =
        with(coordinates) {
            require(size == 2)
            Euclidean2DPosition(coordinates[0].toDouble(), coordinates[1].toDouble())
        }

    override fun canNodeFitPosition(node: Node<T>, position: Euclidean2DPosition) =
        getNodesWithin(getShape(node).transformed { origin(position) })
            .minusElement(node)
            .isEmpty()

    /**
     * @returns all nodes that the given [node] would collide with while moving to the [desiredPosition].
     */
    private fun nodesOnPath(node: Node<T>, desiredPosition: Euclidean2DPosition): List<Node<T>> = with(node.shape) {
        val currentPosition = getPosition(node)
        shapeFactory.rectangle(currentPosition.distanceTo(desiredPosition) + diameter, diameter)
            .transformed { Segment2D(currentPosition, desiredPosition).midPoint.let { origin(it.x, it.y) } }
            .let { movementArea -> getNodesWithin(movementArea).minusElement(node) }
    }

    /**
     * @returns the farthest position reachable by the given [node] towards the [desiredPosition], avoiding
     * node overlapping. Since nodes may have various shapes, this method uses the circle circumscribing their
     * shape as hitbox. For a better understanding of how to compute collision points with circular hitboxes
     * see [this discussion](https://bit.ly/3f00NvJ).
     */
    private fun farthestPositionReachable(node: Node<T>, desiredPosition: Euclidean2DPosition): Euclidean2DPosition {
        val currentPosition = getPosition(node)
        val desiredMovement = Segment2D(currentPosition, desiredPosition)
        return nodesOnPath(node, desiredPosition)
            .map { getShape(it) }
            .flatMap { other ->
                intersect(desiredMovement, other.centroid, other.radius + node.shape.radius).points
            }
            .minBy { currentPosition.distanceTo(it) }
            ?: desiredPosition
    }
}
