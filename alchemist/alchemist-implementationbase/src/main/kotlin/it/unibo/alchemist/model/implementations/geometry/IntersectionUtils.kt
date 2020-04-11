package it.unibo.alchemist.model.implementations.geometry

import it.unibo.alchemist.model.interfaces.geometry.Vector2D
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Segment2D
import org.danilopianini.lang.MathUtils.fuzzyEquals
import java.util.Optional
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * In euclidean geometry, the intersection of two lines can be an [EMPTY] set, a [POINT],
 * or a [LINE] (in other words, infinite points).
 */
enum class LinesIntersectionType {
    EMPTY,
    POINT,
    LINE
}

/**
 * Describes the result of the intersection between two lines in an euclidean space.
 *
 * @param type
 *              the type of intersection.
 * @param point
 *              the intersection point (if present).
 */
data class LinesIntersection<P : Vector2D<P>>(
    val type: LinesIntersectionType,
    val point: Optional<P> = Optional.empty()
) {
    constructor(point: P) : this(LinesIntersectionType.POINT, Optional.of(point))

    companion object {
        /**
         * Creates an instance of [LinesIntersection] whose type is [LinesIntersectionType.EMPTY].
         */
        fun <P : Vector2D<P>> empty() = LinesIntersection<P>(LinesIntersectionType.EMPTY)

        /**
         * Creates an instance of [LinesIntersection] whose type is [LinesIntersectionType.LINE].
         */
        fun <P : Vector2D<P>> line() = LinesIntersection<P>(LinesIntersectionType.LINE)
    }
}

/**
 * Finds the intersection of two lines represented by segments. Degenerate segments (of zero
 * length) are not supported.
 */
fun <P : Vector2D<P>> linesIntersection(s1: Segment2D<P>, s2: Segment2D<P>): LinesIntersection<P> {
    require(!s1.isDegenerate && !s2.isDegenerate) { "degenerate segments are not lines" }
    val m1 = s1.slope
    val q1 = s1.intercept
    val m2 = s2.slope
    val q2 = s2.intercept
    return when {
        coincide(m1, m2, q1, q2, s1, s2) -> LinesIntersection.line()
        areParallel(m1, m2) -> LinesIntersection.empty()
        else -> {
            val intersection = when {
                s1.yAxisAligned -> s1.first.newFrom(s1.first.x, m2 * s1.first.x + q2)
                s2.yAxisAligned -> s1.first.newFrom(s2.first.x, m1 * s2.first.x + q1)
                else -> {
                    val x = (q2 - q1) / (m1 - m2)
                    val y = m1 * x + q1
                    s1.first.newFrom(x, y)
                }
            }
            LinesIntersection(intersection)
        }
    }
}

private fun coincide(m1: Double, m2: Double, q1: Double, q2: Double, s1: Segment2D<*>, s2: Segment2D<*>) =
    when {
        !areParallel(m1, m2) -> false
        s1.yAxisAligned && s2.yAxisAligned -> fuzzyEquals(s1.first.x, s2.first.x)
        else -> fuzzyEquals(q1, q2)
    }

private fun areParallel(m1: Double, m2: Double) =
    (m1.isInfinite() && m2.isInfinite()) || (m1.isFinite() && m2.isFinite() && fuzzyEquals(m1, m2))

/**
 * In euclidean geometry, the intersection of two segments can be an [EMPTY] set, a [POINT], or a
 * [SEGMENT] (in other words, infinite points).
 */
enum class SegmentsIntersectionType {
    EMPTY,
    POINT,
    /**
     * Note that two segments may be collinear, overlapping and share a single point (e.g. an
     * endpoint). In this case the intersection type is [POINT].
     */
    SEGMENT
}

/**
 * Describes the result of the intersection between two segments in an euclidean space.
 *
 * @param type
 *              the type of intersection.
 * @param point
 *              the intersection point (if present).
 */
data class SegmentsIntersection<P : Vector2D<P>>(
    val type: SegmentsIntersectionType,
    val point: Optional<P> = Optional.empty()
) {
    constructor(point: P) : this(SegmentsIntersectionType.POINT, Optional.of(point))

    companion object {
        /**
         * Creates an instance of [SegmentsIntersection] whose type is [SegmentsIntersectionType.EMPTY].
         */
        fun <P : Vector2D<P>> empty() = SegmentsIntersection<P>(SegmentsIntersectionType.EMPTY)

        /**
         * Creates an instance of [SegmentsIntersection] whose type is [SegmentsIntersectionType.SEGMENT].
         */
        fun <P : Vector2D<P>> segment() = SegmentsIntersection<P>(SegmentsIntersectionType.SEGMENT)
    }
}

/**
 * Finds the intersection point of two given segments. This method is able to deal with degenerate
 * and collinear segments.
 */
fun <P : Vector2D<P>> intersection(s1: Segment2D<P>, s2: Segment2D<P>): SegmentsIntersection<P> {
    if (s1.isDegenerate || s2.isDegenerate) {
        val degenerate = s1.takeIf { it.isDegenerate } ?: s2
        val other = s2.takeIf { degenerate == s1 } ?: s1
        return when {
            other.contains(degenerate.first) -> SegmentsIntersection(degenerate.first)
            else -> SegmentsIntersection.empty()
        }
    }
    val intersection = linesIntersection(s1, s2)
    return when {
        intersection.type == LinesIntersectionType.POINT && bothContain(s1, s2, intersection.point.get()) ->
            SegmentsIntersection(intersection.point.get())
        intersection.type == LinesIntersectionType.LINE && !disjoint(s1, s2) -> {
            val sharedEndPoint = sharedEndPoint(s1, s2)
            when {
                sharedEndPoint != null -> SegmentsIntersection(sharedEndPoint)
                /*
                 * Overlapping.
                 */
                else -> SegmentsIntersection.segment()
            }
        }
        else -> SegmentsIntersection.empty()
    }
}

private fun <P : Vector2D<P>> bothContain(s1: Segment2D<P>, s2: Segment2D<P>, point: P) =
    s1.contains(point) && s2.contains(point)

/*
 * Returns false if the segments share one or more points.
 */
private fun <P : Vector2D<P>> disjoint(s1: Segment2D<P>, s2: Segment2D<P>) =
    !(s1.contains(s2.first) || s1.contains(s2.second) || s2.contains(s1.first) || s2.contains(s1.second))

/*
 * Returns the end point shared by the two segments, or null if they share no endpoint OR
 * if they share more than one point (i.e. they overlap).
 */
private fun <P : Vector2D<P>> sharedEndPoint(s1: Segment2D<P>, s2: Segment2D<P>): P? {
    val fuzzyEquals: (P, P) -> Boolean = { first, second ->
        fuzzyEquals(first.x, second.x) && fuzzyEquals(first.y, second.y)
    }
    return when {
        fuzzyEquals(s1.first, s2.first) && !s1.contains(s2.second) -> s1.first
        fuzzyEquals(s1.first, s2.second) && !s1.contains(s2.first) -> s1.first
        fuzzyEquals(s1.second, s2.first) && !s1.contains(s2.second) -> s1.second
        fuzzyEquals(s1.second, s2.second) && !s1.contains(s1.first) -> s1.second
        else -> null
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
    val point1: Optional<P> = Optional.empty(),
    val point2: Optional<P> = Optional.empty()
) {

    constructor(point: P) : this(CircleSegmentIntersectionType.POINT, Optional.of(point))

    companion object {
        /**
         * Creates an instance of [CircleSegmentIntersection] whose type is [CircleSegmentIntersectionType.EMPTY].
         */
        fun <P : Vector2D<P>> empty() = CircleSegmentIntersection<P>(CircleSegmentIntersectionType.EMPTY)

        /**
         * Creates an appropriate instance of [CircleSegmentIntersection], taking care that, in case the resulting
         * instance has type [CircleSegmentIntersectionType.POINT], such point is stored in [point1].
         */
        fun <P : Vector2D<P>> create(point1: Optional<P>, point2: Optional<P>): CircleSegmentIntersection<P> =
            when {
                point1.isPresent && point2.isPresent ->
                    CircleSegmentIntersection(CircleSegmentIntersectionType.PAIR, point1, point2)
                point1.isPresent || point2.isPresent -> {
                    val present = point1.takeIf { point1.isPresent } ?: point2
                    val absent = point2.takeIf { point2.isEmpty } ?: point1
                    CircleSegmentIntersection(CircleSegmentIntersectionType.POINT, present, absent)
                }
                else -> empty()
            }
    }
}

/**
 * Finds the intersection between a segment and a circle.
 */
fun <P : Vector2D<P>> intersection(
    segment: Segment2D<P>,
    center: Vector2D<P>,
    radius: Double
): CircleSegmentIntersection<P> {
    val vector = segment.toVector()
    /*
     * a, b and c are the terms of the 2nd grade equation of the intersection
     */
    val a = vector.x.pow(2) + vector.y.pow(2)
    val b = 2 * (vector.x * (segment.first.x - center.x) + vector.y * (segment.first.y - center.y))
    val c = (segment.first.x - center.x).pow(2) + (segment.first.y - center.y).pow(2) - radius.pow(2)
    val det = b.pow(2) - 4 * a * c
    return when {
        fuzzyEquals(a, 0.0) || a < 0.0 || det < 0.0 -> CircleSegmentIntersection.empty()
        fuzzyEquals(det, 0.0) -> {
            val t = -b / (2 * a)
            val p = intersectionPoint(segment, vector, t)
            when {
                p.isPresent -> CircleSegmentIntersection(p.get())
                else -> CircleSegmentIntersection.empty()
            }
        }
        else -> {
            val t1 = (-b + sqrt(det)) / (2 * a)
            val t2 = (-b - sqrt(det)) / (2 * a)
            val p1 = intersectionPoint(segment, vector, t1)
            val p2 = intersectionPoint(segment, vector, t2)
            CircleSegmentIntersection.create(p1, p2)
        }
    }
}

private fun <P : Vector2D<P>> intersectionPoint(segment: Segment2D<P>, vector: Vector2D<P>, t: Double): Optional<P> =
    Optional.empty<P>().takeUnless { t.fuzzyLiesBetween(0.0, 1.0) } ?: run {
        val x = segment.first.x + t * vector.x
        val y = segment.first.y + t * vector.y
        Optional.of(segment.first.newFrom(x, y))
    }
