/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.geometry.euclidean2d.navigator

import it.unibo.alchemist.model.implementations.geometry.euclidean2d.AwtMutableConvexPolygon
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.AwtShapeExtension.vertices
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.Segment2DImpl
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.Vector2D
import it.unibo.alchemist.model.interfaces.geometry.Vector2D.Companion.zCross
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Intersection2D
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Segment2D
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.navigator.ExtendableConvexPolygon
import java.awt.Shape
import java.awt.geom.Line2D
import java.awt.geom.Point2D

/**
 * An [ExtendableConvexPolygon] located inside an environment with obstacles.
 * Obstacles, as well as the environment's boundaries, are taken into account when the polygon is
 * extended (i.e. the polygon is prevented from intersecting an obstacle or growing beyond such
 * boundaries).
 * A rectangular region is assumed, its [origin], [width] and [height] are to be specified. Both
 * [java.awt.Shape]s and [ConvexPolygon]s can be specified as obstacles (see [awtObstacles]
 * and [polygonalObstacles]).
 * This class is explicitly designed for the algorithm contained in [generateNavigationGraph].
 * TODO(improve the quality of this class)
 */
class ExtendableConvexPolygonInEnvironment(
    private val vertices: MutableList<Euclidean2DPosition>,
    private val origin: Euclidean2DPosition,
    /**
     * Width of the environment (only positive).
     */
    private val width: Double,
    /**
     * Height of the environment (only positive).
     */
    private val height: Double,
    /**
     * Obstacles represented as [java.awt.Shape]s, are assumed to be immutable and must be polygons
     * (i.e. shapes without curved segments).
     */
    private val awtObstacles: List<Shape>
) : AwtMutableConvexPolygon(vertices), ExtendableConvexPolygon {

    /**
     * Obstacles represented as [ConvexPolygon]s, are assumed to be mutable but limited to the extension
     * (i.e. they can only grow, not shrink). This is the behavior of seeds used by [generateNavigationGraph],
     * making this assumption allows to cache useful data such as whether an edge can still advance or
     * an obstacle has already been encountered. This is a var but is assumed to be set only once before
     * starting to extend this polygon.
     */
    lateinit var polygonalObstacles: List<ConvexPolygon>

    private val canEdgeAdvance: MutableList<Boolean> = MutableList(vertices.size) { true }

    /**
     * Caches the normal versor to each edge.
     */
    private val normals: MutableList<Euclidean2DPosition?> = MutableList(vertices.size) { null }

    /**
     * Caches the growth direction (a vector) of both the vertices of each edge, this is useful in
     * the advanced case (see [extend]).
     */
    private var growthDirections: MutableList<Pair<Euclidean2DPosition?, Euclidean2DPosition?>?> =
        MutableList(vertices.size) { null }

    override fun addVertex(index: Int, x: Double, y: Double): Boolean {
        val oldEdge = getEdge(circularPrevious(index))
        if (super.addVertex(index, x, y)) {
            addCacheAt(index)
            voidCacheAt(circularPrevious(index), oldEdge)
            return true
        }
        return false
    }

    override fun removeVertex(index: Int): Boolean {
        val oldEdge = getEdge(circularPrevious(index))
        if (super.removeVertex(index)) {
            removeCacheAt(index)
            voidCacheAt(circularPrevious(index), oldEdge)
            return true
        }
        return false
    }

    override fun moveVertex(index: Int, newX: Double, newY: Double): Boolean {
        val modifiedEdges = listOf(circularPrevious(index), index)
            .map { it to getEdge(it) }
        if (super.moveVertex(index, newX, newY)) {
            modifiedEdges.forEach { voidCacheAt(it.first, it.second) }
            return true
        }
        return false
    }

    override fun replaceEdge(index: Int, newEdge: Segment2D<Euclidean2DPosition>): Boolean {
        val modifiedEdges = listOf(circularPrevious(index), index, circularNext(index))
            .map { it to getEdge(it) }
        if (super.replaceEdge(index, newEdge)) {
            modifiedEdges.forEach { voidCacheAt(it.first, it.second) }
            return true
        }
        return false
    }

    /**
     * The cache is set to some default value, the same it assumes when it's voided.
     */
    private fun addCacheAt(index: Int) {
        canEdgeAdvance.add(index, true)
        growthDirections.add(index, null)
        normals.add(index, null)
    }

    private fun removeCacheAt(index: Int) {
        canEdgeAdvance.removeAt(index)
        growthDirections.removeAt(index)
        normals.removeAt(index)
    }

    /**
     * Usually cache related to an edge is voided when the edge is somehow modified. However, different
     * caches may apply different policies for voiding (e.g. a cache memorizing the normal of each edge
     * is to be voided if the slope of the edge changes). This method accepts the index of a modified edge
     * and the old edge and applies different policies to decide if each cache should be voided.
     */
    private fun voidCacheAt(index: Int, old: Segment2D<Euclidean2DPosition>) {
        val new = getEdge(index)
        canEdgeAdvance[index] = true
        if (!old.isParallelTo(new) && !(old.isDegenerate || new.isDegenerate)) {
            growthDirections[index] = null
            normals[index] = null
        }
    }

    /**
     * Advances an edge in its normal direction of a quantity equal to [step], if [extend] has
     * modified the growth direction of the edge so as to follow an oblique obstacle (advanced case),
     * the modified growth direction is used.
     * The polygon is prevented from growing out of the environment's boundaries, but not from
     * intersecting obstacles.
     */
    /*
     * Note that even when growth direction is modified the edge still advances in its normal direction.
     * What has been modified are the individual growth directions of its endpoints, which means its
     * slope will be preserved but not its length.
     * In order to guarantee that the advanced edge is always parallel to the old one (i.e. that the
     * edge actually advance in its normal direction), we need to resize the directions of growth
     * of the endpoints so that their component in the direction normal to the edge is equal to step.
     */
    override fun advanceEdge(index: Int, step: Double): Boolean = true.takeIf { step == 0.0 } ?: run {
        val edge = getEdge(index)
        if (edge.isDegenerate) {
            return false
        }
        if (normals[index] == null) {
            normals[index] = computeNormal(index, edge)
        }
        val normal = checkNotNull(normals[index]) { "internal error: no normal found" }
        cacheGrowthDirection(index, normal)
        val toMovementVector: (Euclidean2DPosition?).() -> Euclidean2DPosition = {
            requireNotNull(this) {
                "internal error: no growth direction found"
            }.let {
                val length = findLength(it, normal, step)
                require(length.isFinite()) { "internal error: invalid length" }
                it.resized(length)
            }
        }
        val firstMovement = growthDirections[index]?.first.toMovementVector()
        val secondMovement = growthDirections[index]?.second.toMovementVector()
        /*
         * super method is used in order to avoid voiding useful cache
         */
        if (super.replaceEdge(index, edge.copyWith(edge.first + firstMovement, edge.second + secondMovement))) {
            if (getEdge(index).isInRectangle(origin, width, height)) {
                return true
            }
            super.replaceEdge(index, edge)
        }
        return false
    }

    /**
     * Computes the normal vector to the specified edge, taking care that its verse allows to extend
     * the polygon and not shrink it (this is all about figuring out in which verse the polygon extends).
     */
    private fun computeNormal(index: Int, edge: Segment2D<Euclidean2DPosition> = getEdge(index)): Euclidean2DPosition {
        val curr = edge.toVector
        val prev = getEdge(circularPrevious(index)).toVector
        val normal = curr.normal().normalized()
        if (zCross(curr, normal) > 0.0 != zCross(curr, prev) > 0.0) {
            return normal * -1.0
        }
        return normal
    }

    /**
     * Caches the growth directions of both the vertices of the specified edge if they're not cached yet.
     */
    private fun cacheGrowthDirection(index: Int, normal: Euclidean2DPosition) {
        val growthDirection = growthDirections[index]
        if (growthDirection?.first == null || growthDirection.second == null) {
            if (growthDirection == null) {
                growthDirections[index] = Pair(normal, normal)
            } else {
                if (growthDirection.first == null) {
                    growthDirections[index] = growthDirection.copy(first = normal)
                }
                if (growthDirection.second == null) {
                    growthDirections[index] = growthDirection.copy(second = normal)
                }
            }
        }
    }

    /**
     * Given a vector a, we want to resize it so that its scalar projection on a second vector b
     * is equal to a certain quantity q. In order to do so, we need to know the length of the new
     * vector a'. This method computes that quantity. [bUnit] is b of unitary magnitude.
     */
    private fun findLength(a: Euclidean2DPosition, bUnit: Euclidean2DPosition, q: Double) = q / a.dot(bUnit)

    /**
     * Extends the polygon in each direction of a quantity equal to [step].
     * The advancement of an edge is blocked if an obstacle is intersected, unless in a
     * particular case called advanced case. Such case shows up when a single vertex of
     * the polygon intruded an obstacle, but no vertex from the obstacle intruded the polygon.
     * Plus, the intruded side of the obstacle should be oblique (or better, its slope should
     * be different from the one of the advancing edge).
     * When this happens, we can do a simple operation in order to keep growing and allow a
     * higher coverage of the walkable area. We increment the order of the polygon (by adding
     * a vertex) and adjust the direction of growth in order for the new edge to follow the
     * side of the obstacle.
     */
    override fun extend(step: Double): Boolean {
        val obstacles = awtObstacles + polygonalObstacles.map { it.asAwtShape() }
        var extended = false
        vertices.indices.filter { canEdgeAdvance[it] }.forEach { i ->
            val hasAdvanced = advanceEdge(i, step)
            val intersectedObs = obstacles.filter { intersects(it) }
            /*
             * Returns true if no obstacle is intersected or if we are in the advanced case.
             */
            val isAdvancedCase: () -> Boolean = {
                /*
                 * Can be in the advanced case for at most 2 obstacles.
                 */
                intersectedObs.size <= 2 && intersectedObs.all { isAdvancedCase(it, i, step) }
            }
            if (hasAdvanced && getEdge(i).isInRectangle(origin, width, height) && isAdvancedCase()) {
                intersectedObs.forEach { adjustGrowth(it, i, step) }
                extended = true
            } else {
                if (hasAdvanced) {
                    advanceEdge(i, -step)
                }
                // set a flag in order to stop trying to extend this edge
                canEdgeAdvance[i] = false
            }
        }
        return extended
    }

    /*
     * Checks whether we are in advanced case. See [extend]. The index of the
     * growing edge and the step of growth should be provided as well.
     */
    private fun isAdvancedCase(obstacle: Shape, index: Int, step: Double) =
        obstacle.vertices().none { containsBoundaryIncluded(it) } &&
            vertices.filter { obstacle.contains(it.toPoint()) }.size == 1 &&
            !firstIntrudedEdge(obstacle, index, step).isParallelTo(getEdge(index))

    /*
     * During the advancement of an edge, multiple edges of an obstacle may be
     * intersected. This method allows to find the first intruded edge in the
     * advanced case (i.e., the one that the polygon first intruded during its
     * advancement). The index of the growing edge and the step of growth should
     * be provided as well.
     */
    private fun firstIntrudedEdge(obstacle: Shape, index: Int, step: Double): Segment2D<Euclidean2DPosition> {
        var intrudingVertex = getEdge(index).first
        var growthDirection = growthDirections[index]?.first
        if (!obstacle.contains(intrudingVertex.toPoint())) {
            intrudingVertex = getEdge(index).second
            growthDirection = growthDirections[index]?.second
        }
        checkNotNull(growthDirection) { "no growth direction found" }
        /*
         * a segment going from the old position of the intruding vertex to the new one
         */
        val movementSegment = Segment2DImpl(intrudingVertex, intrudingVertex - growthDirection.resized(step))
        val intrudedEdges = findIntersectingEdges(obstacle, movementSegment)
        require(intrudedEdges.size == 1) { "vertex is not intruding" }
        return intrudedEdges.first()
    }

    /*
     * Finds the edges of the obstacle intersecting with the given edge of the polygon.
     */
    private fun findIntersectingEdges(obstacle: Shape, e: Segment2D<Euclidean2DPosition>) =
        obstacle.vertices().run {
            mapIndexed { i, v -> Segment2DImpl(v, this[(i + 1) % size]) }.filter { edgesIntersect(it, e) }
        }

    /*
     * Delegates the check to java.awt.geom.Line2D.
     */
    private fun edgesIntersect(e1: Segment2D<*>, e2: Segment2D<*>) =
        Line2D.Double(e1.first.toPoint(), e1.second.toPoint())
            .intersectsLine(e2.first.x, e2.first.y, e2.second.x, e2.second.y)

    private val Vector2D<*>.toEuclidean get() = when (this) {
        is Euclidean2DPosition -> this
        else -> Euclidean2DPosition(x, y)
    }

    /*
     * Adjusts the growth directions in the advanced case. See [extend].
     */
    private fun adjustGrowth(obstacle: Shape, indexOfAdvancingEdge: Int, step: Double) {
        val indexOfIntrudingV = vertices.indexOfFirst { obstacle.contains(it.toPoint()) }
        // intersecting edges
        val polygonEdge1 = getEdge(indexOfIntrudingV)
        val polygonEdge2 = getEdge(circularPrevious(indexOfIntrudingV))
        val obstacleEdge: Segment2D<Euclidean2DPosition> = firstIntrudedEdge(obstacle, indexOfAdvancingEdge, step)
        // intersecting points lying on polygon boundary
        val intersection1 = polygonEdge1.intersect(obstacleEdge)
        val intersection2 = polygonEdge2.intersect(obstacleEdge)
        require(intersection1 is Intersection2D.SinglePoint && intersection2 is Intersection2D.SinglePoint) {
            "Bug in the Alchemist geometric engine. Found in ${this::class.qualifiedName}"
        }
        val p1 = intersection1.point.toEuclidean
        val p2 = intersection2.point.toEuclidean
        // a new edge is going to be added, its vertices will grow following the intruded
        // obstacleEdge. In order to do so, their growth directions will be modified to be
        // parallel to such edge, but in opposite senses.
        val d1: Euclidean2DPosition
        val d2: Euclidean2DPosition
        if (p1.distanceTo(obstacleEdge.first) < p2.distanceTo(obstacleEdge.first)) {
            d1 = (obstacleEdge.first - p1).normalized()
            d2 = (obstacleEdge.second - p2).normalized()
        } else {
            d1 = (obstacleEdge.second - p1).normalized()
            d2 = (obstacleEdge.first - p2).normalized()
        }
        // since we intruded an obstacle we need to step back anyway
        advanceEdge(indexOfAdvancingEdge, -step)
        modifyGrowthDirection(indexOfIntrudingV, d1, true)
        addVertex(indexOfIntrudingV, vertices[indexOfIntrudingV].x, vertices[indexOfIntrudingV].y)
        canEdgeAdvance[indexOfIntrudingV] = false
        modifyGrowthDirection(circularPrevious(indexOfIntrudingV), d2, false)
    }

    private fun modifyGrowthDirection(i: Int, newD: Euclidean2DPosition, first: Boolean) {
        val d = growthDirections[i]
        if (d == null) {
            growthDirections[i] = if (first) Pair(newD, null) else Pair(null, newD)
        } else {
            growthDirections[i] = if (first) d.copy(first = newD) else d.copy(second = newD)
        }
    }

    private fun Vector2D<*>.toPoint() = Point2D.Double(x, y)

    override fun equals(other: Any?) = super.equals(other)

    override fun hashCode() = super.hashCode()
}
