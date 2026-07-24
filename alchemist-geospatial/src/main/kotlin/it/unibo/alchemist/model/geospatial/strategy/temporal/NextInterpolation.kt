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
 * the next temporal slice.
 */
class NextInterpolation<T> : TemporalInterpolationStrategy<T> {
    override fun interpolate(valueBefore: T?, valueAfter: T?, weight: Double): T? = valueAfter
}

/**
 * [NextInterpolation] for [Double] type.
 */
class NextDoubleInterpolation(delegate: TemporalInterpolationStrategy<Double> = NextInterpolation()) :
    TemporalInterpolationStrategy<Double> by delegate
