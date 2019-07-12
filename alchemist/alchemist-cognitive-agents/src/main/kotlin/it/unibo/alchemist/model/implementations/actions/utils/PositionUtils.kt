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
 * Multiply each coordinate of this position by the given number.
 */
operator fun <P : Position<P>> P.times(n: Double) =
        this.cartesianCoordinates.map { it * n }.toTypedArray()

/**
 * Create a list of points equally distributed in the circle of given radius with center in this point.
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
 * Rotate a point around some other point by a given number of radians.
 */
fun <P : Position2D<P>> P.rotate(
    env: Environment<*, P>,
    center: P = env.makePosition(0.0, 0.0),
    radians: Double
): P = with(this - center) {
    env.makePosition(x * cos(radians) - y * sin(radians), y * cos(radians) + x * sin(radians))
} + center