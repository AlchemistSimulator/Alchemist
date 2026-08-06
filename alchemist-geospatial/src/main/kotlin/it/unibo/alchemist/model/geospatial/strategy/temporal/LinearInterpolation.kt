/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.strategy.temporal

/**
 * [TemporalInterpolation] that blends between two adjacent values.
 */
class LinearInterpolation : TemporalInterpolation {
    override fun interpolate(valueBefore: Double, valueAfter: Double, weight: Double): Double = when {
        weight == 0.0 -> valueBefore
        weight == 1.0 -> valueAfter
        valueBefore.isNaN() || valueAfter.isNaN() -> Double.NaN
        else -> valueBefore + (valueAfter - valueBefore) * weight
    }

    private companion object {
        private const val serialVersionUID = 1L
    }
}
