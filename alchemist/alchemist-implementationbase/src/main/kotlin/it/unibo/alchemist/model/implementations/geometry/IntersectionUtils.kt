package it.unibo.alchemist.model.implementations.geometry

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DSegment
import org.danilopianini.lang.MathUtils.fuzzyEquals
import java.util.Optional
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * In euclidean geometry, the intersection of two lines can be
 * an empty set, a point, or a line (in other words, infinite points).
 */
enum class LinesIntersectionType {
    /**
     */
    POINT,
    /**
     */
    LINE,
    /**
     */
    EMPTY
}

/**
 */
data class LinesIntersectionResult(
    /**
     */
    val type: LinesIntersectionType,
    /**
     */
    val point: Optional<Euclidean2DPosition> = Optional.empty()
)

/**
 * Finds the intersection of two lines represented by two segments.
 * Such segments are required not to be degenerate (of length 0).
 */
fun intersectionLines(l1: Euclidean2DSegment, l2: Euclidean2DSegment): LinesIntersectionResult {
    require(!l1.isDegenerate() && !l2.isDegenerate()) { "degenerate lines" }
    val m1 = l1.slope()
    val q1 = l1.first.y - m1 * l1.first.x
    val m2 = l2.slope()
    val q2 = l2.first.y - m2 * l2.first.x
    return when {
        areParallel(m1, m2) -> {
            when {
                coincide(m1, m2, q1, q2, l1.first.x, l2.first.x) -> LinesIntersectionResult(LinesIntersectionType.LINE)
                else -> LinesIntersectionResult(LinesIntersectionType.EMPTY)
            }
        }
        else -> {
            val intersection = when {
                m1.isInfinite() -> Euclidean2DPosition(l1.first.x, m2 * l1.first.x + q2)
                m2.isInfinite() -> Euclidean2DPosition(l2.first.x, m1 * l2.first.x + q1)
                else -> {
                    val x = (q2 - q1) / (m1 - m2)
                    val y = m1 * x + q1
                    Euclidean2DPosition(x, y)
                }
            }
            LinesIntersectionResult(LinesIntersectionType.POINT, Optional.of(intersection))
        }
    }
}

/*
 * Checks whether two PARALLEL lines coincide.
 */
private fun coincide(m1: Double, m2: Double, q1: Double, q2: Double, x1: Double, x2: Double) =
    when {
        m1.isInfinite() && m2.isInfinite() -> fuzzyEquals(x1, x2)
        else -> fuzzyEquals(q1, q2)
    }

private fun areParallel(m1: Double, m2: Double) =
    (m1.isInfinite() && m2.isInfinite()) || (m1.isFinite() && m2.isFinite() && fuzzyEquals(m1, m2))

/**
 * In euclidean geometry, the intersection of two segments can be
 * an empty set, a point, or a segment (in other words, infinite points).
 */
enum class SegmentsIntersectionTypes {
    /**
     * Segments have a single point of intersection.
     */
    POINT,
    /**
     * Segments have infinite points of intersection, lying on a segment.
     * In other words, they are collinear and overlapping.
     * Note that two segments may be collinear, overlapping and share a
     * single point (e.g. an endpoint). In this case the intersection
     * type is [POINT].
     */
    SEGMENT,
    /**
     * Segments do not intersect.
     */
    EMPTY;
}

/**
 */
data class SegmentsIntersectionResult(
    /**
     */
    val type: SegmentsIntersectionTypes,
    /**
     */
    val intersection: Optional<Euclidean2DPosition> = Optional.empty()
)

/**
 * Finds the intersection point of two given segments (if exists). This method is
 * able to deal with degenerate edges (of length zero) and collinear segments.
 */
fun intersection(s1: Euclidean2DSegment, s2: Euclidean2DSegment): SegmentsIntersectionResult {
    if (s1.isDegenerate() || s2.isDegenerate()) {
        return intersectionDegenerate(s1, s2)
    }
    val p = s1.first
    val r = s1.toVector()
    val q = s2.first
    val s = s2.toVector()
    val denom = zCross(r, s)
    val num = zCross((q - p), r)
    return when {
        /*
         * Segments are parallel.
         */
        fuzzyEquals(denom, 0.0) -> {
            when {
                !fuzzyEquals(num, 0.0) -> SegmentsIntersectionResult(SegmentsIntersectionTypes.EMPTY)
                /*
                 * Collinear.
                 */
                else -> {
                    val t0 = (q - p).dot(r) / r.dot(r)
                    val t1 = t0 + s.dot(r) / r.dot(r)
                    intersectionCollinear(s2, t0, t1)
                }
            }
        }
        else -> {
            val t = zCross((q - p), s) / denom
            val u = zCross((q - p), r) / denom
            when {
                t.liesBetween(0.0, 1.0) && u.liesBetween(0.0, 1.0) ->
                    SegmentsIntersectionResult(SegmentsIntersectionTypes.POINT, Optional.of(p + r.times(t)))
                /*
                 * Not parallel but not intersecting.
                 */
                else -> SegmentsIntersectionResult(SegmentsIntersectionTypes.EMPTY)
            }
        }
    }
}

private fun intersectionDegenerate(s1: Euclidean2DSegment, s2: Euclidean2DSegment): SegmentsIntersectionResult {
    val degenerate = s1.takeIf { it.isDegenerate() } ?: s2
    val other = s2.takeIf { degenerate == s1 } ?: s1
    if (other.contains(degenerate.first)) {
        return SegmentsIntersectionResult(SegmentsIntersectionTypes.POINT, Optional.of(degenerate.first))
    }
    return SegmentsIntersectionResult(SegmentsIntersectionTypes.EMPTY)
}

private fun intersectionCollinear(s2: Euclidean2DSegment, t0: Double, t1: Double): SegmentsIntersectionResult {
    return when {
        /*
         * Segments are overlapping.
         */
        DoubleInterval(t0, t1).intersects(DoubleInterval(0.0, 1.0)) -> {
            /*
             * Segments may only share an endpoint.
             */
            when {
                (fuzzyEquals(t0, 0.0) || fuzzyEquals(t0, 1.0)) && !t1.liesBetween(0.0, 1.0) ->
                    SegmentsIntersectionResult(SegmentsIntersectionTypes.POINT, Optional.of(s2.first))
                (fuzzyEquals(t1, 0.0) || fuzzyEquals(t1, 1.0)) && !t0.liesBetween(0.0, 1.0) ->
                    SegmentsIntersectionResult(SegmentsIntersectionTypes.POINT, Optional.of(s2.second))
                else -> SegmentsIntersectionResult(SegmentsIntersectionTypes.SEGMENT)
            }
        }
        /*
         * Collinear but disjoint.
         */
        else -> SegmentsIntersectionResult(SegmentsIntersectionTypes.EMPTY)
    }
}

/**
 */
enum class CircleSegmentIntersectionType {
    /**
     */
    POINT,
    /**
     */
    PAIR,
    /**
     */
    EMPTY;
}

/**
 */
data class CircleSegmentIntersectionResult(
    /**
     */
    val type: CircleSegmentIntersectionType,
    /**
     */
    val point1: Optional<Euclidean2DPosition> = Optional.empty(),
    /**
     */
    val point2: Optional<Euclidean2DPosition> = Optional.empty()
)

/**
 * Finds the intersection between a circle and a segment.
 */
fun intersection(
    segment: Euclidean2DSegment,
    center: Euclidean2DPosition,
    radius: Double
): CircleSegmentIntersectionResult {
    val vector = segment.toVector()
    /*
     * a, b and c are the terms of the 2nd grade equation of the intersection
     */
    val a = vector.x.pow(2) + vector.y.pow(2)
    val b = 2 * (vector.x * (segment.first.x - center.x) + vector.y * (segment.first.y - center.y))
    val c = (segment.first.x - center.x).pow(2) + (segment.first.y - center.y).pow(2) - radius.pow(2)
    val det = b.pow(2) - 4 * a * c
    return when {
        fuzzyEquals(a, 0.0) || a < 0.0 || det < 0.0 ->
            CircleSegmentIntersectionResult(CircleSegmentIntersectionType.EMPTY)
        fuzzyEquals(det, 0.0) -> {
            val t = -b / (2 * a)
            when {
                t.liesBetween(0.0, 1.0) -> CircleSegmentIntersectionResult(
                    CircleSegmentIntersectionType.POINT,
                    Optional.of(
                        Euclidean2DPosition(segment.first.x + t * vector.x, segment.first.y + t * vector.y)
                    )
                )
                else -> CircleSegmentIntersectionResult(CircleSegmentIntersectionType.EMPTY)
            }
        }
        else -> {
            val t1 = (-b + sqrt(det)) / (2 * a)
            val t2 = (-b - sqrt(det)) / (2 * a)
            val p1 = if (t1.liesBetween(0.0, 1.0)) {
                Optional.of(
                    Euclidean2DPosition(segment.first.x + t1 * vector.x, segment.first.y + t1 * vector.y)
                )
            } else {
                Optional.empty()
            }
            val p2 = if (t2.liesBetween(0.0, 1.0)) {
                Optional.of(
                    Euclidean2DPosition(segment.first.x + t2 * vector.x, segment.first.y + t2 * vector.y)
                )
            } else {
                Optional.empty()
            }
            when (mutableListOf(t1, t2).filter { it.liesBetween(0.0, 1.0) }.count()) {
                0 -> CircleSegmentIntersectionResult(CircleSegmentIntersectionType.EMPTY)
                1 -> {
                    if (p2.isEmpty) {
                        CircleSegmentIntersectionResult(CircleSegmentIntersectionType.POINT, p1, p2)
                    } else {
                        CircleSegmentIntersectionResult(CircleSegmentIntersectionType.POINT, p2, p1)
                    }
                }
                else -> CircleSegmentIntersectionResult(CircleSegmentIntersectionType.PAIR, p1, p2)
            }
        }
    }
}
