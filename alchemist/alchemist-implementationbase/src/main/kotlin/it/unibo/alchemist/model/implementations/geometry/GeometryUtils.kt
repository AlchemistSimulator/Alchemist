package it.unibo.alchemist.model.implementations.geometry

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DEdge
import org.danilopianini.lang.MathUtils.fuzzyEquals
import java.awt.Shape
import java.awt.geom.PathIterator
import java.awt.geom.Point2D
import java.lang.IllegalStateException
import java.util.*
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Computes the angle with atan2(y, x)
 *
 * @return atan2(y, x) (in radians)
 */
fun Euclidean2DPosition.asAngle() = atan2(y, x)

/**
 * Obtains the vertices of a polygonal shape.
 * Any curved segment connecting two points will be considered as
 * a straight line between them.
 */
fun Shape.vertices(): List<Euclidean2DPosition> {
    val vertices = mutableListOf<Euclidean2DPosition>()
    val coords = DoubleArray(6)
    val iterator = getPathIterator(null)
    while (!iterator.isDone) {
        when (iterator.currentSegment(coords)) {
            PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO -> vertices.add(Euclidean2DPosition(coords[0], coords[1]))
        }
        iterator.next()
    }
    return vertices
}

/**
 * Translates the point with the given vector.
 */
fun Euclidean2DPosition.translate(v: Euclidean2DPosition) = Euclidean2DPosition(x + v.x, y + v.y)

/**
 * Normalizes the vector.
 */
fun Euclidean2DPosition.normalize(): Euclidean2DPosition = times(1.0 / sqrt(x * x + y * y))

/**
 * Resizes the vector in order for it to have a length equal
 * to the specified parameter. Its direction and verse are preserved.
 */
fun Euclidean2DPosition.resize(newLen: Double): Euclidean2DPosition = normalize().times(newLen)

/**
 * Find the normal of a vector.
 */
fun Euclidean2DPosition.normal(): Euclidean2DPosition = Euclidean2DPosition(-y, x)

/**
 * Checks whether the given point is inside a rectangular region starting in
 * lowerBound and ending in upperBound (bounds are included).
 */
fun isInBoundaries(p: Euclidean2DPosition, lowerBound: Point2D, upperBound: Point2D) =
    p.x >= lowerBound.x && p.y >= lowerBound.y && p.x <= upperBound.x && p.y <= upperBound.y

/**
 * Checks whether the given edge is inside a rectangular region starting in
 * lowerBound and ending in upperBound (bounds are included).
 */
fun isInBoundaries(e: Euclidean2DEdge, lowerBound: Point2D, upperBound: Point2D) =
    isInBoundaries(e.first, lowerBound, upperBound) && isInBoundaries(e.second, lowerBound, upperBound)

/**
 * Computes the medium point of a segment.
 */
fun Euclidean2DEdge.midPoint() = Euclidean2DPosition((first.x + second.x) / 2, (first.y + second.y) / 2)

/**
 * Checks whether the segment (represented by a pair of positions)
 * contains the given point.
 */
fun Euclidean2DEdge.contains(p: Euclidean2DPosition) =
    areCollinear(first, second, p) && p.x.liesBetween(first.x, second.x) && p.y.liesBetween(first.y, second.y)

/**
 * Determines if three points are collinear (i.e. they lie on the same line).
 */
fun areCollinear(p1: Euclidean2DPosition, p2: Euclidean2DPosition, p3: Euclidean2DPosition): Boolean {
    return if (fuzzyEquals(p1.x, p2.x)) {
        fuzzyEquals(p1.x, p3.x)
    } else {
        val m = Pair(p1, p2).computeSlope()
        val q = p1.y - m * p1.x
        fuzzyEquals((m * p3.x + q), p3.y)
    }
}

/**
 * Computes the slope of the line passing through a couple of points.
 * If the points coincide NaN is the result.
 */
fun Euclidean2DEdge.computeSlope() = (second.y - first.y) / (second.x - first.x)

/**
 * Checks if a value lies between two values (included) provided in any order.
 */
fun Double.liesBetween(v1: Double, v2: Double) = this >= min(v1, v2) && this <= max(v1, v2)

/**
 * Finds the intersection point of two given segments (if present). This method is
 * able to deal with degenerate edges (of length zero) and collinear segments.
 * See [IntersectionResultTypes] to know how interpret the results. In case of
 * collinear, overlapping segments that share a single endpoint, INTERSECTING
 * is returned.
 */
fun intersection(s1: Euclidean2DEdge, s2: Euclidean2DEdge): IntersectionResult {
    if (s1.isDegenerate() && s2.isDegenerate()) {
        return if (s1.first == s2.first) {
            IntersectionResult(IntersectionResultTypes.INTERSECTING, Optional.of(s1.first))
        } else {
            IntersectionResult(IntersectionResultTypes.NOT_INTERSECTING)
        }
    }
    if (s1.isDegenerate() || s2.isDegenerate()) {
        val degenerate = if (s1.isDegenerate()) s1 else s2
        val other = if (degenerate == s1) s2 else s1
        return if (other.contains(degenerate.first)) {
            IntersectionResult(IntersectionResultTypes.INTERSECTING, Optional.of(degenerate.first))
        } else {
            IntersectionResult(IntersectionResultTypes.NOT_INTERSECTING)
        }
    }
    val p = s1.first
    val r = s1.second - p
    val q = s2.first
    val s = s2.second - q
    val denom = zCross(r, s)
    val num = zCross((q - p), r)
    if (fuzzyEquals(num, 0.0) && fuzzyEquals(denom, 0.0)) { // segments are collinear
        val t0 = (q - p).dot(r) / r.dot(r)
        val t1 = t0 + s.dot(r) / r.dot(r)
        if (Pair(t0, t1).intersects(0.0, 1.0)) { // segments are overlapping
            // we found out that segments are collinear and overlapping, but they may only share an endpoint,
            // in which case they are normally intersecting
            if ((fuzzyEquals(t0, 0.0) || fuzzyEquals(t0, 1.0)) && !t1.liesBetween(0.0, 1.0)) {
                return IntersectionResult(IntersectionResultTypes.INTERSECTING, Optional.of(s2.first))
            }
            if ((fuzzyEquals(t1, 0.0) || fuzzyEquals(t1, 1.0)) && !t0.liesBetween(0.0, 1.0)) {
                return IntersectionResult(IntersectionResultTypes.INTERSECTING, Optional.of(s2.second))
            }
            return IntersectionResult(IntersectionResultTypes.COLLINEAR_OVERLAPPING)
        } else {
            return IntersectionResult(IntersectionResultTypes.COLLINEAR_DISJOINT)
        }
    }
    if (fuzzyEquals(denom, 0.0) && !fuzzyEquals(num, 0.0)) {  // parallel
        return IntersectionResult(IntersectionResultTypes.NOT_INTERSECTING)
    }
    val t = zCross((q - p), s) / denom
    val u = zCross((q - p), r) / denom
    if (t.liesBetween(0.0, 1.0) && u.liesBetween(0.0, 1.0)) {
        return IntersectionResult(IntersectionResultTypes.INTERSECTING, Optional.of(p + r.times(t)))
    }
    return IntersectionResult(IntersectionResultTypes.NOT_INTERSECTING) // not parallel but not intersecting
}

/**
 * Computes the z component of the cross product of the given vectors.
 */
fun zCross(v1: Euclidean2DPosition, v2: Euclidean2DPosition) = v1.x * v2.y - v1.y * v2.x

/**
 * Dot product between bidimensional vectors.
 */
fun Euclidean2DPosition.dot(v: Euclidean2DPosition) = x * v.x + y * v.y

/**
 * Multiplies each coordinate of a vector for a scalar number n.
 */
fun Euclidean2DPosition.times(n: Double) = Euclidean2DPosition(x * n, y * n)

/**
 * An edge is degenerate if its points coincide (and its length is zero).
 */
fun Euclidean2DEdge.isDegenerate(): Boolean = first == second

/**
 * Checks whether two intervals (inclusive) intersects.
 */
fun Pair<Double, Double>.intersects(start: Double, end: Double) =
    first.liesBetween(start, end) || second.liesBetween(start, end) ||
        start.liesBetween(first, second) || end.liesBetween(first, second)

/**
 */
enum class IntersectionResultTypes {
    /**
     * Segments intersects, i.e. they share a single common point.
     */
    INTERSECTING,
    /**
     * Segments are collinear and overlapping. Note that two segments
     * may be collinear, overlapping and share a single point as well
     * (e.g. an endpoint). See [intersection] to know how such case
     * is handled.
     */
    COLLINEAR_OVERLAPPING,
    /**
     * Segments are collinear but disjoint, thus they don't intersect.
     */
    COLLINEAR_DISJOINT,
    /**
     * Segments do not intersect.
     */
    NOT_INTERSECTING;
}

/**
 */
data class IntersectionResult(
    /**
     */
    val type: IntersectionResultTypes,
    /**
     */
    val intersection: Optional<Euclidean2DPosition> = Optional.empty())
