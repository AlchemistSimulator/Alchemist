/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.filters

import it.unibo.alchemist.model.Position
import java.util.function.Predicate

/**
 * Filtering condition for deployments. (e.g inject a [Module] in a node if [test]
 * is satisfied).
 */
interface Filter<P : Position<P>> : Predicate<P>, (P) -> Boolean {

    /**
     * Checks if the [position] is inside the shape.
     * @return true if the position is inside the [Filter].
     */
    operator fun contains(position: P): Boolean

    /**
     * Checks if the [position] is inside the shape.
     * @return true if the position is inside the [Filter].
     */
    override fun test(position: P) = contains(position)

    /**
     * Checks if the [position] is inside the shape.
     * @return true if the position is inside the [Filter].
     */
    override operator fun invoke(position: P): Boolean = contains(position)
}
