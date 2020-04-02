/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.geometry.euclidean.twod

import it.unibo.alchemist.model.implementations.geometry.vertices
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.MutableConvexPolygon
import java.awt.Shape
import java.util.Optional

/**
 * Creates a MutableConvexPolygon from a java.awt.Shape.
 * If the Polygon could not be created (e.g. because of the
 * non-convexity of the given shape), an empty optional is
 * returned.
 * Each curved segment of the shape will be considered as
 * a straight line.
 */
fun fromShape(shape: Shape): Optional<MutableConvexPolygon> {
    return try {
        Optional.of(MutableConvexPolygonImpl(shape.vertices().toMutableList()))
    } catch (e: IllegalArgumentException) {
        Optional.empty()
    }
}
