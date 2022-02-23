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

/**
 * Negates the [filter]'s test.
 * @param [filter] the filter to be negated.
 */
class Not<T> (
    val filter: Filter<T>
) : Filter<T> {
    /**
     * Returns true if [filter] is not satisfied.
     */
    override fun test(something: T): Boolean = !filter.test(something)
}
