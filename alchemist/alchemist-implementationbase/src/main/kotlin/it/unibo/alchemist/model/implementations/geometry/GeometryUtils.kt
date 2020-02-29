package it.unibo.alchemist.model.implementations.geometry

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DSegment
import org.danilopianini.lang.MathUtils.fuzzyEquals
import java.awt.Shape
import java.awt.geom.PathIterator
import java.awt.geom.Point2D
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.math.pow
import kotlin.math.acos

/**
 * Computes the angle with atan2(y, x)
 *
 * @return atan2(y, x) (in radians)
 */
fun Euclidean2DPosition.asAngle() = atan2(y, x)

/**
 * Obtains the vertices of a polygonal shape. Any curved segment connecting
 * two points will be considered as a straight line between them.
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
 * Multiplies each coordinate of a vector for a scalar number n.
 */
fun Euclidean2DPosition.times(n: Double) = Euclidean2DPosition(x * n, y * n)

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
 * Computes the z component of the cross product of the given vectors.
 */
fun zCross(v1: Euclidean2DPosition, v2: Euclidean2DPosition) = v1.x * v2.y - v1.y * v2.x

/**
 * Dot product between bidimensional vectors.
 */
fun Euclidean2DPosition.dot(v: Euclidean2DPosition) = x * v.x + y * v.y

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
fun isInBoundaries(e: Euclidean2DSegment, lowerBound: Point2D, upperBound: Point2D) =
    isInBoundaries(e.first, lowerBound, upperBound) && isInBoundaries(e.second, lowerBound, upperBound)

/**
 * Mutates an edge to a vector. In particular, the vector representing the
 * movement from the first point to the second point of the edge.
 */
fun Euclidean2DSegment.toVector() = second - first

/**
 * Computes the slope of the line passing through a couple of points.
 * If the points coincide NaN is the result.
 */
fun Euclidean2DSegment.slope() = toVector().run { y / x }

/**
 * An edge is degenerate if its points coincide (and its length is zero).
 */
fun Euclidean2DSegment.isDegenerate(): Boolean = first == second

/**
 * Checks whether the segment (represented by a pair of positions)
 * contains the given point.
 */
fun Euclidean2DSegment.contains(p: Euclidean2DPosition) =
    areCollinear(first, second, p) && p.x.liesBetween(first.x, second.x) && p.y.liesBetween(first.y, second.y)

/**
 * Computes the medium point of the current segment.
 */
fun Euclidean2DSegment.midPoint() = Euclidean2DPosition((first.x + second.x) / 2, (first.y + second.y) / 2)

/**
 * Determines if three points are collinear (i.e. they lie on the same line).
 */
fun areCollinear(p1: Euclidean2DPosition, p2: Euclidean2DPosition, p3: Euclidean2DPosition): Boolean {
    return if (fuzzyEquals(p1.x, p2.x)) {
        fuzzyEquals(p1.x, p3.x)
    } else {
        val m = Pair(p1, p2).slope()
        val q = p1.y - m * p1.x
        fuzzyEquals((m * p3.x + q), p3.y)
    }
}

/**
 * Checks if a value lies between two values (included) provided in any order.
 */
fun Double.liesBetween(v1: Double, v2: Double) = this >= min(v1, v2) && this <= max(v1, v2)

/**
 * Checks whether two intervals (inclusive) intersects.
 */
fun Pair<Double, Double>.intersects(start: Double, end: Double) =
    first.liesBetween(start, end) || second.liesBetween(start, end) ||
        start.liesBetween(first, second) || end.liesBetween(first, second)

/**
 * Finds the magnitude of a vector.
 */
fun <V : Vector<V>> Vector<V>.magnitude(): Double {
    var sum = 0.0
    for (d in 0 until dimensions) {
        sum += getCoordinate(d).pow(2)
    }
    return sqrt(sum)
}

/**
 * Computes the dot product between two vectors.
 */
fun <V : Vector<V>> Vector<V>.dot(other: V): Double {
    var dot = 0.0
    for (d in 0 until dimensions) {
        dot += getCoordinate(d) * other.getCoordinate(d)
    }
    return dot
}

/**
 * Computes the angle in radians between two vectors.
 */
fun <V : Vector<V>> Vector<V>.angleBetween(other: V): Double =
    acos(dot(other) / (magnitude() * other.magnitude()))

/**
 * Checks whether the segment is aligned to the x axis.
 */
fun Euclidean2DSegment.isXAxisAligned(): Boolean = fuzzyEquals(first.y, second.y)

/**
 * Checks whether the segment is aligned to the y axis.
 */
fun Euclidean2DSegment.isYAxisAligned(): Boolean = fuzzyEquals(first.x, second.x)

/**
 * Checks whether the segment is axis-aligned.
 */
fun Euclidean2DSegment.isAxisAligned(): Boolean = isXAxisAligned() || isYAxisAligned()

/**
 * Finds the point of the segment which is closest to the provided position.
 */
fun Euclidean2DSegment.closestPointTo(p: Euclidean2DPosition): Euclidean2DPosition {
    if (isDegenerate()) {
        return first
    }
    if (contains(p)) {
        return p
    }
    val m1 = slope()
    val intersection = when {
        m1.isInfinite() -> Euclidean2DPosition(first.x, p.y)
        fuzzyEquals(m1, 0.0) -> Euclidean2DPosition(p.x, first.y)
        else -> {
            val q1 = first.y - m1 * first.x
            val m2 = -1 / m1
            val q2 = p.y - m2 * p.x
            val x = (q2 - q1) / (m1 - m2)
            val y = m1 * x + q1
            Euclidean2DPosition(x, y)
        }
    }
    return if (contains(intersection)) {
        intersection
    } else {
        if ((first - p).magnitude() < (second - p).magnitude()) {
            first
        } else {
            second
        }
    }
}

/**
 * Checks if the provided segment intersects with the polygon, boundary excluded.
 */
fun ConvexPolygon.intersectsBoundaryExcluded(s: Euclidean2DSegment): Boolean =
    vertices().indices
        .map { intersection(getEdge(it), s) }
        .filter { it.type == SegmentsIntersectionTypes.POINT }
        .map { it.intersection.get() }
        .distinct()
        .size > 1

/**
 * Finds the point on the line represented by the current segment, given
 * its x coordinate. Returns null if the point cannot be located.
 */
fun Euclidean2DSegment.findPointOnLineGivenX(x: Double): Euclidean2DPosition? {
    val m = slope()
    if (m.isInfinite()) {
        return null
    }
    val q = first.y - m * first.x
    return Euclidean2DPosition(x, m * x + q)
}

/**
 * Finds the point on the line represented by the current segment, given
 * its y coordinate. Returns null if the point cannot be located.
 */
fun Euclidean2DSegment.findPointOnLineGivenY(y: Double): Euclidean2DPosition? {
    val m = slope()
    if (m.isInfinite()) {
        return Euclidean2DPosition(first.x, y)
    }
    if (fuzzyEquals(m, 0.0)) {
        return null
    }
    val q = first.y - m * first.x
    return Euclidean2DPosition((y - q) / m, y)
}
