package it.unibo.alchemist.model.interfaces.geometry.euclidean.twod

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.AwtShapeCompatible
import java.awt.Shape

/**
 * A convex polygon is a simple polygon (i.e. not self-intersecting and
 * without holes) in which no line segment between two points on the boundary
 * ever goes outside the polygon.
 */
interface ConvexPolygon : ConvexEuclidean2DShape, AwtShapeCompatible {

    /**
     * A list is used because vertices do have an order.
     */
    fun vertices(): List<Euclidean2DPosition>

    /**
     * The index parameter specify which edge to get: edge i connects
     * vertices i and i+1.
     */
    fun getEdge(index: Int): Euclidean2DSegment

    /**
     * Checks whether the given shape intersects with the polygon.
     */
    fun intersects(shape: Shape): Boolean
}
