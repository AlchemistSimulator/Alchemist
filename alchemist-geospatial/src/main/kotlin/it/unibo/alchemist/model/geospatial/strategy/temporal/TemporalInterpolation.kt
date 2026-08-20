/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.strategy.temporal

import java.io.Serializable

/**
 * Strategy for **temporal interpolation**: given the two **already** spatially resolved values of the
 * slices bracketing the current time, plus a normalized weight, produces the value at the current
 * time.
 */
fun interface TemporalInterpolation : Serializable {

    /**
     * @param valueBefore spatially resolved value of the slice at or immediately before the current time.
     * @param valueAfter spatially resolved value of the slice immediately after the current time.
     * @param weight normalized position in `[0.0, 1.0]`: `0.0` coincides with [valueBefore], `1.0` with [valueAfter].
     * @return the interpolated value.
     */
    fun interpolate(valueBefore: Double, valueAfter: Double, weight: Double): Double
}
