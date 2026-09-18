/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.strategy.converter

import java.io.Serializable

/**
 * Converts a raw `Double` value read from a GRIB / NetCDF file, into a value of type [T].
 *
 * Implementations of this interface must handle the conversion logic,
 * taking into account that any missing/fill value is represented by a [Double.NaN].
 *
 * @param T the data type resulting from the conversion.
 */
fun interface MeasurementConverter<T> : Serializable {

    /**
     * Converts the raw `Double` value to [T].
     *
     * @param value the numeric value to be converted.
     * **Note:** it may be [Double.NaN] if it represents a missing/fill value.
     * @return the converted value of type [T].
     */
    fun convert(value: Double): T
}
