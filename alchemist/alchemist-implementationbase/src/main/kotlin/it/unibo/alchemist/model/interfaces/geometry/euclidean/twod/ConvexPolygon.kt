package it.unibo.alchemist.model.interfaces.geometry.euclidean.twod

import it.unibo.alchemist.model.implementations.geometry.SegmentsIntersectionType
import it.unibo.alchemist.model.implementations.geometry.intersection
import it.unibo.alchemist.model.implementations.geometry.vertices
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.geometry.AwtShapeCompatible
import java.awt.Shape
import java.lang.IllegalStateException
import kotlin.math.min

/**
 * A convex polygon is a simple polygon (i.e. not self-intersecting and
 * without holes) in which no line segment between two points on the boundary
 * ever goes outside the polygon.
 */
interface ConvexPolygon : Euclidean2DConvexShape, AwtShapeCompatible {

    /**
     * A list is used because vertices do have an order.
     */
    fun vertices(): List<Euclidean2DPosition>

    /**
     * The index parameter specify which edge to get: edge i connects
     * vertices i and i+1.
     */
    fun getEdge(index: Int): Segment2D<Euclidean2DPosition>

    /**
     * Checks whether the given shape intersects with the polygon.
     */
    fun intersects(shape: Shape): Boolean

    /**
     * Returns a collection containing the edges (or sides) of the polygon.
     */
    fun edges() = vertices().indices.map { getEdge(it) }

    /**
     * Checks whether the polygon contains the given shape.
     */
    fun contains(shape: Shape) = shape.vertices().all { contains(it) }

    /**
     * Checks whether the given vector is contained in the polygon or lies on its boundary.
     */
    fun containsBoundaryIncluded(vector: Euclidean2DPosition) = contains(vector) || edges().any { it.contains(vector) }

    /**
     * Checks whether the given vector is contained in the polygon, boundary excluded.
     */
    fun containsBoundaryExcluded(vector: Euclidean2DPosition): Boolean =
        contains(vector) && edges().none { it.contains(vector) }

    /**
     * Checks if the provided segment intersects with the polygon, boundary excluded.
     */
    fun intersectsBoundaryExcluded(segment: Segment2D<Euclidean2DPosition>): Boolean = edges()
        .map { intersection(it, segment) }
        .filter { it.type == SegmentsIntersectionType.POINT }
        .map { it.point.get() }
        .distinct()
        .size > 1

    /**
     * Finds the edge of the polygon closest to the provided segment.
     */
    fun closestEdgeTo(segment: Segment2D<Euclidean2DPosition>): Segment2D<Euclidean2DPosition> = edges().minWith(
        compareBy({
            it.distanceTo(segment)
        }, {
            min(segment.distanceTo(it.first) + segment.distanceTo(it.second),
                it.distanceTo(segment.first) + it.distanceTo(segment.second))
        })
    ) ?: throw IllegalStateException("no edge could be found")
}
