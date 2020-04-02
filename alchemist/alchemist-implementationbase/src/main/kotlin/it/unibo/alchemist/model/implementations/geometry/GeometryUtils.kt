package it.unibo.alchemist.model.implementations.geometry

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.Vector2D
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Segment2D
import org.danilopianini.lang.MathUtils.fuzzyEquals
import java.awt.Shape
import java.awt.geom.PathIterator

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
fun isInBoundaries(edge: Segment2D<*>, origin: Vector2D<*>, width: Double, height: Double) =
    isInBoundaries(edge.first, origin, width, height) && isInBoundaries(edge.second, origin, width, height)

/**
 * Checks whether the given point is inside a rectangular region described by an origin
 * point and width and height values (only positive).
 */
fun isInBoundaries(p: Vector2D<*>, origin: Vector2D<*>, width: Double, height: Double): Boolean =
    p.x >= origin.x && p.y >= origin.y && p.x <= origin.x + width && p.y <= origin.y + height

/**
 * Determines if three points are collinear (i.e. they lie on the same line).
 */
fun <P : Vector2D<P>> areCollinear(p1: P, p2: P, p3: P): Boolean {
    return if (fuzzyEquals(p1.x, p2.x)) {
        fuzzyEquals(p1.x, p3.x)
    } else {
        val m = Segment2D(p1, p2).slope
        val q = p1.y - m * p1.x
        fuzzyEquals((m * p3.x + q), p3.y)
    }
}

/**
 * Creates an [Euclidean2DSegment]. [x2] defaults to [x1] and [y2] defaults to [y1].
 */
fun createSegment(x1: Double, y1: Double, x2: Double = x1, y2: Double = y1) =
    Segment2D(Euclidean2DPosition(x1, y1), Euclidean2DPosition(x2, y2))
