/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.reading

import java.time.Instant

/**
 * A time-ordered sequence of [RasterGrid] slices backed by geophysical source data.
 *
 * [instants] and [grid] are aligned: the i-th instant is the real-world timestamp of the
 * slice returned by `grid(i)`. [instants] is guaranteed to be strictly ascending with no
 * duplicates. [instants] exposes [java.time.Instant] so that callers can convert
 * real-world timestamps to simulation time once at construction; [grid] is index-based
 * because temporal interpolation operates on adjacent indices, not timestamps directly.
 */
interface TimedGrid {
    /**
     * Real-world timestamps of each slice, strictly ascending, aligned with [grid].
     */
    val instants: List<Instant>

    /**
     * Returns the spatial slice at [index].
     *
     * @param index 0-based, aligned with [instants].
     */
    fun grid(index: Int): RasterGrid
}
