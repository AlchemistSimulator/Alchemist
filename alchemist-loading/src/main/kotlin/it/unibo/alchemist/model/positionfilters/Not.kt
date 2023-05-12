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
 * Negates the [positionBasedFilter]'s test.
 * @param [positionBasedFilter] the filter to be negated.
 */
class Not<P : Position<P>> (val positionBasedFilter: PositionBasedFilter<P>) : PositionBasedFilter<P> {
    /**
     * Returns true if [positionBasedFilter] is not satisfied.
     */
    override operator fun contains(position: P) = position !in positionBasedFilter
}
