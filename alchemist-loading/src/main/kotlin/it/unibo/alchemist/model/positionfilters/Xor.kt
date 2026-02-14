/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.positionfilters

import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.PositionBasedFilter

/**
 * Position-based filter that matches when exactly one of two underlying filters
 * matches the provided position (exclusive-or behaviour).
 *
 * @param P the position type
 * @property positionBasedFilterA the first filter
 * @property positionBasedFilterB the second filter
 */
data class Xor<P : Position<P>>(
    val positionBasedFilterA: PositionBasedFilter<P>,
    val positionBasedFilterB: PositionBasedFilter<P>,
) : PositionBasedFilter<P> {
    /**
     * Returns true if exactly one of [positionBasedFilterA] or [positionBasedFilterB]
     * contains the given [position].
     */
    override operator fun contains(position: P) = position in positionBasedFilterA != position in positionBasedFilterB
}
