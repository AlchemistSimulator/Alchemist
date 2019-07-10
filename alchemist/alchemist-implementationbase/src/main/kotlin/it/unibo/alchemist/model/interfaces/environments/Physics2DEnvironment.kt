package it.unibo.alchemist.model.interfaces.environments

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.geometry.GeometricShape2D
import it.unibo.alchemist.model.interfaces.geometry.GeometricShape2DFactory

/**
 * Interface for a 2D environment supporting collision detection.
 *
 * @param <T> Concentration type
 * @param <P> Position type
 */
interface Physics2DEnvironment<T, P : Position2D<P>> : Environment<T, P> {

    /**
     * A factory of shapes compatible with the {@link Position} type supported by the environment.
     */
    val shapeFactory: GeometricShape2DFactory<P>

    /**
     * Gets the heading of a node in a vector form, considering the center of the node's shape as the origin.
     *
     * @param node The node
     * @return The heading of the given node
     */
    fun getHeading(node: Node<T>): Double

    /**
     * Sets the heading of a node
     *
     * @param node The node
     * @param radians The new angle in radians
     */
    fun setHeading(node: Node<T>, radians: Double)

    /**
     * Sets the shape of a node.
     * @param node The node
     * @param shape The shape
     */
    fun setShape(node: Node<T>, shape: GeometricShape2D<P>)

    /**
     * Gets the shape of a node relatively to its position and heading in the environment.
     *
     * @param node The node
     * @return Its shape
     */
    fun getShape(node: Node<T>): GeometricShape2D<P>

    /**
     * Gets all nodes whose shape.intersect is true for the given shape.
     * @param shape the shape
     * @return the set of nodes colliding with the given shape
     */
    fun getNodesWithin(shape: GeometricShape2D<P>): List<Node<T>>
}
