package it.unibo.alchemist.test

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.Segment2DImpl
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Intersection2D
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.Vector2D
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Segment2D
import org.danilopianini.lang.MathUtils
import org.junit.jupiter.api.Test

internal infix fun Double.shouldBeFuzzy(other: Double): Unit =
    MathUtils.fuzzyEquals(this, other) shouldBe true

internal fun Euclidean2DShapeFactory.oneOfEachWithSize(size: Double) =
    mapOf(
        "circle" to circle(size * 2),
        "circleSector" to circleSector(size * 2, Math.PI, 0.0),
        "rectangle" to rectangle(size, size),
        "adimensional" to adimensional()
    )

internal const val DEFAULT_SHAPE_SIZE: Double = 1.0

/**
 * Creates an [Euclidean2DPosition].
 */
fun coords(x: Double, y: Double) = Euclidean2DPosition(x, y)

/**
 * Creates a [Segment2D].
 */
fun segment(x1: Double, y1: Double, x2: Double, y2: Double) = Segment2DImpl(coords(x1, y1), coords(x2, y2))

class TestIntersections {

    private inline fun <P : Vector2D<P>, reified I : Intersection2D<P>> intersectionShouldBe(
        segment1: Segment2D<P>,
        segment2: Segment2D<P>,
        intersect: Segment2D<P>.(Segment2D<P>) -> Intersection2D<P>
    ): I {
        val intersection = segment1.intersect(segment2)
        intersection.shouldBeTypeOf<I>()
        return intersection as I
    }

    private inline fun <P : Vector2D<P>, reified I : Intersection2D<P>> linesIntersectionShouldBe(
        segment1: Segment2D<P>,
        segment2: Segment2D<P>
    ): I = intersectionShouldBe(segment1, segment2) { intersectAsLines(it) }

    private fun <P : Vector2D<P>> linesIntersectionShouldBe(
        segment1: Segment2D<P>,
        segment2: Segment2D<P>,
        expectedPoint: P
    ) {
        val intersection = linesIntersectionShouldBe<P, Intersection2D.SinglePoint<P>>(segment1, segment2)
        intersection.point shouldBe expectedPoint
    }

    private fun <P : Vector2D<P>> linesIntersectionShouldBeEmpty(segment1: Segment2D<P>, segment2: Segment2D<P>) =
        linesIntersectionShouldBe<P, Intersection2D.None>(segment1, segment2)

    private fun <P : Vector2D<P>> linesIntersectionShouldBeInfinite(segment1: Segment2D<P>, segment2: Segment2D<P>) =
        linesIntersectionShouldBe<P, Intersection2D.InfinitePoints>(segment1, segment2)

    private inline fun <P : Vector2D<P>, reified I : Intersection2D<P>> segmentsIntersectionShouldBe(
        segment1: Segment2D<P>,
        segment2: Segment2D<P>
    ): I = intersectionShouldBe(segment1, segment2) { intersectSegment(it) }

    private fun <P : Vector2D<P>> segmentsIntersectionShouldBe(
        segment1: Segment2D<P>,
        segment2: Segment2D<P>,
        expectedPoint: P
    ) {
        val intersection = segmentsIntersectionShouldBe<P, Intersection2D.SinglePoint<P>>(segment1, segment2)
        intersection.point shouldBe expectedPoint
    }

    private fun <P : Vector2D<P>> intersectionShouldBeEmpty(segment1: Segment2D<P>, segment2: Segment2D<P>) =
        segmentsIntersectionShouldBe<P, Intersection2D.None>(segment1, segment2)

    private fun <P : Vector2D<P>> intersectionShouldBeInfinite(segment1: Segment2D<P>, segment2: Segment2D<P>) =
        segmentsIntersectionShouldBe<P, Intersection2D.InfinitePoints>(segment1, segment2)

    private inline fun <P : Vector2D<P>, reified I : Intersection2D<P>> circleIntersectionShouldBe(
        segment: Segment2D<P>,
        center: P,
        radius: Double
    ): I {
        val intersection = segment.intersectCircle(center, radius)
        intersection.shouldBeTypeOf<I>()
        return intersection as I
    }

    private fun <P : Vector2D<P>> circleIntersectionShouldBe(
        segment: Segment2D<P>,
        center: P,
        radius: Double,
        expectedPoint1: P,
        expectedPoint2: P
    ) {
        val intersection = circleIntersectionShouldBe<P, Intersection2D.MultiplePoints<P>>(segment, center, radius)
        intersection.points shouldContainExactlyInAnyOrder listOf(expectedPoint1, expectedPoint2)
    }

    private fun <P : Vector2D<P>> circleIntersectionShouldBe(
        segment: Segment2D<P>,
        center: P,
        radius: Double,
        expectedPoint: P
    ) {
        val intersection = circleIntersectionShouldBe<P, Intersection2D.SinglePoint<P>>(segment, center, radius)
        intersection.point shouldBe expectedPoint
    }

    private fun <P : Vector2D<P>> circleIntersectionShouldBeEmpty(
        segment: Segment2D<P>,
        center: P,
        radius: Double
    ) = circleIntersectionShouldBe<P, Intersection2D.None>(segment, center, radius)

    @Test
    fun testLinesIntersection() {
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

    @Test
    fun testSegmentsIntersection() {
        /*
         * Plain intersection.
         */
        segmentsIntersectionShouldBe(
            segment(1.0, 1.0, 5.0, 5.0),
            segment(3.0, 1.0, 1.0, 3.0),
            coords(2.0, 2.0)
        )
        /*
         * Segments share an endpoint.
         */
        segmentsIntersectionShouldBe(
            segment(1.0, 1.0, 5.0, 5.0),
            segment(5.0, 5.0, 6.0, 1.0),
            coords(5.0, 5.0)
        )
        /*
         * Segments share an endpoint and are collinear.
         */
        segmentsIntersectionShouldBe(
            segment(1.0, 1.0, 5.0, 5.0),
            segment(5.0, 5.0, 6.0, 6.0),
            coords(5.0, 5.0)
        )
        /*
         * Segments are parallel.
         */
        intersectionShouldBeEmpty(
            segment(1.0, 1.0, 5.0, 5.0),
            segment(1.0, 2.0, 5.0, 6.0)
        )
        /*
         * Segments are not parallel but not intersecting as well.
         */
        intersectionShouldBeEmpty(
            segment(1.0, 1.0, 5.0, 5.0),
            segment(2.0, 3.0, 1.0, 5.0)
        )
        /*
         * Segments are collinear but disjoint.
         */
        intersectionShouldBeEmpty(
            segment(1.0, 1.0, 5.0, 5.0),
            segment(6.0, 6.0, 7.0, 7.0)
        )
        /*
         * Segments are coincident.
         */
        intersectionShouldBeInfinite(
            segment(1.0, 1.0, 5.0, 5.0),
            segment(1.0, 1.0, 5.0, 5.0)
        )
        /*
         * Overlapping.
         */
        intersectionShouldBeInfinite(
            segment(1.0, 1.0, 5.0, 5.0),
            segment(3.0, 3.0, 7.0, 7.0)
        )
        /*
         * Overlapping with negative coords.
         */
        intersectionShouldBeInfinite(
            segment(1.0, 1.0, 5.0, 5.0),
            segment(-3.0, -3.0, 4.0, 4.0)
        )
        /*
         * One contains the other.
         */
        intersectionShouldBeInfinite(
            segment(1.0, 1.0, 5.0, 5.0),
            segment(-3.0, -3.0, 7.0, 7.0)
        )
        /*
         * Overlapping and share an endpoint.
         */
        intersectionShouldBeInfinite(
            segment(1.0, 1.0, 5.0, 5.0),
            segment(3.0, 3.0, 5.0, 5.0)
        )
        /*
         * Intersections with axis-aligned segments.
         */
        segmentsIntersectionShouldBe(
            segment(1.0, 1.0, 5.0, 1.0),
            segment(3.0, -1.0, 3.0, 1.0),
            coords(3.0, 1.0)
        )
        segmentsIntersectionShouldBe(
            segment(1.0, 1.0, 5.0, 1.0),
            segment(3.0, -1.0, 3.0, 5.0),
            coords(3.0, 1.0)
        )
        segmentsIntersectionShouldBe(
            segment(1.0, 1.0, 5.0, 1.0),
            segment(5.0, 1.0, 6.0, 1.0),
            coords(5.0, 1.0)
        )
        /*
         * Aligned to the x-axis and overlapping.
         */
        intersectionShouldBeInfinite(
            segment(1.0, 1.0, 5.0, 1.0),
            segment(4.9, 1.0, 6.0, 1.0)
        )
        /*
         * Aligned to the x-axis and collinear but disjoint.
         */
        intersectionShouldBeEmpty(
            segment(1.0, 1.0, 5.0, 1.0),
            segment(6.0, 1.0, 7.0, 1.0)
        )
        /*
         * Aligned to the y-axis.
         */
        segmentsIntersectionShouldBe(
            segment(1.0, 1.0, 1.0, 6.0),
            segment(1.0, 1.0, 1.0, -6.0),
            coords(1.0, 1.0)
        )
        intersectionShouldBeEmpty(
            segment(1.0, 1.0, 1.0, 6.0),
            segment(1.0, -1.0, 1.0, -6.0)
        )
        intersectionShouldBeInfinite(
            segment(1.0, 1.0, 1.0, 6.0),
            segment(1.0, 2.0, 1.0, -6.0)
        )
        /*
         * Degenerate segments.
         */
        segmentsIntersectionShouldBe(
            segment(1.0, 1.0, 1.0, 1.0),
            segment(1.0, 1.0, 1.0, 1.0),
            coords(1.0, 1.0)
        )
        intersectionShouldBeEmpty(
            segment(1.0, 1.0, 1.0, 1.0),
            segment(1.0, 2.0, 1.0, 2.0)
        )
    }

    @Test
    fun testCircleSegmentIntersection() {
        circleIntersectionShouldBe(
            segment(1.0, 1.0, 5.0, 1.0),
            coords(3.0, 3.0),
            2.0,
            coords(3.0, 1.0)
        )
        circleIntersectionShouldBe(
            segment(1.0, -1.0, 5.0, -1.0),
            coords(3.0, 3.0),
            2.0
        )
        circleIntersectionShouldBe(
            segment(0.0, 3.0, 6.0, 3.0),
            coords(3.0, 3.0),
            2.0,
            coords(1.0, 3.0),
            coords(5.0, 3.0)
        )
        circleIntersectionShouldBe(
            segment(3.0, 3.0, 6.0, 3.0),
            coords(3.0, 3.0),
            2.0,
            coords(5.0, 3.0)
        )
        circleIntersectionShouldBe(
            segment(10.0, 3.0, 12.0, 3.0),
            coords(3.0, 3.0),
            2.0
        )
        circleIntersectionShouldBe(
            segment(0.0, 1.0, 1.0, 1.0),
            coords(1.0, 1.0),
            1.0,
            coords(0.0, 1.0)
        )
    }

    @Test
    fun testClosestPoint() {
        val segment = segment(1.0, 3.0, 3.0, 1.0)
        segment.closestPointTo(coords(4.0, 2.0)) shouldBe segment.second
        segment.closestPointTo(coords(4.0, 1.0)) shouldBe segment.second
        segment.closestPointTo(coords(3.0, 2.0)) shouldBe coords(2.5, 1.5)
    }
}
