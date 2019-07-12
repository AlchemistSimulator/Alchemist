package it.unibo.alchemist.model.implementations.actions.utils

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
fun RandomGenerator.nextDouble(from: Double, to: Double) = nextDouble() * (to - from) - to

/**
 * Fisher–Yates shuffle algorithm using Apache random number generator.
 * https://www.geeksforgeeks.org/shuffle-a-given-array-using-fisher-yates-shuffle-algorithm/
 *
 * @param rg
 *          the simulation {@link RandomGenerator}.
 */
fun <R> Iterable<R>.shuffled(rg: RandomGenerator): Iterable<R> = toMutableList().apply {
    for (i in size - 1 downTo 1) {
        Collections.swap(this, i, rg.nextInt(i + 1))
    }
}