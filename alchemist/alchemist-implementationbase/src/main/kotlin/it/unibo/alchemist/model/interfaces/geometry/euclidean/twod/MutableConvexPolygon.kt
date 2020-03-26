package it.unibo.alchemist.model.interfaces.geometry.euclidean.twod

/**
 * A mutable convex polygon.
 */
interface MutableConvexPolygon : ConvexPolygon {

    /**
     * Adds a vertex in the specified position. See also [addVertex].
     */
    fun addVertex(index: Int, x: Double, y: Double): Boolean

    /**
     * Removes the vertex specified by the index parameter.
     *
     * If the removal of such vertex would cause the violation of the
     * convexity, false is returned and the vertex is not removed. Please
     * be also aware that a polygon cannot have less than 3 vertices.
     */
    fun removeVertex(index: Int): Boolean

    /**
     * Move the specified vertex to the new absolute coordinates specified.
     *
     * If the new position of the specified vertex would cause the
     * violation of the convexity, false is returned and the operation
     * is not performed.
     */
    fun moveVertex(index: Int, newX: Double, newY: Double): Boolean

    /**
     * If the new coords would cause the lost of the convexity,
     * false is returned.
     */
    fun moveEdge(index: Int, newEdge: Euclidean2DSegment): Boolean

    /**
     * Performs a union in-place with a collection of overlapping
     * polygons. Returns a boolean indicating whether the union was
     * successful or not (e.g. because the polygon would have lost its
     * convexity or the provided polygons did not overlap).
     */
    fun union(polygons: Collection<ConvexPolygon>): Boolean
}
