package it.unibo.alchemist.model.interfaces.geometry.navigationmeshes.deaccon

import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.MutableConvexPolygon
import java.awt.Shape

/**
 * A convex polygon capable of extending itself.
 *
 * This interface was explicitly designed for the DEACCON algorithm.
 */
interface ExtendableConvexPolygon : MutableConvexPolygon {

    /**
     * Advances the specified edge in its normal direction. The step
     * parameter represents the length of the vector that will be used
     * to translate the edge. Returns a boolean indicating whether the
     * advancement was performed or not (e.g. because it would have
     * caused the lost of the convexity).
     */
    fun advanceEdge(index: Int, step: Double): Boolean

    /**
     * Tries to extend the polygon in each direction. Each edge is given
     * a chance to advance in its normal direction. The step parameter
     * represents the length of the vector that will be used to translate
     * each edge.
     * Returns a boolean indicating whether the polygon extended (e.g. at
     * least one edge advanced) or not (e.g. for the presence of obstacles).
     * The boundaries of the environment need to be specified as well, in
     * order to prevent the polygon from growing beyond them. This method
     * is able to cope with non axis-aligned convex polygonal obstacles
     * as well.
     */
    fun extend(step: Double, obstacles: Collection<Shape>, envWidth: Double, envHeight: Double): Boolean
}