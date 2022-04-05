/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.util

import org.apache.commons.math3.random.RandomGenerator
import java.util.Collections

/**
 * Utilities that extend the functionality of [Iterable].
 */
object IterableExtension {

    /**
     * [Fisher–Yates shuffle algorithm](https://bit.ly/33Z3xFu)
     * using Apache random number generator.
     * More information [on Wikipedia](https://en.wikipedia.org/wiki/Fisher-Yates_shuffle).
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
     * Returns a random element of the Iterable
     */
    fun <R> Iterable<R>.randomElement(randomGenerator: RandomGenerator): R =
        with(toList()) { get(randomGenerator.nextInt(size)) }
}
