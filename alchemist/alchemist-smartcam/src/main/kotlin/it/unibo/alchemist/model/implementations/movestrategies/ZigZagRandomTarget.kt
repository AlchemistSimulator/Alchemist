package it.unibo.alchemist.model.implementations.movestrategies

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy
import org.apache.commons.math3.random.RandomGenerator
import kotlin.math.sqrt

/**
 * Strategy to move a node in a zigzag direction.
 *
 * @param <T> concentration type
 * @param node the node
 * @param env the environment containing the node
 * @param rng the random number generator to use
 * @param distance distance to travel before picking another destination
 */
class ZigZagRandomTarget<T> (
    private val node: Node<T>,
    private val env: Environment<T, Euclidean2DPosition>,
    private val rng: RandomGenerator,
    private val distance: Double
) : TargetSelectionStrategy<Euclidean2DPosition> {
    private var target: Euclidean2DPosition? = null
    private var lastNodePosition = env.getPosition(node)


    override fun getTarget(): Euclidean2DPosition {
        val currentPosition = env.getPosition(node)
        if (target == null || lastNodePosition == currentPosition || currentPosition == target) {
            // if it hasn't moved (assuming it's because of an obstacle) or it has reached the target then pick another one
            target = pickRandomDestination()
        }
        lastNodePosition = currentPosition
        return target!! // it cannot be null here
    }

    private fun pickRandomDestination(): Euclidean2DPosition {
        val x = (2 * rng.nextDouble() -1) * distance
        val y = (2 * rng.nextInt(2) -1) * sqrt(distance * distance + x * x)
        return Euclidean2DPosition(x, y) + env.getPosition(node)
    }
}
