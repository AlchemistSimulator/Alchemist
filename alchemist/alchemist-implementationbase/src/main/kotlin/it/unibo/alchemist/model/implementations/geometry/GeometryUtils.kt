package it.unibo.alchemist.model.implementations.geometry

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import org.danilopianini.lang.MathUtils.fuzzyEquals
import java.awt.Shape
import java.awt.geom.PathIterator
import java.awt.geom.Point2D
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
fun Euclidean2DPosition.translate(v: Euclidean2DPosition) = Euclidean2DPosition(this.x + v.x, this.y + v.y)

/**
 * Normalizes the vector.
 */
fun Euclidean2DPosition.normalize(): Euclidean2DPosition {
    val len = sqrt(x * x + y * y)
    return Euclidean2DPosition(x / len, y / len)
}

/**
 * Resizes the vector in order for it to have a length equal
 * to the specified parameter. Its direction and verse are preserved.
 */
fun Euclidean2DPosition.resize(newLen: Double): Euclidean2DPosition {
    val n = this.normalize()
    return Euclidean2DPosition(n.x * newLen, n.y * newLen)
}

/**
 * Checks whether the given point is inside a rectangular region starting in
 * lowerBound and ending in upperBound.
 */
fun isInBoundaries(p: Euclidean2DPosition, lowerBound: Point2D, upperBound: Point2D) =
    p.x >= lowerBound.x && p.y >= lowerBound.y && p.x <= upperBound.x && p.y <= upperBound.y

/**
 * Checks whether the given edge is inside a rectangular region starting in
 * lowerBound and ending in upperBound.
 */
fun isInBoundaries(e: Pair<Euclidean2DPosition, Euclidean2DPosition>, lowerBound: Point2D, upperBound: Point2D) =
    isInBoundaries(e.first, lowerBound, upperBound) && isInBoundaries(e.second, lowerBound, upperBound)

/**
 * Computes the medium point of a segment.
 */
fun Pair<Euclidean2DPosition, Euclidean2DPosition>.midPoint() =
    Euclidean2DPosition((first.x + second.x) / 2, (first.y + second.y) / 2)

/**
 * Checks whether the segment (represented by a pair of positions)
 * contains the given point.
 */
fun Pair<Euclidean2DPosition, Euclidean2DPosition>.contains(p: Euclidean2DPosition): Boolean {
    val isCollinear: Boolean
    isCollinear = if (fuzzyEquals(first.x, second.x)) {
        fuzzyEquals(p.x, first.x)
    } else {
        val m = computeSlope()
        val q = first.y - m * first.x
        fuzzyEquals((m * p.x + q), p.y)
    }
    return isCollinear && p.x.liesBetween(first.x, second.x) && p.y.liesBetween(first.y, second.y)
}

/**
 * Computes the slope of the line passing through a couple of points.
 * If the points coincide NaN is the result.
 */
fun Pair<Euclidean2DPosition, Euclidean2DPosition>.computeSlope() = (second.y - first.y) / (second.x - first.x)

/**
 * Checks if a value lies between two values (included) provided in any order.
 */
fun Double.liesBetween(v1: Double, v2: Double) = this >= min(v1, v2) && this <= max(v1, v2)

/**
 * Finds the intersection point of two given segments.
 */
fun intersection(s1: Pair<Euclidean2DPosition, Euclidean2DPosition>, s2: Pair<Euclidean2DPosition, Euclidean2DPosition>): Euclidean2DPosition {
    val p1 = s1.first
    val p2 = s1.second
    val p3 = s2.first
    val p4 = s2.second
    val denom = (p4.x - p3.x) * (p1.y - p2.y) - (p1.x - p2.x) * (p4.y - p3.y)
    require(denom != 0.0) { "segments are collinear" }
    val ta = ((p3.y - p4.y) * (p1.x - p3.x) + (p4.x - p3.x) * (p1.y - p3.y)) / denom
    require(ta in 0.0..1.0) { "segments do not intersect" }
    return Euclidean2DPosition(p1.x + ta * (p2.x - p1.x), p1.y + ta * (p2.y - p1.y))
}
