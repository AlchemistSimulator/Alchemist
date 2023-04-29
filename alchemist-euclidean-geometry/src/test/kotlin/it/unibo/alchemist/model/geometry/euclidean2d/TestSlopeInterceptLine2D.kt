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
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import it.unibo.alchemist.model.euclidean.geometry.Intersection2D
import it.unibo.alchemist.model.euclidean.geometry.Line2D
import it.unibo.alchemist.model.euclidean.geometry.SlopeInterceptLine2D
import it.unibo.alchemist.model.euclidean.positions.Euclidean2DPosition
import it.unibo.alchemist.model.geometry.Vector2D
import it.unibo.alchemist.model.geometry.euclidean2d.Segments.coords
import it.unibo.alchemist.model.geometry.euclidean2d.Segments.segment
import org.junit.jupiter.api.assertThrows

class TestSlopeInterceptLine2D : StringSpec() {
    private val horizontalLine: Line2D<Euclidean2DPosition> = SlopeInterceptLine2D(0.0, 2.0, ::coords)
    private val verticalLine: Line2D<Euclidean2DPosition> = SlopeInterceptLine2D(2.0, ::coords)
    private val obliqueLine: Line2D<Euclidean2DPosition> = SlopeInterceptLine2D(1.0, 2.0, ::coords)

    private fun line(x1: Double, y1: Double, x2: Double, y2: Double) = segment(x1, y1, x2, y2).toLine()

    private inline fun <P : Vector2D<P>, reified I : Intersection2D<P>> intersectionShouldBe(
        line1: Line2D<P>,
        line2: Line2D<P>,
    ): I {
        val intersection = line1.intersect(line2)
        intersection.shouldBeTypeOf<I>()
        return intersection
    }

    private fun <P : Vector2D<P>> shouldIntersectIn(line1: Line2D<P>, line2: Line2D<P>, expectedPoint: P) =
        intersectionShouldBe<P, Intersection2D.SinglePoint<P>>(line1, line2).point shouldBe expectedPoint

    private fun <P : Vector2D<P>> shouldNotIntersect(line1: Line2D<P>, line2: Line2D<P>) =
        intersectionShouldBe<P, Intersection2D.None>(line1, line2)

    private fun <P : Vector2D<P>> shouldCoincide(line1: Line2D<P>, line2: Line2D<P>) =
        intersectionShouldBe<P, Intersection2D.InfinitePoints>(line1, line2)

    private inline fun <P : Vector2D<P>, reified I : Intersection2D<P>> intersectionShouldBe(
        line: Line2D<P>,
        center: P,
        radius: Double,
    ): I {
        val intersection = line.intersectCircle(center, radius)
        intersection.shouldBeTypeOf<I>()
        return intersection
    }

    private fun <P : Vector2D<P>> shouldIntersectIn(
        line: Line2D<P>,
        center: P,
        radius: Double,
        expectedPoint1: P,
        expectedPoint2: P,
    ) {
        val intersection = intersectionShouldBe<P, Intersection2D.MultiplePoints<P>>(line, center, radius)
        intersection.points shouldContainExactlyInAnyOrder listOf(expectedPoint1, expectedPoint2)
    }

    private fun <P : Vector2D<P>> shouldIntersectIn(line: Line2D<P>, center: P, radius: Double, expectedPoint: P) =
        intersectionShouldBe<P, Intersection2D.SinglePoint<P>>(line, center, radius).point shouldBe expectedPoint

    private fun <P : Vector2D<P>> shouldNotIntersect(line: Line2D<P>, center: P, radius: Double) =
        intersectionShouldBe<P, Intersection2D.None>(line, center, radius)

    init {
        "test is horizontal" {
            horizontalLine.isHorizontal shouldBe true
            verticalLine.isHorizontal shouldBe false
            obliqueLine.isHorizontal shouldBe false
        }

        "test is vertical" {
            horizontalLine.isVertical shouldBe false
            verticalLine.isVertical shouldBe true
            obliqueLine.isVertical shouldBe false
        }

        "test contains" {
            horizontalLine.contains(coords(2.0, 2.0)) shouldBe true
            horizontalLine.contains(coords(100.0, 2.0)) shouldBe true
            horizontalLine.contains(coords(2.0, 2.5)) shouldBe false
            verticalLine.contains(coords(2.0, 2.0)) shouldBe true
            verticalLine.contains(coords(2.0, 100.0)) shouldBe true
            verticalLine.contains(coords(2.5, 2.0)) shouldBe false
            obliqueLine.contains(coords(3.0, 5.0)) shouldBe true
            obliqueLine.contains(coords(-3.0, -1.0)) shouldBe true
            obliqueLine.contains(coords(4.0, 5.0)) shouldBe false
        }

        "test findPoint" {
            horizontalLine.findPoint(-100.0).y shouldBe 2.0
            horizontalLine.findPoint(100.0).y shouldBe 2.0
            assertThrows<UnsupportedOperationException> { verticalLine.findPoint(0.0) }
            obliqueLine.findPoint(2.0).y shouldBe 4.0
            obliqueLine.findPoint(-4.0).y shouldBe -2.0
        }

        "test isParallelTo" {
            horizontalLine.isParallelTo(verticalLine) shouldBe false
            horizontalLine.isParallelTo(obliqueLine) shouldBe false
            horizontalLine.isParallelTo(SlopeInterceptLine2D(0.0, 100.0, ::coords)) shouldBe true
            verticalLine.isParallelTo(horizontalLine) shouldBe false
            verticalLine.isParallelTo(obliqueLine) shouldBe false
            verticalLine.isParallelTo(SlopeInterceptLine2D(100.0, ::coords)) shouldBe true
            obliqueLine.isParallelTo(horizontalLine) shouldBe false
            obliqueLine.isParallelTo(verticalLine) shouldBe false
            obliqueLine.isParallelTo(SlopeInterceptLine2D(1.0, 0.0, ::coords)) shouldBe true
        }

        "test equals" {
            /*
             * Coincident lines but different objects.
             */
            (obliqueLine == SlopeInterceptLine2D(1.0, 2.0, ::coords)) shouldBe true
        }

        "test intersect" {
            shouldCoincide(
                line(1.0, 1.0, 2.0, 2.0),
                line(3.0, 3.0, 4.0, 4.0),
            )
            shouldNotIntersect(
                line(1.0, 1.0, 2.0, 2.0),
                line(2.0, 1.0, 3.0, 2.0),
            )
            shouldIntersectIn(
                line(1.0, 1.0, 2.0, 2.0),
                line(4.0, 1.0, 3.0, 2.0),
                coords(2.5, 2.5),
            )
            shouldCoincide(
                line(1.0, 1.0, 2.0, 1.0),
                line(3.0, 1.0, 4.0, 1.0),
            )
            shouldNotIntersect(
                line(1.0, 1.0, 2.0, 1.0),
                line(1.0, 2.0, 4.0, 2.0),
            )
            shouldIntersectIn(
                line(1.0, 1.0, 2.0, 1.0),
                line(1.0, 3.0, 2.0, 2.0),
                coords(3.0, 1.0),
            )
            shouldIntersectIn(
                line(1.0, 1.0, 2.0, 1.0),
                line(2.0, 3.0, 2.0, 2.0),
                coords(2.0, 1.0),
            )
            shouldCoincide(
                line(2.0, 5.0, 2.0, 6.0),
                line(2.0, 3.0, 2.0, 2.0),
            )
            shouldNotIntersect(
                line(1.0, 3.0, 1.0, 2.0),
                line(2.0, 3.0, 2.0, 2.0),
            )
            shouldIntersectIn(
                line(1.0, 3.0, 2.0, 2.0),
                line(2.0, 3.0, 2.0, 2.0),
                coords(2.0, 2.0),
            )
        }

        "test intersect circle" {
            /*
             * Horizontal line.
             */
            shouldIntersectIn(
                line(1.0, 1.0, 5.0, 1.0),
                coords(3.0, 3.0),
                2.0,
                coords(3.0, 1.0),
            )
            shouldNotIntersect(
                line(1.0, -1.0, 5.0, -1.0),
                coords(3.0, 3.0),
                2.0,
            )
            shouldIntersectIn(
                line(0.0, 3.0, 6.0, 3.0),
                coords(3.0, 3.0),
                2.0,
                coords(1.0, 3.0),
                coords(5.0, 3.0),
            )
            /*
             * Vertical line.
             */
            shouldIntersectIn(
                line(1.0, 1.0, 1.0, 5.0),
                coords(3.0, 3.0),
                2.0,
                coords(1.0, 3.0),
            )
            shouldNotIntersect(
                line(-1.0, 1.0, -1.0, 5.0),
                coords(3.0, 3.0),
                2.0,
            )
            shouldIntersectIn(
                line(3.0, 1.0, 3.0, 5.0),
                coords(3.0, 3.0),
                2.0,
                coords(3.0, 1.0),
                coords(3.0, 5.0),
            )
            /*
             * Oblique line.
             */
            shouldNotIntersect(
                line(0.0, 5.0, 1.0, 6.0),
                coords(3.0, 3.0),
                2.0,
            )
            shouldIntersectIn(
                line(1.0, 3.0, 3.0, 5.0),
                coords(3.0, 3.0),
                2.0,
                coords(1.0, 3.0),
                coords(3.0, 5.0),
            )
        }
    }
}
