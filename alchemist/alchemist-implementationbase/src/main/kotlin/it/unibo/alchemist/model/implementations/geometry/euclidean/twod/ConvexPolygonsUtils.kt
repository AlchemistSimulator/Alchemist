/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.geometry.euclidean.twod

import it.unibo.alchemist.model.implementations.geometry.SegmentsIntersectionTypes
import it.unibo.alchemist.model.implementations.geometry.contains
import it.unibo.alchemist.model.implementations.geometry.distanceTo
import it.unibo.alchemist.model.implementations.geometry.intersection
import it.unibo.alchemist.model.implementations.geometry.isInBoundaries
import it.unibo.alchemist.model.implementations.geometry.vertices
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DSegment
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.MutableConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.navigator.ExtendableConvexPolygon
import java.awt.Shape
import java.awt.geom.Point2D
import java.lang.IllegalStateException
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

/**
 * Returns a collection containing the edges (or sides) of the polygon.
 */
fun ConvexPolygon.edges() = vertices().indices.map { getEdge(it) }

/**
 * Checks whether the given vector is contained in the polygon or
 * lies on its boundary.
 */
fun ConvexPolygon.containsOrLiesOnBoundary(vector: Euclidean2DPosition): Boolean =
    contains(vector) || edges().any { it.contains(vector) }

/**
 * Adds a vertex to the polygon, linked to the (previous) last vertex.
 */
fun MutableConvexPolygon.addVertex(x: Double, y: Double): Boolean = addVertex(vertices().size, x, y)

/**
 * Checks if the provided segment intersects with the polygon, boundary excluded.
 */
fun ConvexPolygon.intersectsBoundaryExcluded(segment: Euclidean2DSegment): Boolean =
    edges()
        .map { intersection(it, segment) }
        .filter { it.type == SegmentsIntersectionTypes.POINT }
        .map { it.intersection.get() }
        .distinct()
        .size > 1

/**
 * Finds the edge of the polygon closest to the provided segment.
 */
fun ConvexPolygon.closestEdgeTo(segment: Euclidean2DSegment): Euclidean2DSegment =
    edges().minWith(
        compareBy({
            it.distanceTo(segment)
        }, {
            segment.distanceTo(it.first) + segment.distanceTo(it.second)
        })
    ) ?: throw IllegalStateException("no edge could be found")

/**
 * Advances the specified edge only if it remains inside the given region.
 * See [ExtendableConvexPolygon.advanceEdge].
 */
fun ExtendableConvexPolygon.advanceEdge(
    index: Int,
    step: Double,
    origin: Euclidean2DPosition,
    width: Double,
    height: Double
): Boolean {
    val oldEdge = getEdge(index)
    if (advanceEdge(index, step)) {
        if (isInBoundaries(getEdge(index), origin, width, height)) {
            return true
        }
        moveEdge(index, oldEdge)
    }
    return false
}
