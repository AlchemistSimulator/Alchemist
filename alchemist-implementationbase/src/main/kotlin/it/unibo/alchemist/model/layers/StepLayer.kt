/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.layers

import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.Position2D
import it.unibo.alchemist.model.observation.Observable

/**
 * A [Layer] with a discontinuous spatial distribution that never changes over time: the value is [maxValue] where
 * both coordinates exceed the thresholds [mx] and [my], and [minValue] elsewhere.
 *
 * @param T the type describing the concentration in this [Layer]
 * @param P [Position2D] type
 * @param mx the x value above which the concentration is at its maximum value
 * @param my the y value above which the concentration is at its maximum value
 * @param maxValue the high value of concentration
 * @param minValue the low value of concentration
 */
class StepLayer<T, P : Position2D<out P>>(
    private val mx: Double,
    private val my: Double,
    private val maxValue: T,
    private val minValue: T,
) : Layer<T, P> {
    /**
     * Builds a [StepLayer] whose concentration is at its [maxValue] in the first quadrant, for positive values of
     * both coordinates, and [minValue] elsewhere.
     */
    constructor(maxValue: T, minValue: T) : this(0.0, 0.0, maxValue, minValue)

    override fun getValue(p: P): T = if (p.x > mx && p.y > my) maxValue else minValue

    override fun observeValue(position: P): Observable<T> = Layer.constant(getValue(position))
}
