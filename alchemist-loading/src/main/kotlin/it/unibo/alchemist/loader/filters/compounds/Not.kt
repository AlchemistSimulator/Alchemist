/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.filters.compounds

import it.unibo.alchemist.loader.filters.Filter
import it.unibo.alchemist.model.Position

/**
 * Negates the [filter]'s test.
 * @param [filter] the filter to be negated.
 */
class Not<P : Position<P>> (val filter: Filter<P>) : Filter<P> {
    /**
     * Returns true if [filter] is not satisfied.
     */
    override operator fun contains(position: P) = position !in filter
}
