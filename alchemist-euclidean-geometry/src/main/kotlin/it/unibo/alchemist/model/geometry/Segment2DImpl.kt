/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geometry

/**
 * Implementation of a [Segment2D].
 */
data class Segment2DImpl<P : Vector2D<P>>(override val first: P, override val second: P) : Segment2D<P> {
    override fun copyWith(first: P, second: P): Segment2D<P> = copy(first = first, second = second)
}
