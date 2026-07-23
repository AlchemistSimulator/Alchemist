/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.strategy.missing

import it.unibo.alchemist.model.geospatial.reading.RasterGrid
import java.io.Serializable

/**
 * Strategy for **missing values**: what to return when a missing value is encountered
 * inside a [RasterGrid].
 *
 * @param T the type of the [RasterGrid].
 */
fun interface MissingValueStrategy<T> : Serializable {
    /**
     * @return the value to return in place of the missing one.
     */
    fun value(): T?
}
