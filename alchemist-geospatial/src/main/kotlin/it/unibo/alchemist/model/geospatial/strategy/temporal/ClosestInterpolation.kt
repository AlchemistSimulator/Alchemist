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
 * [TemporalInterpolationStrategy] that always resolves with the value of
 * the closest temporal slice.
 */
class ClosestInterpolation<T> : TemporalInterpolationStrategy<T> {
    override fun interpolate(valueBefore: T?, valueAfter: T?, weight: Double): T? =
        if (weight < 0.5) valueBefore else valueAfter
}

/**
 * [ClosestInterpolation] for [Double] type.
 */
class ClosestDoubleInterpolation(delegate: TemporalInterpolationStrategy<Double> = ClosestInterpolation()) :
    TemporalInterpolationStrategy<Double> by delegate
