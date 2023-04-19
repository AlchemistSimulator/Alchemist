package it.unibo.alchemist.model.interfaces.geometry.euclidean2d

import it.unibo.alchemist.model.implementations.geometry.AwtShapeCompatible
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import java.awt.Shape

/**
 * A simple polygon (i.e. not self-intersecting and without holes) in which no
 * line segment between two points on the boundary ever goes outside the polygon.
 */
interface ConvexPolygon : Euclidean2DConvexShape, AwtShapeCompatible {

    /**
     * @returns the vertices of the polygon, sorted so that the polygon could
     * be obtained by connecting consecutive points in the list with a segment
     */
    fun vertices(): List<Euclidean2DPosition>

    /**
     * @returns the edges (= sides) of the polygon
     */
    fun edges(): List<Segment2D<Euclidean2DPosition>>

    /**
     * Depending on the implementation, this may be faster than [edges].get([index]).
     * @param index indicates which edge to get: edge i connects vertices i and i+1
     * (with respect to the ordering of vertices used in [vertices])
     * @returns the specified edge (= side) of the polygon
     */
    fun getEdge(index: Int): Segment2D<Euclidean2DPosition>

    /**
     * Checks if the polygon contains a vector (= a point). The definition of insideness
     * may vary depending on the implementation, this may affect the outcome for points
     * lying on the polygon's boundary. For accurate operations see [containsBoundaryIncluded]
     * and [containsBoundaryExcluded].
     * @param vector the vector (= point)
     * @returns true if the polygon contains the vector
     */
    override fun contains(vector: Euclidean2DPosition): Boolean

    /**
     * Checks if a vector (= a point) lies on the polygon's boundary.
     * @param vector the vector (= point)
     * @returns true if the vector lies on the polygon's boundary
     */
    fun liesOnBoundary(vector: Euclidean2DPosition): Boolean

    /**
     * Checks if a vector (= a point) is contained in the polygon or lies on its boundary.
     * @param vector the vector (= point)
     * @returns true if the vector is contained in the polygon or lies on its boundary
     */
    fun containsBoundaryIncluded(vector: Euclidean2DPosition): Boolean

    /**
     * Checks if a vector (= a point) is contained in the polygon, boundary excluded.
     * @param vector the vector (= point)
     * @returns true if the vector is contained in the interior of the polygon
     */
    fun containsBoundaryExcluded(vector: Euclidean2DPosition): Boolean

    /**
     * Checks if the polygon contains a polygonal [java.awt.Shape] (i.e. without curved
     * segments). A polygonal shape is contained in a polygon if all of its points are
     * contained in (or lie on the boundary of) the latter.
     * @param shape the polygonal shape
     * @returns true if the polygon contains the shape.
     */
    fun contains(shape: Shape): Boolean

    /**
     * Checks if a [java.awt.Shape] intersects the polygon, adjacent shapes are not
     * considered to be intersecting.
     * @param shape the shape
     * @returns true if the shape intersects with the polygon
     */
    fun intersects(shape: Shape): Boolean

    /**
     * A polygon is adjacent to another if any of its points lies on the boundary of the other.
     * @param other the other polygon
     * @returns true if the polygons are adjacent
     */
    fun isAdjacentTo(other: ConvexPolygon): Boolean

    /**
     * Checks if a segment intersects with the polygon, segments lying on the polygon's
     * boundary are not considered to be intersecting.
     * @param segment the segment
     * @returns true if the segment intersects the polygon
     */
    fun intersects(segment: Segment2D<Euclidean2DPosition>): Boolean

    /**
     * Finds the edge of the polygon closest to the provided [segment], i.e. the first one
     * that would collide (= intersect) with the segment in case the polygon extended on
     * each side.
     * @param segment the segment
     * @returns the edge of the polygon closest to the provided segment
     */
    fun closestEdgeTo(segment: Segment2D<Euclidean2DPosition>): Segment2D<Euclidean2DPosition>
}
