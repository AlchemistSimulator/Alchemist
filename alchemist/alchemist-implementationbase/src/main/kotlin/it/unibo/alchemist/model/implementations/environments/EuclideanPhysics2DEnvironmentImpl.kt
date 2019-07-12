package it.unibo.alchemist.model.implementations.environments

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Neighborhood
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.GeometricShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DShape

/**
 * Base class for {@link EuclideanPhysics2DEnvironmentImpl}
 */
abstract class AbstractEuclideanPhysics2DEnvironment<T> : Abstract2DEnvironment<T, Euclidean2DPosition>(), EuclideanPhysics2DEnvironment<T> {

    /**
     * The default heading for a node.
     */
    protected val defaultHeading = Euclidean2DPosition(0.0, 0.0)
    private val nodeToHeading = mutableMapOf<Node<T>, Euclidean2DPosition>()
    private var largestShapeDiameter: Double = 0.0

    override fun getNodesWithin(shape: Euclidean2DShape): List<Node<T>> =
        if (shape.diameter <= 0) emptyList()
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

    override fun getShape(node: Node<T>): GeometricShape<P> =
        nodeToShape.getOrPut(node, this::getDefaultShape)
            .rotated(getHeading(node))
            .withOrigin(if (nodes.contains(node)) getPosition(node) else getDefaultOrigin())

    /**
     * move this code in addNode when and if you merge this with AbstractEnvironment
     */
    override fun nodeAdded(node: Node<T>, position: P, neighborhood: Neighborhood<T>?) {
        super.nodeAdded(node, position, neighborhood)
        nodeToShape.putIfAbsent(node, getDefaultShape())
        if (getShape(node).diameter > largestShapeDiameter) {
            largestShapeDiameter = getShape(node).diameter
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun nodeRemoved(node: Node<T>?, neighborhood: Neighborhood<T>?) =
        super.nodeRemoved(node, neighborhood)
            .also {
                nodeToShape.remove(node)
                nodeToHeading.remove(node)
                largestShapeDiameter = nodeToShape.maxBy { it.value.diameter }?.value?.diameter ?: 0.0
            }

    /**
     * Moves the node only if it doesn't collide with others.
     */
    override fun moveNodeToPosition(node: Node<T>, newpos: P) =
        if (canNodeFitPosition(node, newpos)) super.moveNodeToPosition(node, newpos) else Unit

    /**
     * A node should be added only if it doesn't collide with already existing nodes.
     */
    override fun nodeShouldBeAdded(node: Node<T>, p: P): Boolean =
        canNodeFitPosition(node, p)

    /**
     * @return the default shape for nodes without one
     */
    protected fun getDefaultShape() =
        shapeFactory.adimensional()

    /**
     * @return the default origin for nodes' shapes not yet added
     */
    protected fun getDefaultOrigin() =
        makePosition(0.0, 0.0)

    private fun canNodeFitPosition(node: Node<T>, position: P) =
        getNodesWithin(getShape(node).withOrigin(position))
            .minusElement(node)
            .isEmpty()
}