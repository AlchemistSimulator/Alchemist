/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geometry.euclidean2d

import it.unibo.alchemist.model.euclidean.geometry.Segment2DImpl
import it.unibo.alchemist.model.euclidean.positions.Euclidean2DPosition

object Segments {

    /**
     * Creates an [Euclidean2DPosition].
     */
    fun coords(x: Double, y: Double) = Euclidean2DPosition(x, y)

    /**
     * Creates a [Segment2DImpl].
     */
    fun segment(x1: Double, y1: Double, x2: Double, y2: Double) = Segment2DImpl(coords(x1, y1), coords(x2, y2))
}
