package it.unibo.alchemist.model.implementations.geometry.euclidean.twod

import it.unibo.alchemist.model.implementations.geometry.contains
import it.unibo.alchemist.model.implementations.geometry.vertices
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.GeometricShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.MutableConvexPolygon
import java.awt.Shape
import java.awt.geom.Area
import java.awt.geom.Path2D
import java.util.Optional

/**
 * Implementation of a [MutableConvexPolygon].
 */
open class MutableConvexPolygonImpl (
        private val vertices: MutableList<Euclidean2DPosition>
) : MutableConvexPolygon {

    companion object {
        /**
         * Creates a MutableConvexPolygon from a java.awt.Shape.
         * If the Polygon could not be created (e.g. because of the
         * non-convexity of the given shape), an empty optional is
         * returned.
         * Each curved segment of the shape will be considered as
         * a straight line.
         */
        fun fromShape(s: Shape): Optional<MutableConvexPolygon> {
            return try {
                Optional.of(MutableConvexPolygonImpl(s.vertices().toMutableList()))
            } catch (e: IllegalArgumentException) {
                Optional.empty()
            }
        }
    }

    /*
     * An AwtEuclidean2DShape is immutable, thus composition is used
     * over inheritance.
     */
    private var shape: AwtEuclidean2DShape? = null

    override val diameter: Double = getShape().diameter

    override val centroid: Euclidean2DPosition = getShape().centroid

    override fun vertices(): List<Euclidean2DPosition> = vertices

    override fun addVertex(x: Double, y: Double) = addVertex(vertices.size, x, y)

    override fun addVertex(index: Int, x: Double, y: Double): Boolean {
        vertices.add(index, Euclidean2DPosition(x, y))
        if (isConvex()) {
            shape = null // invalid cached shape
            return true
        }
        vertices.removeAt(index)
        return false
    }

    override fun removeVertex(index: Int): Boolean {
        if (vertices.size == 3) {
            return false
        }
        val v = vertices[index]
        vertices.removeAt(index)
        if (isConvex()) {
            shape = null // invalid cached shape
            return true
        }
        vertices.add(index, v)
        return false
    }

    override fun moveVertex(index: Int, newX: Double, newY: Double): Boolean {
        val oldV = vertices[index]
        vertices[index] = Euclidean2DPosition(newX, newY)
        if (isConvex()) {
            shape = null // invalid cached shape
            return true
        }
        vertices[index] = oldV
        return false
    }

    override fun getEdge(index: Int) = Pair(vertices[index], vertices[(index + 1) % vertices.size])

    override fun moveEdge(index: Int, newEdge: Pair<Euclidean2DPosition, Euclidean2DPosition>): Boolean {
        val oldEdge = getEdge(index)
        vertices[index] = newEdge.first
        vertices[(index + 1) % vertices.size] = newEdge.second
        if (isConvex()) {
            shape = null // invalid cached shape
            return true
        }
        moveEdge(index, oldEdge)
        return false
    }

    override fun intersects(other: Euclidean2DShape) = getShape().intersects(other)

    /*
     * Delegates to java.awt.Area.
     */
    override fun intersects(other: Shape): Boolean {
        val a = Area(asAwtShape())
        a.intersect(Area(other))
        return !a.isEmpty
    }

    override fun contains(vector: Euclidean2DPosition) = getShape().contains(vector)

    override fun containsOrLiesOnBoundary(vector: Euclidean2DPosition) =
        contains(vector) || vertices.indices.map { getEdge(it) }.any { it.contains(vector) }

    /*
     * Delegates to java.awt.Area.
     */
    override fun union(polygons: Collection<ConvexPolygon>): Boolean {
        if (polygons.any { !intersects(it.asAwtShape()) }) {
            return false // polygons are not overlapping
        }
        val union = Area(asAwtShape())
        polygons.forEach { union.add(Area(it.asAwtShape())) }
        val resultingPolygon = fromShape(union)
        if (resultingPolygon.isPresent) {
            mutateTo(resultingPolygon.get())
            return true
        }
        return false
    }

    override fun transformed(transformation: Euclidean2DTransformation.() -> Unit) =
        getShape().transformed(transformation) as GeometricShape<Euclidean2DPosition, Euclidean2DTransformation>

    final override fun asAwtShape() = getShape().asAwtShape()

    /**
     * Mutates this polygon to a copy of the specified one.
     */
    protected open fun mutateTo(p: MutableConvexPolygon) {
        p.vertices().forEachIndexed { i, newV ->
            if (i < vertices.size) {
                vertices[i] = newV
            } else {
                vertices.add(i, newV)
            }
        }
        while (vertices.size > p.vertices().size) {
            vertices.removeAt(vertices.size - 1)
        }
        shape = null // invalid cached shape
    }

    /*
     * Basically, we check that every edge either turn left or
     * right with respect to the previous edge. If they all
     * turn in the same direction then the polygon is convex.
     */
    private fun isConvex(): Boolean {
        if (vertices.isEmpty()) {
            return true
        }
        var e1 = getEdge(0)
        var e2: Pair<Euclidean2DPosition, Euclidean2DPosition>
        var sense: Boolean? = null
        for (i in 1 until vertices.size) {
            e2 = getEdge(i)
            val z = computeZCrossProduct(e1, e2)
            if (z != 0.0) {
                if (sense == null) {
                    sense = z > 0.0
                } else if (sense != z > 0.0) {
                    return false
                }
            }
            e1 = e2
        }
        return true
    }

    /*
     * If the cache is not valid, recomputes it.
     */
    private fun getShape(): AwtEuclidean2DShape {
        if (shape == null) {
            // a Path2D is used to represent a Polygon in double precision
            val s = Path2D.Double()
            vertices.forEachIndexed { i, p ->
                if (i == 0) {
                    s.moveTo(p.x, p.y)
                } else {
                    s.lineTo(p.x, p.y)
                }
            }
            s.closePath()
            shape = AwtEuclidean2DShape(s)
        }
        return shape as AwtEuclidean2DShape
    }

    /*
     * Z component of the cross product.
     */
    private fun computeZCrossProduct(e1: Pair<Euclidean2DPosition, Euclidean2DPosition>,
                                     e2: Pair<Euclidean2DPosition, Euclidean2DPosition>) =
        (e1.second.x - e1.first.x) * (e2.second.y - e1.second.y) -
            (e1.second.y - e1.first.y) * (e2.second.y - e1.second.y)

    /**
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }
        other as MutableConvexPolygonImpl
        if (vertices != other.vertices) {
            return false
        }
        return true
    }

    /**
     */
    override fun hashCode(): Int {
        return vertices.hashCode()
    }
}
