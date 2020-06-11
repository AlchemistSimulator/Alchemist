/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist

import java.util.Collections
import org.apache.commons.math3.random.RandomGenerator

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
 * [Fisher–Yates shuffle algorithm](https://bit.ly/33Z3xFu)
 * using Apache random number generator.
 * More information [on Wikipedia](https://en.wikipedia.org/wiki/Fisher-Yates_shuffle).
 *
 * @param rg
 *          the simulation {@link RandomGenerator}.
 */
fun <R> Iterable<R>.shuffled(rg: RandomGenerator): Iterable<R> = toMutableList().apply {
    for (i in size - 1 downTo 1) {
        Collections.swap(this, i, rg.nextInt(i + 1))
    }
}
