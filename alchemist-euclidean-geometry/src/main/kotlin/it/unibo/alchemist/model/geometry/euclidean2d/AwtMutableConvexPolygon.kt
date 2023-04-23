/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geometry.euclidean2d

import it.unibo.alchemist.model.geometry.AwtShapeCompatible
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.MutableConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Segment2D
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import java.awt.Shape
import java.awt.geom.Area
import java.awt.geom.Path2D

/**
 * [MutableConvexPolygon] partly delegated to [AwtEuclidean2DShape] and [java.awt.geom].
 * Each modification operation on this object has a time complexity of O(n), where n is
 * the number of vertices/edges.
 * Collinear points are allowed.
 */
open class AwtMutableConvexPolygon(
    /**
     * Vertices must be sorted as indicated in [ConvexPolygon.vertices].
     */
    private val vertices: MutableList<Euclidean2DPosition>,
) : AbstractConvexPolygon(), MutableConvexPolygon {

    init {
        require(isConvex()) { "Given vertices do not represent a convex polygon" }
        /*
         * Remove collinear vertices, this is the only time this operation is performed
         */
        var i = 0
        while (i < vertices.size) {
            val prev = vertices[circularPrevious(i)]
            val curr = vertices[i]
            val next = vertices[circularNext(i)]
            if (Segment2DImpl(prev, curr).isCollinearWith(next)) {
                vertices.removeAt(i)
                i--
            }
            i++
        }
    }

    /*
     * An AwtEuclidean2DShape is immutable, thus composition is used over inheritance.
     */
    private var shape: AwtEuclidean2DShape? = null

    /*
     * Custom getter allows re-computation of the value
     */
    override val diameter: Double get() = getShape().diameter

    override val centroid: Euclidean2DPosition get() = getShape().centroid

    override fun vertices(): List<Euclidean2DPosition> = vertices

    /**
     * @returns the specified edge of the polygon, this is faster than [edges].get([index]).
     */
    override fun getEdge(index: Int) = Segment2DImpl(vertices[index], vertices[circularNext(index)])

    override fun edges(): List<Segment2D<Euclidean2DPosition>> = vertices.indices.map { getEdge(it) }

    override fun addVertex(index: Int, x: Double, y: Double): Boolean {
        vertices.add(index, Euclidean2DPosition(x, y))
        /*
         * Only the modified/new edges are passed, which vary depending
         * on the operation performed (addition/removal of a vertex/edge).
         */
        if (isConvex(circularPrevious(index), index)) {
            shape = null
            return true
        }
        vertices.removeAt(index)
        return false
    }

    override fun removeVertex(index: Int): Boolean {
        val oldVertex = vertices[index]
        vertices.removeAt(index)
        if (isConvex(circularPrevious(index))) {
            shape = null
            return true
        }
        vertices.add(index, oldVertex)
        return false
    }

    override fun moveVertex(index: Int, newX: Double, newY: Double): Boolean {
        val oldVertex = vertices[index]
        vertices[index] = Euclidean2DPosition(newX, newY)
        if (isConvex(circularPrevious(index), index)) {
            shape = null
            return true
        }
        vertices[index] = oldVertex
        return false
    }

    override fun replaceEdge(index: Int, newEdge: Segment2D<Euclidean2DPosition>): Boolean {
        val oldEdge = getEdge(index)
        vertices[index] = newEdge.first
        vertices[circularNext(index)] = newEdge.second
        if (isConvex(circularPrevious(index), index, circularNext(index))) {
            shape = null
            return true
        }
        replaceEdge(index, oldEdge)
        return false
    }

    /**
     * Delegated to [AwtEuclidean2DShape] (adopts the definition of insideness used by
     * [java.awt.Shape]s).
     */
    override fun contains(vector: Euclidean2DPosition): Boolean = getShape().contains(vector)

    /**
     * Delegated to [java.awt.geom.Area], this is accurate and does not consider adjacent
     * shapes to be intersecting.
     */
    override fun intersects(shape: Shape): Boolean = with(Area(asAwtShape())) {
        intersect(Area(shape))
        !isEmpty
    }

    /**
     * Delegated to [AwtEuclidean2DShape] unless [other] is [AwtShapeCompatible], in which case
     * [intersects] is used so as to guarantee maximum accuracy.
     */
    override fun intersects(other: Euclidean2DShape) = when (other) {
        is AwtShapeCompatible -> intersects(other.asAwtShape())
        else -> getShape().intersects(other)
    }

    override fun transformed(transformation: Euclidean2DTransformation.() -> Unit) =
        getShape().transformed(transformation) as Euclidean2DShape

    final override fun asAwtShape() = getShape().asAwtShape()

    override fun equals(other: Any?) =
        other != null && (this === other || other is AwtMutableConvexPolygon && vertices == other.vertices)

    override fun hashCode() = vertices.hashCode()

    /*
     * If the cache is not valid, recomputes it.
     */
    private fun getShape(): AwtEuclidean2DShape {
        if (shape == null) {
            /*
             * a Path2D is used to represent a Polygon in double precision.
             */
            val path = Path2D.Double()
            vertices.forEachIndexed { i, p ->
                if (i == 0) {
                    path.moveTo(p.x, p.y)
                } else {
                    path.lineTo(p.x, p.y)
                }
            }
            path.closePath()
            shape = AwtEuclidean2DShape(path)
        }
        return shape as AwtEuclidean2DShape
    }
}
