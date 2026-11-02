/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.strategy.temporal

/**
 * How to compute a value between two consecutive instants.
 */
fun interface TemporalInterpolation {

    /**
     * The value between [valueBefore] and [valueAfter], at [weight] from 0 (before) to 1 (after).
     */
    fun interpolate(valueBefore: Double, valueAfter: Double, weight: Double): Double
}
