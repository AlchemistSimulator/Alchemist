/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geometry.euclidean2d.util

import it.unibo.alchemist.model.positions.Euclidean2DPosition
import java.awt.Shape
import java.awt.geom.PathIterator

/**
 * Collection of extensions to Java AWT meant to simplify its usage as geometric engine.
 */
object AwtShapes {
    /**
     * When using java.awt.geom.PathIterator to iterate over the boundary of a
     * Shape, you need to pass an array of this size.
     */
    private const val ARRAY_SIZE_FOR_PATH_ITERATOR = 6

    /**
     * Obtains the vertices of a polygonal shape. Any curved segment connecting
     * two points will be considered as a straight line between them.
     */
    fun Shape.vertices(): List<Euclidean2DPosition> {
        val vertices = mutableListOf<Euclidean2DPosition>()
        val coords = DoubleArray(ARRAY_SIZE_FOR_PATH_ITERATOR)
        val iterator = getPathIterator(null)
        while (!iterator.isDone) {
            when (iterator.currentSegment(coords)) {
                PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO -> {
                    vertices.add(Euclidean2DPosition(coords[0], coords[1]))
                }
            }
            iterator.next()
        }
        return vertices
    }
}
