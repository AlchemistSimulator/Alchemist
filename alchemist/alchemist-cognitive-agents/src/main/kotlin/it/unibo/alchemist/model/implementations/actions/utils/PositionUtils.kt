package it.unibo.alchemist.model.implementations.actions.utils

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Position2D
import org.apache.commons.math3.random.RandomGenerator
import kotlin.math.cos
import kotlin.math.sin

/**
 * Create a position from an array of double representing the coordinates of the point
 */
fun <T, P : Position<P>> Environment<T, P>.makePosition(coords: Array<Double>): P = makePosition(*coords)

/**
 * Multiply each coordinate of this position by a number.
 *
 * @param n
 *          the second factor of each multiplication.
 */
operator fun <P : Position<P>> P.times(n: Double) =
        this.cartesianCoordinates.map { it * n }.toTypedArray()

/**
 * Create a list of points equally distributed in the circle of given radius with center in this position.
 *
 * @param env
 *          the environment containing the position to rotate.
 * @param rg
 *          the simulation {@link RandomGenerator}.
 * @param radius
 *          the distance each generated position must have from this.
 * @param quantity
 *          the number of positions to generate.
 */
fun <P : Position2D<P>> P.surrounding(
    env: Environment<*, P>,
    rg: RandomGenerator,
    radius: Double,
    quantity: Int
): List<P> = (1..quantity).map { it * Math.PI * 2 / quantity }
        .shuffled(rg)
        .map { env.makePosition(this.x + radius, y).rotate(env, this, it) }

/**
 * Perform the rotation of a position.
 *
 * @param env
 *          the environment containing the position to rotate.
 * @param center
 *          the position around which operate the rotation.
 * @param radians
 *          the number of radians representing the rotation angle.
 */
fun <P : Position2D<P>> P.rotate(
    env: Environment<*, P>,
    center: P = env.makePosition(0.0, 0.0),
    radians: Double
): P = with(this - center) {
    env.makePosition(x * cos(radians) - y * sin(radians), y * cos(radians) + x * sin(radians))
} + center