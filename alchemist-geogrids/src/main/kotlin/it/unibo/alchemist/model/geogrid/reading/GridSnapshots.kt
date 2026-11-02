/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.reading

import kotlin.time.Instant

/**
 * A sequence of [RasterGrid]s over time: `grid(i)` holds the data at `instants[i]`.
 */
interface GridSnapshots {
    /**
     * The instants of the grids, strictly ascending.
     */
    val instants: List<Instant>

    /**
     * The grid at the instant with the given [index] in [instants].
     */
    fun grid(index: Int): RasterGrid
}
