package it.unibo.alchemist.model.implementations.environments

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.*

/**
 * An Environment supporting {@link GeometricShape} and collisions detection.
 * It does not allow for two nodes to overlap unless their shape is punctiform.
 *
 * TODO: getAllNodesInRange is a public method which doesn't consider the shapes..
 */
class Physics2DEnvironment<T> : Continuous2DEnvironment<T>(), PhysicsEnvironment<T, Euclidean2DPosition> {
    override val shapeFactory: GeometricShapeFactory<Euclidean2DPosition> = GeometricShapeFactory.getInstance()
    private val defaultShape = shapeFactory.punctiform()
    private val defaultHeading = 0.0
    private val defaultOrigin = makePosition(0.0, 0.0)
    private val nodeToShape = mutableMapOf<Node<T>, GeometricShape<Euclidean2DPosition>>()
    private val nodeToHeading = mutableMapOf<Node<T>, Double>()
    private var largestShapeDiameter: Double = 0.0

    override fun getNodesWithin(shape: GeometricShape<Euclidean2DPosition>): List<Node<T>> =
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

    override fun setShape(node: Node<T>, shape: GeometricShape<Euclidean2DPosition>) {
        nodeToShape[node] = shape
    }

    override fun getShape(node: Node<T>): GeometricShape<Euclidean2DPosition> =
        nodeToShape.getOrPut(node, {defaultShape})
            .rotate(getHeading(node))
            .withOrigin(getPosition(node) ?: defaultOrigin)

    /**
     * TODO: move this code in addNode when and if you merge this with AbstractEnvironment
     */
    override fun nodeAdded(node: Node<T>, position: Euclidean2DPosition, neighborhood: Neighborhood<T>?) {
        super.nodeAdded(node, position, neighborhood)
        nodeToShape.putIfAbsent(node, defaultShape)
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
    override fun moveNodeToPosition(node: Node<T>, newpos: Euclidean2DPosition) =
        if (canNodeFitPosition(node, newpos)) super.moveNodeToPosition(node, newpos) else Unit

    /**
     * A node should be added only if it doesn't collide with already existing nodes.
     */
    override fun nodeShouldBeAdded(node: Node<T>, p: Euclidean2DPosition): Boolean =
        canNodeFitPosition(node, p)

    /**
     * {@inheritDoc}
     * @return null if the node is not found.
     */
    override fun getPosition(node: Node<T>?): Euclidean2DPosition? {
        return super.getPosition(node)
    }

    private fun canNodeFitPosition(node: Node<T>, position: Euclidean2DPosition) =
        getNodesWithin(getShape(node).withOrigin(position))
            .minusElement(node)
            .isEmpty()
}