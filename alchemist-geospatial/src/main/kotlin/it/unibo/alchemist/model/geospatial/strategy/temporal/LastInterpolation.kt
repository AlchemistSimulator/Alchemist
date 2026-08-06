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
 * [TemporalInterpolation] that always resolves with the value of
 * the last temporal slice.
 */
class LastInterpolation : TemporalInterpolation {
    override fun interpolate(valueBefore: Double, valueAfter: Double, weight: Double): Double = valueBefore

    private companion object {
        private const val serialVersionUID = 1L
    }
}
