package it.unibo.alchemist.model.implementations.environments

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import kotlin.math.abs

/**
 * A bounded [EuclideanPhysics2DEnvironment], nodes cannot exist outside the rectangular boundaries defined by [height]
 * and [width] and centered in (0,0).
 */
class Rectangular2DEnvironment<T>(
    /**
     * The environment's width limits the positions of the nodes inside a rectangle [width * height] centered in (0,0)
     */
    val width: Double = Double.POSITIVE_INFINITY,
    /**
     * The environment's height limits the positions of the nodes inside a rectangle [width * height] centered in (0,0)
     */
    val height: Double = Double.POSITIVE_INFINITY
) : Continuous2DEnvironment<T>() {

    override fun nodeShouldBeAdded(node: Node<T>, position: Euclidean2DPosition) =
        isWithinWorldLimits(position) && super.nodeShouldBeAdded(node, position)

    override fun canNodeFitPosition(node: Node<T>, position: Euclidean2DPosition) =
        isWithinWorldLimits(position) && super.canNodeFitPosition(node, position)

    private fun isWithinWorldLimits(pos: Euclidean2DPosition) =
        abs(pos.x) < width / 2 && abs(pos.y) < height / 2
}