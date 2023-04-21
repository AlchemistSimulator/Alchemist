/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.geometry.euclidean2d

import it.unibo.alchemist.model.geometry.Vector2D
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.AwtShapeExtension.vertices
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Intersection2D
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Segment2D
import java.awt.Shape
import kotlin.math.min

/**
 * An abstract [ConvexPolygon] providing a convexity test.
 */
abstract class AbstractConvexPolygon : ConvexPolygon {

    companion object {
        /**
         * @returns the sum of the distances between this segment's endpoints and [other].
         */
        private fun <V : Vector2D<V>> Segment2D<V>.cumulativeDistanceTo(other: Segment2D<V>): Double =
            other.distanceTo(first) + other.distanceTo(second)

        /**
         * @returns the minimum cumulative distance between this segment and [other] (this segment's
         * [cumulativeDistanceTo] [other] maybe different from [other]'s [cumulativeDistanceTo] this segment).
         */
        private fun <V : Vector2D<V>> Segment2D<V>.minCumulativeDistanceTo(other: Segment2D<V>): Double =
            min(cumulativeDistanceTo(other), other.cumulativeDistanceTo(this))
    }

    override fun liesOnBoundary(vector: Euclidean2DPosition): Boolean = edges().any { it.contains(vector) }

    override fun containsBoundaryIncluded(vector: Euclidean2DPosition): Boolean =
        contains(vector) || liesOnBoundary(vector)

    override fun containsBoundaryExcluded(vector: Euclidean2DPosition): Boolean =
        contains(vector) && !liesOnBoundary(vector)

    override fun contains(shape: Shape): Boolean = shape.vertices().all { containsBoundaryIncluded(it) }

    /*
     * It's important that intersects(Shape) does not consider adjacent shapes as intersecting.
     */
    override fun isAdjacentTo(other: ConvexPolygon): Boolean = !intersects(other.asAwtShape()) &&
        (other.vertices().any { liesOnBoundary(it) } || vertices().any { other.liesOnBoundary(it) })

    override fun closestEdgeTo(segment: Segment2D<Euclidean2DPosition>): Segment2D<Euclidean2DPosition> =
        requireNotNull(
            edges().minWithOrNull(compareBy({ it.distanceTo(segment) }, { it.minCumulativeDistanceTo(segment) })),
        ) { "no edge found" }

    override fun intersects(segment: Segment2D<Euclidean2DPosition>): Boolean {
        if (containsBoundaryExcluded(segment.first) || containsBoundaryExcluded(segment.second)) {
            return true
        }
        val intersections = edges()
            .map { it.intersect(segment) } // Either InfinitePoints, SinglePoint, or None
            .filterNot { it is Intersection2D.None }
            .asSequence()
        // Lazily evaluated
        val intersectionPoints = intersections
            .filterIsInstance<Intersection2D.SinglePoint<Euclidean2DPosition>>()
            .map { it.point }
            .distinct()
        return intersections.none { it is Intersection2D.InfinitePoints } && intersectionPoints.count() > 1
    }
    override fun toString(): String = javaClass.simpleName + vertices()

    /**
     * Finds the previous index with the respect to the given [index], restarting from the end if necessary.
     */
    protected fun circularPrevious(index: Int): Int = vertices().size.let { (index - 1 + it) % it }

    /**
     * Finds the next index with respect to the given [index], restarting from the beginning if necessary.
     */
    protected fun circularNext(index: Int): Int = (index + 1) % vertices().size

    /**
     * Checks if the polygon is convex (see [ConvexPolygon]).
     * In order to be convex, a polygon must first be simple (not self-intersecting).
     * Ascertained that the polygon is simple, a rather easy convexity test consists
     * in checking that every edge turns in the same direction (either left or right)
     * with respect to the previous one. If they all turn in the same direction, then
     * the polygon is convex. That is the definition of convexity of a polygon's boundary
     * in this context.
     */
    protected fun isConvex() = !isSelfIntersecting() && isBoundaryConvex()

    /**
     * Checks if the polygon is convex, assuming that every edge apart from the specified
     * ones does not cause self-intersection.
     */
    protected fun isConvex(vararg modifiedEdges: Int) =
        isBoundaryConvex() && modifiedEdges.none { causeSelfIntersection(it) }

    /**
     * Checks if the polygon's boundary is convex. See [isConvex].
     */
    private fun isBoundaryConvex(): Boolean {
        if (edges().count { !it.isDegenerate } < 3) {
            return false
        }
        var e1 = getEdge(vertices().size - 1)
        var sense: Boolean? = null
        return edges().none { e2 ->
            val z = Vector2D.zCross(e1.toVector, e2.toVector)
            var lostConvexity = false
            /*
             * Cross product is 0 in the following cases:
             * - one (or both) of the two edges is degenerate, so it's perfectly
             * fine to skip it as it doesn't affect convexity.
             * - the two edges are linearly dependent, i.e. either they have
             * the same direction or opposite ones. In the former case it's
             * fine to ignore the edge since it can't violate convexity,
             * whereas the latter case means edges are overlapping (since they
             * have opposite directions and are consecutive), which will be
             * detected by a self-intersection test.
             */
            if (z != 0.0) {
                if (sense == null) {
                    sense = z > 0.0
                } else if (sense != z > 0.0) {
                    lostConvexity = true
                }
                e1 = e2
            }
            lostConvexity
        }
    }

    /**
     * Checks whether the polygon is self-intersecting. In this context,
     * a polygon is considered non self-intersecting if the following holds
     * for every edge e:
     * - e must share ONLY its endpoints with its neighboring edges,
     * no other point shall be in common with those edges.
     * - e should not have any point in common with any other edge.
     * Degenerate edges are not considered as they cannot cause self-intersection.
     *
     * This method has a time complexity of O(n^2). Consider using a hash
     * data structure with spatial-related buckets in the future.
     */
    private fun isSelfIntersecting() = vertices().indices.any { causeSelfIntersection(it) }

    /**
     * Checks whether an edge of the polygon cause the latter to be self-intersecting.
     * See [isSelfIntersecting].
     */
    private fun causeSelfIntersection(index: Int): Boolean {
        val curr = getEdge(index)
        if (curr.isDegenerate) {
            return false
        }
        /*
         * First previous edge not degenerate
         */
        var i = circularPrevious(index)
        while (getEdge(i).isDegenerate) {
            i = circularPrevious(i)
        }
        val prevIndex = i
        val prev = getEdge(i)
        /*
         * First next edge not degenerate
         */
        i = circularNext(index)
        while (getEdge(i).isDegenerate) {
            i = circularNext(i)
        }
        val next = getEdge(i)
        return prev.intersect(curr) !is Intersection2D.SinglePoint ||
            curr.intersect(next) !is Intersection2D.SinglePoint ||
            /*
             * We check every edge between the first prev not
             * degenerate and the first next not degenerate.
             */
            generateSequence(circularNext(i)) { circularNext(it) }
                .takeWhile { it != prevIndex }
                .map { getEdge(it) }
                .filter { !it.isDegenerate }
                .any { curr.intersect(it) !is Intersection2D.None }
    }
}
