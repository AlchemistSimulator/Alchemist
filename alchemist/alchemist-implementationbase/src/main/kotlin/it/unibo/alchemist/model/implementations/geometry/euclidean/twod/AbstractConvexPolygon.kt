/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.geometry.euclidean.twod

import it.unibo.alchemist.model.implementations.geometry.SegmentsIntersectionType
import it.unibo.alchemist.model.implementations.geometry.intersection
import it.unibo.alchemist.model.implementations.geometry.vertices
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.Vector2D
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Segment2D
import java.awt.Shape
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import kotlin.math.min

/**
 * An abstract [ConvexPolygon] providing a convexity test.
 */
abstract class AbstractConvexPolygon : ConvexPolygon {

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

    override fun isAdjacentTo(shape: Shape): Boolean = isAdjacentTo(fromShape(shape).orElseThrow {
        IllegalArgumentException("given shape is not convex polygonal")
    })

    override fun intersects(segment: Segment2D<Euclidean2DPosition>): Boolean {
        if (containsBoundaryExcluded(segment.first) || containsBoundaryExcluded(segment.second)) {
            return true
        }
        val intersections = edges().map { intersection(it, segment) }
        return when {
            intersections.any { it.type == SegmentsIntersectionType.SEGMENT } -> false
            else -> intersections.mapNotNull { it.point.orElse(null) }.distinct().size > 1
        }
    }

    override fun closestEdgeTo(segment: Segment2D<Euclidean2DPosition>): Segment2D<Euclidean2DPosition> =
        edges().minWith(
            compareBy({
                it.distanceTo(segment)
            }, {
                min(segment.distanceTo(it.first) + segment.distanceTo(it.second),
                    it.distanceTo(segment.first) + it.distanceTo(segment.second))
            })
        ) ?: throw IllegalStateException("no edge found")

    override fun toString(): String = javaClass.simpleName + vertices()

    /**
     * Computes the circular previous index in the [vertices] collection.
     */
    protected fun circularPrev(index: Int) = vertices().size.let { (index - 1 + it) % it }

    /**
     * Computes the circular next index in the [vertices] collection.
     */
    protected fun circularNext(index: Int) = (index + 1) % vertices().size

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
        if (edges().filter { !it.isDegenerate }.size < 3) {
            return false
        }
        var e1 = getEdge(vertices().size - 1)
        var sense: Boolean? = null
        return edges().none { e2 ->
            val z = Vector2D.zCross(e1.toVector(), e2.toVector())
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
        var i = circularPrev(index)
        while (getEdge(i).isDegenerate) {
            i = circularPrev(i)
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
        return when {
            intersection(prev, curr).type != SegmentsIntersectionType.POINT ||
                intersection(curr, next).type != SegmentsIntersectionType.POINT -> true
            /*
             * We check every edge between the first prev not
             * degenerate and the first next not degenerate.
             */
            else -> generateSequence(circularNext(i)) { circularNext(it) }
                .takeWhile { it != prevIndex }
                .map { getEdge(it) }
                .filter { !it.isDegenerate }
                .any { intersection(curr, it).type != SegmentsIntersectionType.EMPTY }
        }
    }
}
