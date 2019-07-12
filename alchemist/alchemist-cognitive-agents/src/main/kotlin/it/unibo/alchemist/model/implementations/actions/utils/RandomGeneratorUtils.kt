package it.unibo.alchemist.model.implementations.actions.utils

import org.apache.commons.math3.random.RandomGenerator
import java.util.*

/**
 * Generate a random double between the given bounds.
 */
fun RandomGenerator.nextDouble(from: Double, to: Double) = nextDouble() * (to - from) - to

/**
 * Fisher–Yates shuffle algorithm using Apache random number generator.
 */
fun <R> Iterable<R>.shuffled(rg: RandomGenerator): Iterable<R> = toMutableList().apply {
    for (i in size - 1 downTo 1) {
        Collections.swap(this, i, rg.nextInt(i + 1))
    }
}