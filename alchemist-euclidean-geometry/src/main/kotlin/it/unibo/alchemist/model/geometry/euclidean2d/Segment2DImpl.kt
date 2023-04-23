/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geometry.euclidean2d

import it.unibo.alchemist.model.geometry.Vector2D
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Segment2D

/**
 * Implementation of a [Segment2D].
 */
data class Segment2DImpl<P : Vector2D<P>>(override val first: P, override val second: P) : Segment2D<P> {
    override fun copyWith(first: P, second: P): Segment2D<P> = copy(first = first, second = second)
}
