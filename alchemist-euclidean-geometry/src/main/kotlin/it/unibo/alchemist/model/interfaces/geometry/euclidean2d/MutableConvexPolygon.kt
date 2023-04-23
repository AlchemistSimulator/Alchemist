package it.unibo.alchemist.model.interfaces.geometry.euclidean2d

import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * A mutable [ConvexPolygon].
 */
interface MutableConvexPolygon : ConvexPolygon {

    /**
     * Adds a vertex to the polygon.
     * @param index the index in the list of [vertices] where to put the new vertex
     * @param x x coordinate
     * @param y y coordinate
     * @returns true if the operation was performed successfully, false otherwise
     * (e.g. because it would have caused the loss of convexity)
     */
    fun addVertex(index: Int, x: Double, y: Double): Boolean

    /**
     * Removes a vertex from the polygon.
     * @param index the index of the vertex to be removed in the list of [vertices]
     * @returns true if the operation was performed successfully, false otherwise
     * (e.g. because it would have caused the loss of convexity)
     */
    fun removeVertex(index: Int): Boolean

    /**
     * Moves a vertex of the polygon to a new absolute position.
     * @param index the index of the vertex to move
     * @param newX new absolute x coordinate
     * @param newY new absolute y coordinate
     * @returns true if the operation was performed successfully, false otherwise
     * (e.g. because it would have caused the loss of convexity)
     */
    fun moveVertex(index: Int, newX: Double, newY: Double): Boolean

    /**
     * Replaces an edge of the polygon.
     * @param index the index of the edge to replace (edge i connects vertices i and i+1)
     * @param newEdge the new edge
     * @returns true if the operation was performed successfully, false otherwise
     * (e.g. because it would have caused the loss of convexity)
     */
    fun replaceEdge(index: Int, newEdge: Segment2D<Euclidean2DPosition>): Boolean
}
