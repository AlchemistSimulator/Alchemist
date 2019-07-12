package it.unibo.alchemist.model.interfaces.environments

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.geometry.GeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * An environment supporting physics and nodes shapes.
 * Note: due to the high number of parameters its's highly recommended to not use this interface directly,
 * but to create an apposite interface extending this one instead.
 *
 * @param <T> nodes concentration type
 * @param <P> positions type
 * @param <S> vectors type for the geometry used in this environment
 * @param <A> geometric transformations used in this environment
 * @param <F> factory of shapes compatible with this environment
 */
interface PhysicsEnvironment<T, P : Position<P>, S : Vector<S>, A : GeometricTransformation<S>, F : GeometricShapeFactory<S, A>>
    : Environment<T, P>
{
    /**
     * A factory of shapes compatible with this environment.
     */
    val shapeFactory: F

    /**
     * Gets the heading of a node as a direction vector.
     *
     * @param node The node
     * @return The heading of the given node
     */
    fun getHeading(node: Node<T>): S

    /**
     * Sets the heading of a node
     *
     * @param node The node
     * @param direction The direction vector.
     */
    fun setHeading(node: Node<T>, direction: S)

    /**
     * Gets the shape of a node relatively to its position and heading in the environment.
     *
     * @param node The node
     * @return Its shape
     */
    fun getShape(node: Node<T>): GeometricShape<S, A>

    /**
     * Gets all nodes whose shape.intersect is true for the given shape.
     * @param shape the shape
     * @return the set of nodes colliding with the given shape
     */
    fun getNodesWithin(shape: GeometricShape<S, A>): List<Node<T>>
}
