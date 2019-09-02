package it.unibo.alchemist.model.implementations.movestrategies

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.smartcam.randomAngle
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
    getCurrentPosition: () -> Euclidean2DPosition,
    private val makePosition: (Double, Double) -> Euclidean2DPosition,
    private val rng: RandomGenerator,
    private val maxDistance: Double,
    private val minChangeInDirection: Double = 0.0
) : ChangeTargetOnCollision<Euclidean2DPosition>(getCurrentPosition) {
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
    private lateinit var startPosition: Euclidean2DPosition
    private val distance = 100.0 // the random destination is picked at this distance from the node
    private var direction = 0.0 // angle in radians

    override fun initializePositions(currentPosition: Euclidean2DPosition) {
        startPosition = currentPosition
    }

    override fun shouldChangeTarget() = super.shouldChangeTarget() || getCurrentPosition().getDistanceTo(startPosition) >= maxDistance

    override fun chooseTarget() = with(changeDirection()) {
        val x = cos(this) * distance
        val y = sin(this) * sqrt(distance * distance + x * x)
        makePosition(x, y) + getCurrentPosition()
    }

    private fun changeDirection() = with(rng.randomAngle()) {
        if (minChangeInDirection > 0.0 && abs(this - direction) < minChangeInDirection) {
            // in the event a change in direction is not enough we force it to be so
            this + minChangeInDirection * if (rng.nextBoolean()) 1.0 else -1.0
        } else {
            this
        }.also {
            direction = it
        }
    }
}
