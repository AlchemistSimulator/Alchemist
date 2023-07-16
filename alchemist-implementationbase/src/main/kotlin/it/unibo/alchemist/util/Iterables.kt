/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.util

import org.apache.commons.math3.random.RandomGenerator
import java.util.Collections

/**
 * Utilities that extend the functionality of [Iterable].
 */
object Iterables {

    /**
     * Fisher–Yates shuffle algorithm
     * using a [RandomGenerator].
     * More information [on Wikipedia](https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle).
     *
     * @param randomGenerator
     *          the simulation {@link RandomGenerator}.
     */
    fun <R> Iterable<R>.shuffled(randomGenerator: RandomGenerator): Iterable<R> = toMutableList().apply {
        for (i in size - 1 downTo 1) {
            Collections.swap(this, i, randomGenerator.nextInt(i + 1))
        }
    }

    /**
     * Returns a random element of the Iterable using the provided [randomGenerator].
     */
    fun <R> Iterable<R>.randomElement(randomGenerator: RandomGenerator): R =
        with(toList()) { get(randomGenerator.nextInt(size)) }
}
