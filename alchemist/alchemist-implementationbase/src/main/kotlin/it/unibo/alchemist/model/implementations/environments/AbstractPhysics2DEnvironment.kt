package it.unibo.alchemist.model.implementations.environments

import it.unibo.alchemist.model.interfaces.Neighborhood
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.GeometricShape2D

/**
 * Base class for {@link Physics2DEnvironment}
 */
abstract class AbstractPhysics2DEnvironment<T, P : Position2D<P>> : Abstract2DEnvironment<T, P>(), Physics2DEnvironment<T, P> {

    /**
     * The default heading for a node.
     */
    protected val defaultHeading = 0.0
    private val nodeToShape = mutableMapOf<Node<T>, GeometricShape2D<P>>()
    private val nodeToHeading = mutableMapOf<Node<T>, Double>()
    private var largestShapeDiameter: Double = 0.0

    override fun getNodesWithin(shape: GeometricShape2D<P>): List<Node<T>> =
        if (shape.diameter <= 0) emptyList()
        else getNodesWithinRange(shape.centroid, (shape.diameter + largestShapeDiameter) / 2)
            .filter { shape.intersects(getShape(it)) }

    override fun getHeading(node: Node<T>) =
        nodeToHeading.getOrPut(node, { defaultHeading })

    override fun setHeading(node: Node<T>, radians: Double) {
        val oldHeading = getHeading(node)
        getPosition(node)?.also {
            nodeToHeading[node] = radians
            if (!canNodeFitPosition(node, it)) {
                nodeToHeading[node] = oldHeading
            }
        }
    }

    override fun setShape(node: Node<T>, shape: GeometricShape2D<P>) {
        nodeToShape[node] = shape
    }

    override fun getShape(node: Node<T>): GeometricShape2D<P> =
        nodeToShape.getOrPut(node, this::getDefaultShape)
            .rotate(getHeading(node))
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
        shapeFactory.punctiform()

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