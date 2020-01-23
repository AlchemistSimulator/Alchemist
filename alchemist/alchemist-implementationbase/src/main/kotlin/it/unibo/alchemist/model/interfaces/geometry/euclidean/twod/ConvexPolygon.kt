package it.unibo.alchemist.model.interfaces.geometry.euclidean.twod

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.AwtShapeCompatible
import java.awt.Shape

/**
 * A convex polygon is a simple polygon (i.e. not self-intersecting and
 * with no holes) in which no line segment between two points on the boundary
 * ever goes outside the polygon.
 */
interface ConvexPolygon : Euclidean2DShape, AwtShapeCompatible {

    /**
     * A list is used because vertices do have an order.
     */
    fun vertices(): List<Euclidean2DPosition>

    /**
     * Checks whether the given shape intersects with the polygon.
     * This method is "exact" (no bounding box are used).
     */
    fun intersects(other: Shape): Boolean

    /**
     * Checks whether the given vector is contained in the polygon or
     * lies on its boundary.
     */
    fun containsOrLiesOnBoundary(vector: Euclidean2DPosition): Boolean
}
