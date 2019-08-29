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
    private val maxDistance: Double
) : TargetSelectionStrategy<Euclidean2DPosition> {

    private var initialized = false
    private lateinit var lastNodePosition: Euclidean2DPosition
    private lateinit var startPosition: Euclidean2DPosition
    private val distance = 100.0 // random destination picked at this distance from the node
    private var direction = changeDirection()

    override fun getTarget(): Euclidean2DPosition {
        val currentPosition = env.getPosition(node)
        if (!initialized) {
            lastNodePosition = currentPosition
            startPosition = currentPosition
            initialized = true
        }
        // if it hasn't moved (assuming it's because of an obstacle) or or it has gone farther than the maximum distance,
        // then pick another direction
        if (lastNodePosition == currentPosition || startPosition.getDistanceTo(currentPosition) >= maxDistance) {
            startPosition = currentPosition
            changeDirection()
        }
        lastNodePosition = currentPosition
        return computeTarget()
    }

    private fun changeDirection(): Euclidean2DPosition {
        direction = env.makePosition(2 * rng.nextDouble() - 1, 2 * rng.nextDouble() - 1)
        return direction
    }

    private fun computeTarget(): Euclidean2DPosition {
        val x = direction.x * distance
        val y = direction.y * sqrt(distance * distance + x * x)
        return env.makePosition(x, y) + env.getPosition(node)
    }
}
