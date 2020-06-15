/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.SlopeInterceptLine2D
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.Vector2D
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Intersection2D
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Line2D
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Segment2D
import org.junit.jupiter.api.assertThrows

internal inline fun <P : Vector2D<P>, reified I : Intersection2D<P>> intersectionShouldBe(
    segment1: Segment2D<P>,
    segment2: Segment2D<P>,
    asLines: Boolean
): I {
    val intersection = if (asLines) {
        segment1.toLine().intersect(segment2.toLine())
    } else {
        segment1.intersect(segment2)
    }
    intersection.shouldBeTypeOf<I>()
    return intersection as I
}

internal inline fun <P : Vector2D<P>, reified I : Intersection2D<P>> circleIntersectionShouldBe(
    segment: Segment2D<P>,
    center: P,
    radius: Double,
    asLine: Boolean
): I {
    val intersection = if (asLine) {
        segment.toLine().intersectCircle(center, radius)
    } else {
        segment.intersectCircle(center, radius)
    }
    intersection.shouldBeTypeOf<I>()
    return intersection as I
}

class TestSlopeInterceptLine2D : StringSpec() {
    private val horizontalLine: Line2D<Euclidean2DPosition> = SlopeInterceptLine2D(0.0, 2.0, ::coords)
    private val verticalLine: Line2D<Euclidean2DPosition> = SlopeInterceptLine2D(2.0, ::coords)
    private val obliqueLine: Line2D<Euclidean2DPosition> = SlopeInterceptLine2D(1.0, 2.0, ::coords)

    private fun <P : Vector2D<P>> linesIntersectionShouldBe(
        segment1: Segment2D<P>,
        segment2: Segment2D<P>,
        expectedPoint: P
    ) {
        val intersection = intersectionShouldBe<P, Intersection2D.SinglePoint<P>>(segment1, segment2, true)
        intersection.point shouldBe expectedPoint
    }

    private fun <P : Vector2D<P>> linesIntersectionShouldBeEmpty(segment1: Segment2D<P>, segment2: Segment2D<P>) =
        intersectionShouldBe<P, Intersection2D.None>(segment1, segment2, true)

    private fun <P : Vector2D<P>> linesIntersectionShouldBeInfinite(segment1: Segment2D<P>, segment2: Segment2D<P>) =
        intersectionShouldBe<P, Intersection2D.InfinitePoints>(segment1, segment2, true)

    private fun <P : Vector2D<P>> lineCircleIntersectionShouldBe(
        segment: Segment2D<P>,
        center: P,
        radius: Double,
        expectedPoint1: P,
        expectedPoint2: P
    ) {
        val intersection = circleIntersectionShouldBe<P, Intersection2D.MultiplePoints<P>>(
            segment,
            center,
            radius,
            true
        )
        intersection.points shouldContainExactlyInAnyOrder listOf(expectedPoint1, expectedPoint2)
    }

    private fun <P : Vector2D<P>> lineCircleIntersectionShouldBe(
        segment: Segment2D<P>,
        center: P,
        radius: Double,
        expectedPoint: P
    ) {
        val intersection = circleIntersectionShouldBe<P, Intersection2D.SinglePoint<P>>(
            segment,
            center,
            radius,
            true
        )
        intersection.point shouldBe expectedPoint
    }

    private fun <P : Vector2D<P>> lineCircleIntersectionShouldBeEmpty(
        segment: Segment2D<P>,
        center: P,
        radius: Double
    ) = circleIntersectionShouldBe<P, Intersection2D.None>(segment, center, radius, true)

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
            linesIntersectionShouldBeInfinite(
                segment(1.0, 1.0, 2.0, 2.0),
                segment(3.0, 3.0, 4.0, 4.0)
            )
            linesIntersectionShouldBeEmpty(
                segment(1.0, 1.0, 2.0, 2.0),
                segment(2.0, 1.0, 3.0, 2.0)
            )
            linesIntersectionShouldBe(
                segment(1.0, 1.0, 2.0, 2.0),
                segment(4.0, 1.0, 3.0, 2.0),
                coords(2.5, 2.5)
            )
            linesIntersectionShouldBeInfinite(
                segment(1.0, 1.0, 2.0, 1.0),
                segment(3.0, 1.0, 4.0, 1.0)
            )
            linesIntersectionShouldBeEmpty(
                segment(1.0, 1.0, 2.0, 1.0),
                segment(1.0, 2.0, 4.0, 2.0)
            )
            linesIntersectionShouldBe(
                segment(1.0, 1.0, 2.0, 1.0),
                segment(1.0, 3.0, 2.0, 2.0),
                coords(3.0, 1.0)
            )
            linesIntersectionShouldBe(
                segment(1.0, 1.0, 2.0, 1.0),
                segment(2.0, 3.0, 2.0, 2.0),
                coords(2.0, 1.0)
            )
            linesIntersectionShouldBeInfinite(
                segment(2.0, 5.0, 2.0, 6.0),
                segment(2.0, 3.0, 2.0, 2.0)
            )
            linesIntersectionShouldBeEmpty(
                segment(1.0, 3.0, 1.0, 2.0),
                segment(2.0, 3.0, 2.0, 2.0)
            )
            linesIntersectionShouldBe(
                segment(1.0, 3.0, 2.0, 2.0),
                segment(2.0, 3.0, 2.0, 2.0),
                coords(2.0, 2.0)
            )
        }

        "test intersect circle" {
            /*
             * Horizontal line.
             */
            lineCircleIntersectionShouldBe(
                segment(1.0, 1.0, 5.0, 1.0),
                coords(3.0, 3.0),
                2.0,
                coords(3.0, 1.0)
            )
            lineCircleIntersectionShouldBeEmpty(
                segment(1.0, -1.0, 5.0, -1.0),
                coords(3.0, 3.0),
                2.0
            )
            lineCircleIntersectionShouldBe(
                segment(0.0, 3.0, 6.0, 3.0),
                coords(3.0, 3.0),
                2.0,
                coords(1.0, 3.0),
                coords(5.0, 3.0)
            )
            /*
             * Vertical line.
             */
            lineCircleIntersectionShouldBe(
                segment(1.0, 1.0, 1.0, 5.0),
                coords(3.0, 3.0),
                2.0,
                coords(1.0, 3.0)
            )
            lineCircleIntersectionShouldBeEmpty(
                segment(-1.0, 1.0, -1.0, 5.0),
                coords(3.0, 3.0),
                2.0
            )
            lineCircleIntersectionShouldBe(
                segment(3.0, 1.0, 3.0, 5.0),
                coords(3.0, 3.0),
                2.0,
                coords(3.0, 1.0),
                coords(3.0, 5.0)
            )
            /*
             * Oblique line.
             */
            lineCircleIntersectionShouldBeEmpty(
                segment(0.0, 5.0, 1.0, 6.0),
                coords(3.0, 3.0),
                2.0
            )
            lineCircleIntersectionShouldBe(
                segment(1.0, 3.0, 3.0, 5.0),
                coords(3.0, 3.0),
                2.0,
                coords(1.0, 3.0),
                coords(3.0, 5.0)
            )
        }
    }
}
