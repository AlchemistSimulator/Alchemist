package it.unibo.alchemist.model.implementations.utils

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import org.apache.commons.math3.random.RandomGenerator
import java.util.Collections

/**
 * Generate a random double between the given bounds.
 *
 * @param from
 *          the lower bound.
 * @param to
 *          the upper bound.
 */
fun RandomGenerator.nextDouble(from: Double, to: Double) = nextDouble() * (to - from) + from

/**
 * Generate a random Euclidean direction.
 */
fun RandomGenerator.direction() = Euclidean2DPosition(nextDouble(-1.0, 1.0), nextDouble(-1.0, 1.0))

/**
 * Generate a random Euclidean position.
 */
fun RandomGenerator.position() = direction().let {
    val distance = nextInt()
    Euclidean2DPosition(it.x * distance, it.y * distance)
}

/**
 * [Fisher–Yates shuffle algorithm](https://www.worldcat.org/title/statistical-tables-for-biological-agricultural-and-medical-research/oclc/14222135)
 * using Apache random number generator. More information [on Wikipedia](https://en.wikipedia.org/wiki/Fisher-Yates_shuffle).
 *
 * @param rg
 *          the simulation {@link RandomGenerator}.
 */
fun <R> Iterable<R>.shuffled(rg: RandomGenerator): Iterable<R> = toMutableList().apply {
    for (i in size - 1 downTo 1) {
        Collections.swap(this, i, rg.nextInt(i + 1))
    }
}
