package it.unibo.alchemist.test

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.Intersection2D
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.intersectAsLines
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.Vector2D
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Segment2D
import org.danilopianini.lang.MathUtils
import org.junit.jupiter.api.Test
import kotlin.math.pow
import kotlin.math.sqrt

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
fun segment(x1: Double, y1: Double, x2: Double, y2: Double) = Segment2D(coords(x1, y1), coords(x2, y2))

class TestIntersections {

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
        linesIntersectionShouldBe<P, Intersection2D.Line>(segment1, segment2)

    private inline fun <P : Vector2D<P>, reified I : Intersection2D<P>> segmentIntersectionShouldBe(
        segment1: Segment2D<P>,
        segment2: Segment2D<P>
    ): I = intersectionShouldBe(segment1, segment2) { intersectSegment(it) }

    private inline fun <P : Vector2D<P>, reified I : Intersection2D<P>> intersectionShouldBe(
        segment1: Segment2D<P>,
        segment2: Segment2D<P>,
        intersect: Segment2D<P>.(Segment2D<P>) -> Intersection2D<P>
    ): I {
        val intersection = segment1.intersect(segment2)
        intersection.shouldBeTypeOf<I>()
        return intersection as I
    }

    private fun <P : Vector2D<P>> segmentIntersectionShouldBe(
        segment1: Segment2D<P>,
        segment2: Segment2D<P>,
        expectedPoint: P
    ) {
        val intersection = segmentIntersectionShouldBe<P, Intersection2D.SinglePoint<P>>(segment1, segment2)
        intersection.point shouldBe expectedPoint
    }

    private fun <P : Vector2D<P>> intersectionShouldBeEmpty(segment1: Segment2D<P>, segment2: Segment2D<P>) =
        segmentIntersectionShouldBe<P, Intersection2D.None>(segment1, segment2)

    private fun <P : Vector2D<P>> intersectionShouldBeInfinite(segment1: Segment2D<P>, segment2: Segment2D<P>) =
        segmentIntersectionShouldBe<P, Intersection2D.Segment<P>>(segment1, segment2)

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
        segmentIntersectionShouldBe(
            segment(1.0, 1.0, 5.0, 5.0),
            segment(3.0, 1.0, 1.0, 3.0),
            coords(2.0, 2.0)
        )
        /*
         * Segments share an endpoint.
         */
        segmentIntersectionShouldBe(
            segment(1.0, 1.0, 5.0, 5.0),
            segment(5.0, 5.0, 6.0, 1.0),
            coords(5.0, 5.0)
        )
        /*
         * Segments share an endpoint and are collinear.
         */
        segmentIntersectionShouldBe(
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
        segmentIntersectionShouldBe(
            segment(1.0, 1.0, 5.0, 1.0),
            segment(3.0, -1.0, 3.0, 1.0),
            coords(3.0, 1.0)
        )
        segmentIntersectionShouldBe(
            segment(1.0, 1.0, 5.0, 1.0),
            segment(3.0, -1.0, 3.0, 5.0),
            coords(3.0, 1.0)
        )
        segmentIntersectionShouldBe(
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
        segmentIntersectionShouldBe(
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
        segmentIntersectionShouldBe(
            segment(1.0, 1.0, 1.0, 1.0),
            segment(1.0, 1.0, 1.0, 1.0),
            coords(1.0, 1.0)
        )
        intersectionShouldBeEmpty(
            segment(1.0, 1.0, 1.0, 1.0),
            segment(1.0, 2.0, 1.0, 2.0)
        )
    }

    private fun <P : Vector2D<P>> segmentIntersectionShouldBe(
        segment: Segment2D<P>,
        center: P,
        radius: Double,
        expectedType: CircleSegmentIntersectionType,
        expectedPoint1: P? = null,
        expectedPoint2: P? = null
    ) {
        segment.intersectCircle(center, radius).let { intersection ->
            intersection.type shouldBe expectedType
            /*
             * Points can be provided in any order
             */
            mutableSetOf(intersection.point1, intersection.point2) shouldBe
                mutableSetOf(expectedPoint1, expectedPoint2)
        }
    }

    private fun <P : Vector2D<P>> segmentIntersectionShouldBe(
        segment: Segment2D<P>,
        center: P,
        radius: Double,
        expectedPoint1: P,
        expectedPoint2: P
    ) = segmentIntersectionShouldBe(
        segment,
        center,
        radius,
        CircleSegmentIntersectionType.PAIR,
        expectedPoint1,
        expectedPoint2
    )

    private fun <P : Vector2D<P>> segmentIntersectionShouldBe(
        segment: Segment2D<P>,
        center: P,
        radius: Double,
        expectedPoint: P
    ) = segmentIntersectionShouldBe(segment, center, radius, CircleSegmentIntersectionType.POINT, expectedPoint)

    private fun <P : Vector2D<P>> intersectionShouldBeEmpty(
        segment: Segment2D<P>,
        center: P,
        radius: Double
    ) = segmentIntersectionShouldBe(segment, center, radius, CircleSegmentIntersectionType.EMPTY)

    @Test
    fun testCircleSegmentIntersection() {
        segmentIntersectionShouldBe(
            segment(1.0, 1.0, 5.0, 1.0),
            coords(3.0, 3.0),
            2.0,
            coords(3.0, 1.0)
        )
        intersectionShouldBeEmpty(
            segment(1.0, -1.0, 5.0, -1.0),
            coords(3.0, 3.0),
            2.0
        )
        segmentIntersectionShouldBe(
            segment(0.0, 3.0, 6.0, 3.0),
            coords(3.0, 3.0),
            2.0,
            coords(1.0, 3.0),
            coords(5.0, 3.0)
        )
        segmentIntersectionShouldBe(
            segment(3.0, 3.0, 6.0, 3.0),
            coords(3.0, 3.0),
            2.0,
            coords(5.0, 3.0)
        )
        intersectionShouldBeEmpty(
            segment(10.0, 3.0, 12.0, 3.0),
            coords(3.0, 3.0),
            2.0
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

/**
 * Finds the intersection between a segment and a circle.
 */
private fun <P : Vector2D<P>> Segment2D<P>.intersectCircle(
    center: Vector2D<P>,
    radius: Double
): CircleSegmentIntersection<P> {
    fun <P : Vector2D<P>> intersectionPoint(segment: Segment2D<P>, vector: Vector2D<P>, t: Double): P? =
        if (t in 0.0..1.0 || MathUtils.fuzzyEquals(t, 0.0) || MathUtils.fuzzyEquals(t, 1.0)) {
            val x = segment.first.x + t * vector.x
            val y = segment.first.y + t * vector.y
            segment.first.newFrom(x, y)
        } else {
            null
        }
    val vector = toVector()
    /*
     * a, b and c are the terms of the 2nd grade equation of the intersection
     */
    val a = vector.x.pow(2) + vector.y.pow(2)
    val b = 2 * (vector.x * (first.x - center.x) + vector.y * (first.y - center.y))
    val c = (first.x - center.x).pow(2) + (first.y - center.y).pow(2) - radius.pow(2)
    val det = b.pow(2) - 4 * a * c
    return when {
        MathUtils.fuzzyEquals(a, 0.0) || a < 0.0 || det < 0.0 -> CircleSegmentIntersection.empty()
        MathUtils.fuzzyEquals(det, 0.0) -> {
            val t = -b / (2 * a)
            val p = intersectionPoint(this, vector, t)
            when {
                p == null -> CircleSegmentIntersection.empty()
                else -> CircleSegmentIntersection(p)
            }
        }
        else -> {
            val t1 = (-b + sqrt(det)) / (2 * a)
            val t2 = (-b - sqrt(det)) / (2 * a)
            val p1 = intersectionPoint(this, vector, t1)
            val p2 = intersectionPoint(this, vector, t2)
            CircleSegmentIntersection.create(p1, p2)
        }
    }
}

/**
 * Describes the result of the intersection between a circle and a segment in an euclidean space.
 *
 * @param type
 *              the type of intersection.
 * @param point1
 *              the first point of intersection (if present).
 * @param point2
 *              the second point of intersection (if present).
 */
data class CircleSegmentIntersection<P : Vector2D<P>>(
    val type: CircleSegmentIntersectionType,
    val point1: P? = null,
    val point2: P? = null
) {

    constructor(point: P) : this(CircleSegmentIntersectionType.POINT, point)

    companion object {
        /**
         * Creates an instance of [CircleSegmentIntersection] whose type is [CircleSegmentIntersectionType.EMPTY].
         */
        fun <P : Vector2D<P>> empty() =
            CircleSegmentIntersection<P>(
                CircleSegmentIntersectionType.EMPTY
            )

        /**
         * Creates an appropriate instance of [CircleSegmentIntersection], taking care that, in case the resulting
         * instance has type [CircleSegmentIntersectionType.POINT], such point is stored in [point1].
         */
        fun <P : Vector2D<P>> create(point1: P?, point2: P?): CircleSegmentIntersection<P> =
            when {
                point1 == null && point2 == null -> empty()
                point1 == null -> CircleSegmentIntersection(
                    point2!!
                ) // Necessarily not null
                point2 == null -> CircleSegmentIntersection(
                    point1
                )
                else -> CircleSegmentIntersection(
                    CircleSegmentIntersectionType.PAIR,
                    point1,
                    point2
                )
            }
    }
}

/**
 * In euclidean geometry, the intersection between a segment and a circle can be an [EMPTY]
 * set, a [POINT] or a [PAIR] of points.
 */
enum class CircleSegmentIntersectionType {
    EMPTY,
    POINT,
    PAIR
}
