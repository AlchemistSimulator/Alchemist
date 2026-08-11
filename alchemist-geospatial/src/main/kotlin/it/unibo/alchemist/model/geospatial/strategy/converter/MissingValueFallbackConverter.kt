/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.strategy.converter

/**
 * A decorator for [MeasurementConverter] that handles missing/fill values,
 * represented as [Double.NaN], by replacing them with a [defaultValue].
 *
 * @param delegate the inner [MeasurementConverter] used for valid numerical values.
 * @param defaultValue the fallback value returned when encountering [Double.NaN].
 *
 * @param T the output type of the measurement conversion.
 */
class MissingValueFallbackConverter<T>(private val delegate: MeasurementConverter<T>, private val defaultValue: T) :
    MeasurementConverter<T> {

    /**
     * Converts the given [value].
     *
     * Returns [defaultValue] if [value] is [Double.NaN]; otherwise forwards
     * the conversion to [delegate].
     *
     * @param value the raw input value to convert.
     * @return the converted value of type [T], or [defaultValue] if the input was [Double.NaN].
     */
    override fun convert(value: Double): T = if (value.isNaN()) defaultValue else delegate.convert(value)

    private companion object {
        private const val serialVersionUID = 1L
    }
}
