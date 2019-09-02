package it.unibo.alchemist.model.implementations.movestrategies

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy
import org.apache.commons.math3.random.RandomGenerator
import java.lang.Math.toRadians
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Strategy to move an object in a zigzag fashion. It uses euclidean geometry to determine the next position.
 *
 * [getCurrentPosition] must return the current position of the object to move.
 * [makePosition] must be able to create a 2D position given the X and Y coordinates in this order.
 * [rng] is the random number generator to use
 * [maxDistance] defines the maximum distance the object can move before a random change in direction is forced.
 * [minChangeInDirection] is the minimum change in the direction (in degrees) that must be made each time a new direction is chosen.
 *
 * [T] is the type of the concentration of the node used in the secondary constructor.
 */
class ZigZagRandomTarget<T>(
    private val getCurrentPosition: () -> Euclidean2DPosition,
    private val makePosition: (Double, Double) -> Euclidean2DPosition,
    private val rng: RandomGenerator,
    private val maxDistance: Double,
    private val minChangeInDirection: Double = 0.0
) : TargetSelectionStrategy<Euclidean2DPosition> {
    /**
     * Handy constructor for Alchemist where the object to move is a [node] in the [env].
     * [rng] is the random number generator to use
     * [maxDistance] defines the maximum distance the object can move before a random change in direction is forced.
     */
    constructor(node: Node<T>, env: Environment<T, Euclidean2DPosition>, rng: RandomGenerator, maxDistance: Double, minChangeInDirection: Double = 0.0) :
        this({ env.getPosition(node) }, { x, y -> env.makePosition(x, y) }, rng, maxDistance, minChangeInDirection)

    init {
        require(minChangeInDirection >= 0.0)
        require(minChangeInDirection == 0.0 || minChangeInDirection % 360.0 != 0.0) {
            "minChangeInDirection of 360 degrees makes no sense because no any change would be enough"
        }
        require(maxDistance >= 0.0)
    }

    private val minChangeInDirectionRadians = toRadians(minChangeInDirection)
    private var initialized = false
    private lateinit var lastNodePosition: Euclidean2DPosition
    private lateinit var startPosition: Euclidean2DPosition
    private val distance = 100.0 // the random destination is picked at this distance from the node
    private var direction = 0.0 // angle in radians

    override fun getTarget(): Euclidean2DPosition {
        val currentPosition = getCurrentPosition()
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

    private fun changeDirection() {
        var newDirection = 2 * Math.PI * rng.nextDouble()
        if (minChangeInDirection > 0.0 && abs(newDirection - direction) < minChangeInDirection) {
            // in the event a change in direction is not enough we force it to be so
            newDirection += minChangeInDirection * if (rng.nextBoolean()) 1.0 else -1.0
        }
        direction = newDirection
    }

    private fun computeTarget(): Euclidean2DPosition {
        val x = cos(direction) * distance
        val y = sin(direction) * sqrt(distance * distance + x * x)
        return makePosition(x, y) + getCurrentPosition()
    }
}
