package it.unibo.alchemist.model.implementations.movestrategies

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.smartcam.randomAngle
import org.apache.commons.math3.distribution.LevyDistribution
import org.apache.commons.math3.random.RandomGenerator
import kotlin.math.cos
import kotlin.math.sin

/**
 * Selects a target based on a random direction extracted from [rng] (which should be an uniform random generator),
 * and a random distance extracted from a lévy distribution of parameters [location] (aka mu) and [scale] (aka c).
 * [getCurrentPosition] should return the current position of the object to move.
 * [T] is the type of the concentration of the node.
 */
class LevyWalkTarget<T>(
    getCurrentPosition: () -> Euclidean2DPosition,
    private val makePosition: (Double, Double) -> Euclidean2DPosition,
    private val rng: RandomGenerator,
    private val location: Double,
    private val scale: Double
) : ChangeTargetOnCollision<Euclidean2DPosition>(getCurrentPosition) {

    /**
     * Handy constructor for Alchemist where the object to move is a [node] in the [env].
     */
    constructor(node: Node<T>, env: Environment<T, Euclidean2DPosition>, rng: RandomGenerator, location: Double, scale: Double = 0.0) :
        this({ env.getPosition(node) }, { x, y -> env.makePosition(x, y) }, rng, location, scale)

    private val levy = LevyDistribution(rng, location, scale)

    override fun chooseTarget(): Euclidean2DPosition {
        val angle = rng.randomAngle()
        val distance = levy.sample()
        return getCurrentPosition() + makePosition(distance * cos(angle), distance * sin(angle))
    }
}