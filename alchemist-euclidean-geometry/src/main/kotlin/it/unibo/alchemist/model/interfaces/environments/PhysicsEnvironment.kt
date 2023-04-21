package it.unibo.alchemist.model.interfaces.environments

import it.unibo.alchemist.model.EuclideanEnvironment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.geometry.GeometricShape
import it.unibo.alchemist.model.geometry.GeometricTransformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory

/**
 * An environment supporting physics and nodes shapes.
 * Note: due to the high number of parameters it's highly recommended not to use this interface directly,
 * but to create an apposite interface extending this one instead.
 *
 * @param <T> nodes concentration type
 * @param <P> positions type
 * @param <A> geometric transformations used in this environment
 * @param <F> factory of shapes compatible with this environment
 */
interface PhysicsEnvironment<T, P, A, F> : EuclideanEnvironment<T, P>
    where P : Position<P>,
          P : Vector<P>,
          A : GeometricTransformation<P>,
          F : GeometricShapeFactory<P, A> {
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
    fun getHeading(node: Node<T>): P

    /**
     * Sets the heading of a node.
     *
     * @param node The node
     * @param direction The direction vector.
     */
    fun setHeading(node: Node<T>, direction: P)

    /**
     * Gets the shape of a node relatively to its position and heading in the environment.
     *
     * @param node The node
     * @return Its shape
     */
    fun getShape(node: Node<T>): GeometricShape<P, A>

    /**
     * Gets all nodes whose shape.intersect is true for the given shape.
     *
     * @param shape the shape
     * @return the set of nodes colliding with the given shape
     */
    fun getNodesWithin(shape: GeometricShape<P, A>): List<Node<T>>

    /**
     * Computes the farthest position reachable by a [node] towards a [desiredPosition], avoiding node overlapping.
     * If no node is located in between, [desiredPosition] is returned. Otherwise, the first position where the node
     * collides with someone else is returned. For collision purposes, hitboxes are used: each node is given a circular
     * hitbox of radius equal to its shape's radius (shapeless nodes can't cause overlapping). The client can specify
     * a different radius for the hitbox of the moving node.
     */
    fun farthestPositionReachable(
        node: Node<T>,
        desiredPosition: P,
        hitboxRadius: Double = getShape(node).radius,
    ): P
}
