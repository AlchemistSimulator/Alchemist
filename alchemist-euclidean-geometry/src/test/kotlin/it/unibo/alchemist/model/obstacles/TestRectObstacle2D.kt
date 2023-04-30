/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.obstacles

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.geometry.euclidean2d.Segments.coords
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import org.apache.commons.math3.util.FastMath
import org.danilopianini.lang.MathUtils.fuzzyEquals

class TestRectObstacle2D : StringSpec({

    val obstacle = RectObstacle2D<Euclidean2DPosition>(2.0, 2.0, 4.0, 4.0)

    /*
     * Given a vector (startx, starty) -> (endx, endy), this method asserts that the
     * endpoint of the cut version obtained with [RectObstacle2D.next] is fuzzy equals
     * to the expected (nextx, nexty).
     */
    fun nextShouldBe(
        startx: Double,
        starty: Double,
        endx: Double,
        endy: Double,
        nextx: Double,
        nexty: Double,
    ) {
        val next = obstacle.next(coords(startx, starty), coords(endx, endy))
        fuzzyEquals(next.x, nextx) shouldBe true
        fuzzyEquals(next.y, nexty) shouldBe true
    }

    /*
     * Given a vector (startx, starty) -> (endx, endy), this method asserts that the
     * cut version obtained with [RectObstacle2D.next] is fuzzy equals to the original
     * vector.
     */
    fun vectorShouldNotBeCut(startx: Double, starty: Double, endx: Double, endy: Double) {
        nextShouldBe(startx, starty, endx, endy, endx, endy)
    }

    "vector not intersecting obstacle should not be cut" {
        vectorShouldNotBeCut(0.0, 3.0, 2.0, 7.0)
    }

    "vector intersecting obstacle should be cut" {
        nextShouldBe(0.0, 2.0, 4.0, 6.0, 2.0, 4.0)
        nextShouldBe(0.0, 3.0, 7.0, 3.0, 2.0, 3.0)
    }

    "movements on the obstacle's border should be allowed" {
        vectorShouldNotBeCut(3.0, 2.0, 5.0, 2.0)
        vectorShouldNotBeCut(3.0, 2.0, 6.0, 2.0)
        vectorShouldNotBeCut(6.0, 5.0, 6.0, 3.0)
        vectorShouldNotBeCut(6.0, 5.0, 6.0, 2.0)
    }

    "movements from the obstacle's vertices to points outside of it should be allowed" {
        vectorShouldNotBeCut(6.0, 6.0, 7.0, 7.0)
        vectorShouldNotBeCut(6.0, 6.0, 7.0, 6.0)
    }

    "movements from the obstacle's vertices to points inside of it shouldn't be allowed" {
        nextShouldBe(6.0, 6.0, 5.0, 5.0, 6.0, 6.0)
        nextShouldBe(6.0, 6.0, 0.0, 0.0, 6.0, 6.0)
    }

    "movements from the obstacle's vertices to points *close* to its border should be allowed" {
        /*
         * from vertex BL
         */
        vectorShouldNotBeCut(2.0, 2.0, 4.0, FastMath.nextDown(2.0))
        vectorShouldNotBeCut(2.0, 2.0, FastMath.nextDown(2.0), 4.0)
        /*
         * from vertex BR
         */
        vectorShouldNotBeCut(6.0, 2.0, 5.0, 1.9999999999999996)
        vectorShouldNotBeCut(6.0, 2.0, FastMath.nextUp(6.0), 4.0)
        /*
         * from vertex TR
         */
        vectorShouldNotBeCut(6.0, 6.0, 4.0, FastMath.nextUp(6.0))
        vectorShouldNotBeCut(6.0, 6.0, FastMath.nextUp(6.0), 4.0)
        /*
         * from vertex TL
         */
        vectorShouldNotBeCut(2.0, 6.0, 4.0, FastMath.nextUp(6.0))
        vectorShouldNotBeCut(2.0, 6.0, FastMath.nextDown(2.0), 4.0)
    }

    "nearestIntersection() should return the original end point when there's no intersection at all" {
        coords(2.0, 5.0).let { end ->
            obstacle.nearestIntersection(coords(1.0, 2.0), end) shouldBe end
        }
    }

    "movements from vertices towards the obstacle should be restricted to edges" {
        val minX = -5.3
        val maxX = -5.2574
        val minY = -5.3
        val maxY = 5.3074
        val subject = RectObstacle2D<Euclidean2DPosition>(minX, minY, maxX - minY, maxY - minY)
        val topRight = Euclidean2DPosition(-5.257399999999999, 5.3073999999999995)
        val desired1 = Euclidean2DPosition(-5.844256289989133, 6.172836637106708)
        subject.next(topRight, desired1) shouldBe desired1
        val desired2 = Euclidean2DPosition(-5.844256289989133, 5.3073999999999995)
        subject.next(topRight, desired2) shouldBe topRight
    }
})
