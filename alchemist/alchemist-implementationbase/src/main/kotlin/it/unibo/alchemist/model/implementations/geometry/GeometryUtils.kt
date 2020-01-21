package it.unibo.alchemist.model.implementations.geometry

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.PathIterator
import java.awt.geom.Point2D
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min

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
    val iterator = getPathIterator(AffineTransform())
    while (!iterator.isDone) {
        when (iterator.currentSegment(coords)) {
            PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO -> vertices.add(Euclidean2DPosition(coords[0], coords[1]))
        }
        iterator.next()
    }
    return vertices
}

/**
 * Checks whether the segment (represented by a pair of positions)
 * contains the given point.
 */
fun Pair<Euclidean2DPosition, Euclidean2DPosition>.contains(p: Euclidean2DPosition): Boolean {
    val isCollinear: Boolean
    isCollinear = if (first.x == second.x) {
        p.x == first.x
    } else {
        val m = computeSlope()
        val q = first.y - m * first.x
        (m * p.x + q) == p.y
    }
    return isCollinear && p.x.liesBetween(first.x, second.x) && p.y.liesBetween(first.y, second.y)
}


/**
 * Computes the slope of the line passing through a couple of points.
 * If the points coincide NaN is the result.
 */
fun Pair<Euclidean2DPosition, Euclidean2DPosition>.computeSlope(): Double {
    return (second.y - first.y) / (second.x - first.x)
}

/**
 * Checks if a value lies between two values (included) provided in any order.
 */
fun Double.liesBetween(v1: Double, v2: Double): Boolean {
    return this >= min(v1, v2) && this <= max(v1, v2)
}