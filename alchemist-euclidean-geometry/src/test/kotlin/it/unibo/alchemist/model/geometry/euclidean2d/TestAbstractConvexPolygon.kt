/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geometry.euclidean2d

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.geometry.AwtMutableConvexPolygon
import it.unibo.alchemist.model.geometry.MutableConvexPolygon
import it.unibo.alchemist.model.geometry.euclidean2d.Segments.coords
import it.unibo.alchemist.model.geometry.euclidean2d.Segments.segment
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import java.awt.geom.Rectangle2D

/**
 * Tests all the "utils" methods implemented by AbstractConvexPolygon.
 */
class TestAbstractConvexPolygon : StringSpec({

    fun createPolygon(vararg coords: Euclidean2DPosition): MutableConvexPolygon =
        AwtMutableConvexPolygon(coords.toMutableList())

    fun createRectangle(x: Double, y: Double, width: Double, height: Double): MutableConvexPolygon =
        createPolygon(coords(x, y), coords(x + width, y), coords(x + width, y + height), coords(x, y + height))

    val polygon = createRectangle(1.0, 1.0, 5.0, 5.0)

    "test getEdge" {
        polygon.getEdge(0) shouldBe segment(1.0, 1.0, 6.0, 1.0)
        polygon.getEdge(3) shouldBe segment(1.0, 6.0, 1.0, 1.0)
    }

    "test liesOnBoundary" {
        polygon.liesOnBoundary(coords(3.0, 1.0)) shouldBe true
        polygon.liesOnBoundary(coords(6.0, 3.0)) shouldBe true
        polygon.liesOnBoundary(coords(7.0, 3.0)) shouldBe false
        polygon.liesOnBoundary(coords(1.0, 1.0)) shouldBe true
        polygon.liesOnBoundary(coords(1.0 - Double.MIN_VALUE, 1.0 - Double.MIN_VALUE)) shouldBe true
    }

    "test contains boundary included" {
        polygon.containsBoundaryIncluded(coords(1.0, 1.0)) shouldBe true
        polygon.containsBoundaryIncluded(coords(3.0, 3.0)) shouldBe true
        polygon.containsBoundaryIncluded(coords(0.0, 0.0)) shouldBe false
    }

    "test contains boundary excluded" {
        polygon.containsBoundaryExcluded(coords(1.0, 1.0)) shouldBe false
        polygon.containsBoundaryExcluded(coords(3.0, 1.0)) shouldBe false
        polygon.containsBoundaryExcluded(coords(3.0, 3.0)) shouldBe true
        polygon.containsBoundaryExcluded(coords(0.0, 0.0)) shouldBe false
    }

    "test contains shape" {
        polygon.contains(Rectangle2D.Double(2.0, 2.0, 3.0, 3.0)) shouldBe true
        polygon.contains(Rectangle2D.Double(1.0, 1.0, 5.0, 5.0)) shouldBe true
        polygon.contains(Rectangle2D.Double(0.0, 0.0, 3.0, 3.0)) shouldBe false
        polygon.contains(Rectangle2D.Double(0.0, 0.0, 7.0, 7.0)) shouldBe false
    }

    "test is adjacent to" {
        /*
         * Sides match perfectly.
         */
        polygon.isAdjacentTo(createRectangle(6.0, 1.0, 1.0, 5.0)) shouldBe true
        /*
         * Other's adjacent side is smaller.
         */
        polygon.isAdjacentTo(createRectangle(6.0, 2.0, 1.0, 3.0)) shouldBe true
        /*
         * Other's adjacent side is bigger.
         */
        polygon.isAdjacentTo(createRectangle(6.0, 0.0, 1.0, 10.0)) shouldBe true
        /*
         * Other is not axis-aligned and has a single vertex lying on the polygon's boundary.
         */
        polygon.isAdjacentTo(
            createPolygon(coords(6.0, 3.0), coords(7.0, 1.0), coords(10.0, 3.0)),
        ) shouldBe true
        polygon.isAdjacentTo(
            createRectangle(6.0 - Double.MIN_VALUE, 1.0 - Double.MIN_VALUE, 1.0, 5.0),
        ) shouldBe true
        polygon.isAdjacentTo(createRectangle(6.5, 1.0, 1.0, 5.0)) shouldBe false
        /*
         * Same polygon.
         */
        polygon.isAdjacentTo(polygon) shouldBe false
        /*
         * The polygon contains the other, and the latter has a vertex lying on the former's boundary.
         */
        polygon.isAdjacentTo(
            createPolygon(coords(6.0, 3.0), coords(3.0, 1.0), coords(1.0, 3.0)),
        ) shouldBe false
    }

    "test intersects segment" {
        /*
         * Segment outside the polygon and parallel to one of its edges.
         */
        polygon.intersects(segment(1.0, 7.0, 6.0, 7.0)) shouldBe false
        /*
         * Segment outside the polygon but not parallel to any edge.
         */
        polygon.intersects(segment(3.0, 7.0, 8.0, 8.0)) shouldBe false
        /*
         * Intersecting segment with both endpoints outside the polygon.
         */
        polygon.intersects(segment(0.0, 3.0, 3.0, 7.0)) shouldBe true
        /*
         * Intersecting segment with one endpoint on the polygon's boundary.
         */
        polygon.intersects(segment(0.0, 3.0, 3.0, 6.0)) shouldBe true
        /*
         * Intersecting segment with both endpoints on the polygon's boundary.
         */
        polygon.intersects(segment(1.0, 3.0, 3.0, 6.0)) shouldBe true
        /*
         * Intersecting segment with both endpoints inside the polygon.
         */
        polygon.intersects(segment(2.0, 2.0, 5.0, 5.0)) shouldBe true
        /*
         * Diagonal of the polygon.
         */
        polygon.intersects(segment(1.0, 1.0, 6.0, 6.0)) shouldBe true
        /*
         * Intersecting segment with one endpoint inside the polygon and the other coincident with a vertex.
         */
        polygon.intersects(segment(6.0, 6.0, 5.0, 5.0)) shouldBe true
        /*
         * Segment with one endpoint coincident with a vertex and the other outside the polygon.
         */
        polygon.intersects(segment(6.0, 6.0, 7.0, 7.0)) shouldBe false
        /*
         * Segments matching with one edge.
         */
        polygon.intersects(segment(0.0, 6.0, 7.0, 6.0)) shouldBe false
        polygon.intersects(segment(1.0, 6.0, 6.0, 6.0)) shouldBe false
        polygon.intersects(segment(2.0, 6.0, 5.0, 6.0)) shouldBe false
        /*
         * Segment only touching a vertex of the polygon.
         */
        polygon.intersects(segment(0.0, 5.0, 2.0, 7.0)) shouldBe false
    }

    "test closest edge to" {
        polygon.getEdge(0).let {
            polygon.closestEdgeTo(it) shouldBe it
            polygon.closestEdgeTo(segment(1.0, 0.0, 6.0, 0.0)) shouldBe it
            polygon.closestEdgeTo(segment(1.0, 0.0, 3.0, 0.0)) shouldBe it
            polygon.closestEdgeTo(segment(0.0, 0.0, 8.0, 0.0)) shouldBe it
            polygon.closestEdgeTo(segment(1.0, 0.0, 8.0, 0.0)) shouldBe it
            polygon.closestEdgeTo(segment(2.0, 0.0, 3.0, 0.0)) shouldBe it
        }
    }
})
