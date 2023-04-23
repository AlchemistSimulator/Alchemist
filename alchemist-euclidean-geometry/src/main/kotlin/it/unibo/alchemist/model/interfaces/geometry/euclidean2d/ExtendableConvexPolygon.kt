/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.geometry.euclidean2d

/**
 * A sophisticated [MutableConvexPolygon].
 */
interface ExtendableConvexPolygon : MutableConvexPolygon {

    /**
     * Advances an edge in its normal direction.
     * @param index the index of the edge to advance (edge i connects vertices i and i+1)
     * @param step the length of the vector that will be used to advance the edge, negative
     * values are supported and will shrink the polygon instead of extending it.
     * @returns true if the operation was performed successfully, false otherwise
     * (e.g. because it would have caused the loss of convexity)
     */
    fun advanceEdge(index: Int, step: Double): Boolean

    /**
     * Extends the polygon in each direction: each edge is given a chance to advance.
     * @param step the length of the vector that will be used to advance each edge, negative
     * values are supported and will shrink the polygon instead of extending it.
     * @returns true if at least one edge advanced
     */
    fun extend(step: Double): Boolean
}
