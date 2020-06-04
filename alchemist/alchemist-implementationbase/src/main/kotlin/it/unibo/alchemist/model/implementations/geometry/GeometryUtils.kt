package it.unibo.alchemist.model.implementations.geometry

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.Vector2D
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Segment2D
import org.danilopianini.lang.MathUtils.fuzzyEquals

/**
 * Checks whether the given [segment] is inside a rectangular region described by an [origin]
 * point and [width] and [height] values (only positive).
 */
fun isInBoundaries(segment: Segment2D<*>, origin: Vector2D<*>, width: Double, height: Double) =
    isInBoundaries(segment.first, origin, width, height) && isInBoundaries(segment.second, origin, width, height)

/**
 * Checks whether the given point is inside a rectangular region described by an origin
 * point and width and height values (only positive).
 */
fun isInBoundaries(point: Vector2D<*>, origin: Vector2D<*>, width: Double, height: Double): Boolean =
    point.x >= origin.x && point.y >= origin.y && point.x <= origin.x + width && point.y <= origin.y + height

/**
 * Determines if three points are collinear (i.e. they lie on the same line).
 */
fun <P : Vector2D<P>> areCollinear(p1: P, p2: P, p3: P): Boolean =
    when {
        fuzzyEquals(p1.x, p2.x) -> fuzzyEquals(p1.x, p3.x)
        else -> {
            val m = Segment2D(p1, p2).slope
            val q = p1.y - m * p1.x
            fuzzyEquals((m * p3.x + q), p3.y)
        }
    }

/**
 * Creates a [Segment2D]. [x2] defaults to [x1] and [y2] defaults to [y1].
 */
fun createSegment(x1: Double, y1: Double, x2: Double = x1, y2: Double = y1) =
    Segment2D(Euclidean2DPosition(x1, y1), Euclidean2DPosition(x2, y2))
