/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.util

import org.danilopianini.lang.MathUtils

/**
 * Extension functions enriching [Double].
 */
object Doubles {

    /**
     * Checks if two double values are fuzzy equal.
     */
    fun Double.fuzzyEquals(other: Double): Boolean = MathUtils.fuzzyEquals(this, other)

    /**
     * Checks if a double value is fuzzy contained in a range.
     * @param range the range
     * @returns true if the value is (fuzzy) contained in the range
     */
    infix fun Double.fuzzyIn(range: ClosedRange<Double>): Boolean =
        /*
         * Tried to use fuzzyGreaterEquals and fuzzySmallerEquals,
         * throws NoSuchMethodException at runtime, don't know why.
         */
        (MathUtils.fuzzyEquals(this, range.start) || this >= range.start) &&
            (MathUtils.fuzzyEquals(this, range.endInclusive) || this <= range.endInclusive)
}
