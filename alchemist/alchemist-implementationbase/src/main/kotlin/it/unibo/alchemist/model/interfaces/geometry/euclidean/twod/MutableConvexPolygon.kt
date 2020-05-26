package it.unibo.alchemist.model.interfaces.geometry.euclidean.twod

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition

/**
 * A mutable [ConvexPolygon].
 */
interface MutableConvexPolygon : ConvexPolygon {

    /**
     * Adds a vertex in the specified position.
     *
     * If the operation would cause the violation of the convexity, false
     * is returned and the operation is not performed.
     */
    fun addVertex(index: Int, x: Double, y: Double): Boolean

    /**
     * Removes the specified vertex.
     *
     * If the operation would cause the violation of the convexity, false
     * is returned and the operation is not performed.
     */
    fun removeVertex(index: Int): Boolean

    /**
     * Moves the specified vertex to the new absolute coordinates provided.
     *
     * If the operation would cause the violation of the convexity, false
     * is returned and the operation is not performed.
     */
    fun moveVertex(index: Int, newX: Double, newY: Double): Boolean

    /**
     * Moves the specified edge to the new absolute coordinates provided.
     *
     * If the operation would cause the violation of the convexity, false
     * is returned and the operation is not performed.
     */
    fun moveEdge(index: Int, newEdge: Segment2D<Euclidean2DPosition>): Boolean

    /**
     * Performs a union in-place with a collection of overlapping
     * polygons. Returns a boolean indicating whether the union was
     * successful or not (e.g. because the polygon would have lost its
     * convexity or the provided polygons did not overlap).
     */
    fun union(polygons: Collection<ConvexPolygon>): Boolean
}
