/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geometry.shapes

import it.unibo.alchemist.model.geometry.Shape
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector

/**
 * A special shape which does not occupy space and does not intersect with any other, not even with itself.
 * It also ignores any transformation.
 */
class AdimensionalShape<S : Vector<S>, A : Transformation<S>>(
    override val centroid: S,
) : Shape<S, A> {

    override val diameter: Double = 0.0

    override fun intersects(other: Shape<S, A>) = false

    override fun contains(vector: S) = false

    /**
     * Any transformation is ignored.
     */
    override fun transformed(transformation: A.() -> Unit) = this
}
