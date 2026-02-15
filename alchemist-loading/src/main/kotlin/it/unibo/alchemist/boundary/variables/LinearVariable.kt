/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.variables

import java.util.stream.DoubleStream
import java.util.stream.Stream
import kotlin.math.ceil
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A variable that spans linearly between a minimum and a maximum value.
 *
 * This is represented as a sequence of values starting at [min], incrementing by
 * [step], and not exceeding [max]. The [default] parameter is the default value for
 * the variable (it is not constrained to be exactly one of the generated steps).
 *
 * Note: kept as a data class for convenient structural equality and debugging;
 * the implementation preserves the original behavior of the prior class.
 *
 * @property default default value
 * @property min minimum value (inclusive)
 * @property max maximum value (inclusive)
 * @property step step increment between successive values
 */
data class LinearVariable(override val default: Double, val min: Double, val max: Double, val step: Double) :
    AbstractPrintableVariable<Double>() {

    init {
        require(max > min) { "The maximum value is smaller than the minimum in $this." }
        if (default !in min..max) {
            LOGGER.warn("Default value {} of linear variable is out of bounds: [{}, {}]", default, min, max)
        }
    }

    override fun steps(): Long {
        val steps = ceil((max - min) / step).toLong()
        if (min + step * steps <= max) {
            return steps + 1
        }
        return steps
    }

    override fun stream(): Stream<Double> = DoubleStream.iterate(min) { x: Double -> x + step }.limit(steps()).boxed()

    private companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(LinearVariable::class.java)
    }
}
