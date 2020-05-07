/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.geometry

import org.danilopianini.lang.MathUtils
import java.lang.IllegalArgumentException

/**
 * Maps a value in [min, max] to [newMin, newMax].
 */
fun Double.map(min: Double, max: Double, newMin: Double, newMax: Double): Double = when {
    this !in min..max -> throw IllegalArgumentException("$this is not in [$min, $max]")
    MathUtils.fuzzyGreaterEquals(min, max) || MathUtils.fuzzyGreaterEquals(newMin, newMax) ->
        throw IllegalArgumentException("either [$min, $max] and/or [$newMin, $newMax] is empty")
    else -> (this - min) / (max - min) * (newMax - newMin) + newMin
}

/**
 * Checks if a double value is fuzzy contained in a range.
 * @param range the range
 * @returns true if the value is (fuzzy) contained in the range
 */
infix fun Double.fuzzyIn(range: ClosedRange<Double>): Boolean =
    /*
     * Tried to use fuzzyGreaterEquals and fuzzySmallerEquals, throws NoSuchMethodException at runtime, don't know why.
     */
    (MathUtils.fuzzyEquals(this, range.start) || this >= range.start) &&
    (MathUtils.fuzzyEquals(this, range.endInclusive) || this <= range.endInclusive)
