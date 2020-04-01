package it.unibo.alchemist.model.implementations.environments

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import it.unibo.alchemist.model.interfaces.environments.HasBoundaries
import it.unibo.alchemist.model.interfaces.environments.RectangularBoundaries
import it.unibo.alchemist.model.interfaces.geometry.GeometricShape
import kotlin.math.abs

/**
 * A bounded [EuclideanPhysics2DEnvironment], nodes cannot exist nor move outside the rectangular boundaries centered
 * in (0,0).
 */
class Rectangular2DEnvironment<T>(
    /**
     * The environment's width limits the positions of the nodes inside a rectangle [width * height] centered in (0,0).
     */
    width: Double,
    /**
     * The environment's height limits the positions of the nodes inside a rectangle [width * height] centered in (0,0).
     */
    height: Double
) : Continuous2DEnvironment<T>(), HasBoundaries {

    override val boundaries = RectangularBoundaries(width, height)

    override fun nodeShouldBeAdded(node: Node<T>, position: Euclidean2DPosition) =
        isWithinBoundaries(position, node.shape) && super.nodeShouldBeAdded(node, position)

    override fun canNodeFitPosition(node: Node<T>, position: Euclidean2DPosition) =
        isWithinBoundaries(position, node.shape) && super.canNodeFitPosition(node, position)

    private fun isWithinBoundaries(pos: Euclidean2DPosition, shape: GeometricShape<*, *>) =
        abs(pos.x) +
            shape.diameter / 2 < boundaries.width / 2 && abs(pos.y) + shape.diameter / 2 < boundaries.height / 2
}
