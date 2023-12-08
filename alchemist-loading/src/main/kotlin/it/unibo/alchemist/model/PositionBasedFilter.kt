/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

import java.util.function.Predicate

/**
 * Filtering condition for deployments. (e.g., inject a [Module] in a node if [test]
 * is satisfied).
 */
interface PositionBasedFilter<P : Position<P>> : Predicate<P>, (P) -> Boolean {

    /**
     * Checks if the [position] is inside the shape.
     * @return true if the position is inside the [PositionBasedFilter].
     */
    operator fun contains(position: P): Boolean

    /**
     * Checks if the [position] is inside the shape.
     * @return true if the position is inside the [PositionBasedFilter].
     */
    override fun test(position: P) = contains(position)

    /**
     * Checks if the [position] is inside the shape.
     * @return true if the position is inside the [PositionBasedFilter].
     */
    override operator fun invoke(position: P): Boolean = contains(position)
}
