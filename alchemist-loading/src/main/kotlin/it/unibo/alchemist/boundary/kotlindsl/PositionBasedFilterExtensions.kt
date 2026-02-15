/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.kotlindsl

import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.PositionBasedFilter

/**
 * Enables the Kotlin `in` operator for [PositionBasedFilter] instances.
 *
 * This is a convenience extension that allows writing `position in filter` instead of
 * `filter.contains(position)`, even when the filter is available with a star-projected type.
 *
 * Since [PositionBasedFilter] is generic in the position type, this operator performs an unchecked cast to
 * [PositionBasedFilter]<[P]>. The caller is responsible for ensuring that the runtime filter expects positions
 * compatible with [P]; otherwise, the underlying implementation may throw at runtime or behave unexpectedly.
 *
 * @param P the position type.
 * @param position the position being tested for inclusion.
 * @return `true` if [position] is contained in this filter, `false` otherwise.
 */
@Suppress("UNCHECKED_CAST")
operator fun <P : Position<P>> PositionBasedFilter<*>.contains(position: P): Boolean =
    (this as PositionBasedFilter<P>).contains(position)
