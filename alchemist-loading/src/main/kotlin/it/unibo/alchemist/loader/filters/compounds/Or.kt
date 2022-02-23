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
 * Check if either [filterA] or [filterB] is satisfied.
 * @param filterA the first filter.
 * @param filterB the second filter.
 */
data class Or<T> (
    val filterA: Filter<T>,
    val filterB: Filter<T>,
) : Filter<T> {
    /**
     * Returns true if either [filterA] or [filterB] are satisfied.
     */
    override fun test(something: T): Boolean = filterA.test(something) || filterB.test(something)
}
