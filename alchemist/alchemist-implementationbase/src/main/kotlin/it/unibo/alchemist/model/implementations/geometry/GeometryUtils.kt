@file:Suppress("TooManyFunctions")
package it.unibo.alchemist.model.implementations.geometry

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DSegment
import org.danilopianini.lang.MathUtils.fuzzyEquals
import java.awt.Shape
import java.awt.geom.PathIterator
import kotlin.math.atan2

/**
 * Computes the angle with atan2(y, x).
 *
 * @return atan2(y, x) (in radians)
 */
fun Euclidean2DPosition.asAngle() = atan2(y, x)

/**
 * When using java.awt.geom.PathIterator to iterate over the boundary of a
 * Shape, you need to pass an array of this size.
 */
const val ARRAY_SIZE_FOR_PATH_ITERATOR = 6

/**
 * Obtains the vertices of a polygonal shape. Any curved segment connecting
 * two points will be considered as a straight line between them.
 */
fun Shape.vertices(): List<Euclidean2DPosition> {
    val vertices = mutableListOf<Euclidean2DPosition>()
    val coords = DoubleArray(ARRAY_SIZE_FOR_PATH_ITERATOR)
    val iterator = getPathIterator(null)
    while (!iterator.isDone) {
        when (iterator.currentSegment(coords)) {
            PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO -> {
                vertices.add(Euclidean2DPosition(coords[0], coords[1]))
            }
        }
        iterator.next()
    }
    return vertices
}

/**
 * Checks whether the given edge is inside a rectangular region described by an origin
 * point and width and height values (only positive).
 */
fun isInBoundaries(edge: Euclidean2DSegment, origin: Euclidean2DPosition, width: Double, height: Double) =
    isInBoundaries(edge.first, origin, width, height) && isInBoundaries(edge.second, origin, width, height)

/**
 * Checks whether the given point is inside a rectangular region described by an origin
 * point and width and height values (only positive).
 */
fun isInBoundaries(p: Euclidean2DPosition, origin: Euclidean2DPosition, width: Double, height: Double) =
    p.x >= origin.x && p.y >= origin.y && p.x <= origin.x + width && p.y <= origin.y + height

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
 * Finds the other of the segment which is closest to the provided position.
 */
fun Euclidean2DSegment.closestPointTo(other: Euclidean2DPosition): Euclidean2DPosition {
    return when {
        isDegenerate() -> first
        contains(other) -> other
        else -> {
            val m1 = slope()
            val intersection = when {
                m1.isInfinite() -> Euclidean2DPosition(first.x, other.y)
                fuzzyEquals(m1, 0.0) -> Euclidean2DPosition(other.x, first.y)
                else -> {
                    val q1 = first.y - m1 * first.x
                    val m2 = -1 / m1
                    val q2 = other.y - m2 * other.x
                    val x = (q2 - q1) / (m1 - m2)
                    val y = m1 * x + q1
                    Euclidean2DPosition(x, y)
                }
            }
            when {
                contains(intersection) -> intersection
                (first - other).magnitude() < (second - other).magnitude() -> first
                else -> second
            }
        }
    }
}

/**
 * Computes the distance between the current segment and a given point.
 */
fun Euclidean2DSegment.distanceTo(point: Euclidean2DPosition) = closestPointTo(point).getDistanceTo(point)

/**
 * Computes the (minimum) distance between two segments.
 */
fun Euclidean2DSegment.distanceTo(other: Euclidean2DSegment): Double =
    mutableListOf(
        distanceTo(other.first),
        distanceTo(other.second),
        other.distanceTo(first),
        other.distanceTo(second)
    ).min() ?: Double.POSITIVE_INFINITY

/**
 * Creates an [Euclidean2DSegment]. x2 defaults to x1 and y2 defaults to y1.
 */
fun createSegment(x1: Double, y1: Double, x2: Double = x1, y2: Double = y1) =
    Pair(Euclidean2DPosition(x1, y1), Euclidean2DPosition(x2, y2))
