package it.unibo.alchemist.model.deployments

import it.unibo.alchemist.model.Deployment
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position2D
import org.apache.commons.math3.random.RandomGenerator
import java.util.stream.IntStream
import java.util.stream.Stream
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Displaces the nodes in the [environment] in a circular arc, given a [nodeCount],
 * the coordinates of the circle's center [centerX] and [centerY],
 * the circle's radius [radius],
 * perturbation randomness (uniform, generated via [randomGenerator])
 * for radius [radiusRandomness] and for angle [angleRandomness],
 * a [startAngle], and an [endAngle].
 *
 * Default values generate a uniform deployment on a circumference.
 */
data class CircularArc<P : Position2D<P>> @JvmOverloads constructor(
    val environment: Environment<*, P>,
    val randomGenerator: RandomGenerator,
    val nodeCount: Int,
    val centerX: Double = 0.0,
    val centerY: Double = 0.0,
    val radius: Double = 1.0,
    val radiusRandomness: Double = 0.0,
    val angleRandomness: Double = 0.0,
    val startAngle: Double = 0.0,
    val endAngle: Double = 2 * PI,
) : Deployment<P> {
    private val step = (endAngle - startAngle) / nodeCount

    /**
     * @return a [Stream] over the positions of this [Deployment]
     */
    override fun stream(): Stream<P> = IntStream.range(0, nodeCount)
        .mapToDouble { step * it + startAngle } // actual angle
        .mapToObj {
            fun Double.randomized(magnitude: Double) = this * (1 + magnitude * randomGenerator.nextDouble())
            val actualRadius = radius.randomized(radiusRandomness)
            val actualAngle = it.randomized(angleRandomness)
            environment.makePosition(
                centerX + actualRadius * sin(actualAngle),
                centerY + actualRadius * cos(actualAngle),
            )
        }
}
