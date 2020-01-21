package it.unibo.alchemist.model.implementations.geometry.navigationmeshes.deaccon

import it.unibo.alchemist.model.implementations.geometry.*
import it.unibo.alchemist.model.implementations.geometry.euclidean.twod.MutableConvexPolygonImpl
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.MutableConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.navigationmeshes.deaccon.ExtendableConvexPolygon
import java.awt.Shape
import java.awt.geom.*
import java.awt.geom.Point2D

/**
 * Implementation of [ExtendableConvexPolygon]
 */
open class ExtendableConvexPolygonImpl(
        private val vertices: MutableList<Euclidean2DPosition>
) : MutableConvexPolygonImpl(vertices), ExtendableConvexPolygon {

    /**
     * A mutable list designed to cache a value for each edge, extended
     * with some useful methods.
     *
     * Note that edges (or sides) of a polygon are not directed. However,
     * the ith edge is referred to as the one connecting vertices i and i+1.
     * Thus, in the following we will adopt such directing.
     */
    inner class EdgesCache<T>(
        /**
         */
        val default: T
    ) : MutableList<T> by (MutableList(vertices.size) { default }) {

        /**
         * Sets the specified value for the outgoing edge of the given vertex.
         */
        private fun setValueForEdgeFrom(vertexIndex: Int, v: T) {
            this[vertexIndex] = v
        }

        /**
         * Sets the specified value for the incoming edge of the given vertex.
         */
        fun setValueForEdgeTo(vertexIndex: Int, v: T) {
            this[circularPrev(vertexIndex)] = v
        }

        /**
         * Sets the specified value for both the edges incident on the given vertex.
         */
        fun setValueForEdgesIncidentOn(vertexIndex: Int, v: T) {
            setValueForEdgeFrom(vertexIndex, v)
            setValueForEdgeTo(vertexIndex, v)
        }

        /**
         * Sets to the default value the specified edge.
         */
        fun setDefault(edgeIndex: Int) {
            this[edgeIndex] = default
        }

    }

    private var canEdgeAdvance: EdgesCache<Boolean> = EdgesCache(true)
    /*
     * Caches the growth direction (expressed as a Point2D) of both of the
     * vertices of each edge. See [advanceEdge].
     */
    private var growthDirections: EdgesCache<Pair<Euclidean2DPosition?, Euclidean2DPosition?>?> = EdgesCache(null)
    private var normals: EdgesCache<Euclidean2DPosition?> = EdgesCache(null)

    override fun addVertex(index: Int, x: Double, y: Double): Boolean {
        if (super.addVertex(index, x, y)) { // invalid all involved caches
            canEdgeAdvance.add(index, canEdgeAdvance.default)
            canEdgeAdvance.setValueForEdgeTo(index, canEdgeAdvance.default)
            growthDirections.add(index, growthDirections.default)
            growthDirections.setValueForEdgeTo(index, growthDirections.default)
            normals.add(index, normals.default)
            normals.setValueForEdgeTo(index, normals.default)
            return true
        }
        return false
    }

    override fun removeVertex(index: Int): Boolean {
        if (super.removeVertex(index)) {
            canEdgeAdvance.removeAt(index)
            canEdgeAdvance.setValueForEdgeTo(index, canEdgeAdvance.default)
            growthDirections.removeAt(index)
            growthDirections.setValueForEdgeTo(index, growthDirections.default)
            normals.removeAt(index)
            normals.setValueForEdgeTo(index, normals.default)
            return true
        }
        return false
    }

    override fun moveVertex(index: Int, newX: Double, newY: Double): Boolean {
        if (super.moveVertex(index, newX, newY)) {
            canEdgeAdvance.setValueForEdgesIncidentOn(index, canEdgeAdvance.default)
            growthDirections.setValueForEdgesIncidentOn(index, growthDirections.default)
            normals.setValueForEdgesIncidentOn(index, normals.default)
            return true
        }
        return false
    }

    override fun moveEdge(index: Int, newEdge: Pair<Euclidean2DPosition, Euclidean2DPosition>): Boolean {
        // when moving an edge 3 edges are modified: the one moving and the two linked to it
        val oldEdge1 = getEdge(circularPrev(index))
        val oldEdge2 = getEdge(index)
        val oldEdge3 = getEdge((index + 1) % vertices.size)
        if (super.moveEdge(index, newEdge)) {
            invalidCacheIfSlopeChanged(oldEdge1, circularPrev(index))
            invalidCacheIfSlopeChanged(oldEdge2, index)
            invalidCacheIfSlopeChanged(oldEdge3, (index + 1) % vertices.size)
            return true
        }
        return false
    }

    override fun mutateTo(p: MutableConvexPolygon) {
        super.mutateTo(p)
        canEdgeAdvance = EdgesCache(canEdgeAdvance.default)
        growthDirections = EdgesCache(growthDirections.default)
        normals = EdgesCache(normals.default)
    }

    /*
     * Edges only grow in their normal direction. Normally, an edge is advanced
     * by translating both of its vertices with the normal vector of the edge
     * (resized to have magnitude equal to the step parameter). However, vertices
     * may sometimes need to advance in different directions, for instance in order
     * to follow an oblique edge of an obstacle. In such cases we want to guarantee
     * that the advanced edge is always parallel to the old one. This may be non-
     * trivial when the vertices of a given edge e have different directions of
     * growth. In order to preserve the parallelism with the old edge, we need to
     * resize the two directions of growth so that their component in the direction
     * normal to e is equal to step.
     */
    override fun advanceEdge(index: Int, step: Double): Boolean {
        val e = getEdge(index)
        if (e.first == e.second) { // avoid degenerate edges
            return false
        }
        if (normals[index] == null) {
            normals[index] = computeNormal(getEdge(index), step)
        }
        val n = normals[index]!!
        val d = growthDirections[index]
        if (d?.first == null || d.second == null) {
            if (d == null) {
                growthDirections[index] = Pair(n, n)
            } else {
                if (d.first == null) {
                    growthDirections[index] = d.copy(first = n)
                }
                if (d.second == null) {
                    growthDirections[index] = d.copy(second = n)
                }
            }
        }
        var d1 = growthDirections[index]!!.first!!
        var d2 = growthDirections[index]!!.second!!
        d1 = d1.resize(findLength(d1, n, step))
        d2 = d2.resize(findLength(d2, n, step))
        // super method is used in order to avoid useless checks that would invalid useful cache
        return super.moveEdge(index, Pair(e.first.translate(d1), e.second.translate(d2)))
    }

    /*
     * The advancement of an edge is blocked if an obstacle is intersected, unless in a
     * particular case called advanced case. Such case shows up when a single vertex of
     * the polygon intruded an obstacle, but no vertex from the obstacle intruded the polygon.
     * When this happens, we can do a simple operation in order to keep growing and allow a
     * higher coverage of the walkable area. We increment the order of the polygon (by adding
     * a vertex) and adjust the direction of growth in order for the new edge to follow the
     * side of the obstacle.
     */
    override fun extend(step: Double, obstacles: Collection<Shape>, envWidth: Double, envHeight: Double): Boolean {
        var extended = false
        var i = 0
        while (i < vertices.size) {
            if (canEdgeAdvance[i]) {
                val hasAdvanced = advanceEdge(i, step)
                if (hasAdvanced && isInBoundaries(getEdge(i), envWidth, envHeight)) {
                    val intersectedObs = obstacles.filter { intersects(it) }
                    // can be in the advanced case for at most 2 obstacle at a time
                    if (intersectedObs.size <= 2 && intersectedObs.all { isAdvancedCase(it) }) {
                        if (intersectedObs.isNotEmpty()) {
                            intersectedObs.forEach { adjustGrowth(it, i, step) }
                        }
                        extended = true
                        i++
                        continue
                    }
                }
                if (hasAdvanced) {
                    advanceEdge(i, -step)
                }
                // set a flag in order to stop trying to extend this edge
                canEdgeAdvance[i] = false
            }
            i++
        }
        return extended
    }

    private fun invalidCacheIfSlopeChanged(oldEdge: Pair<Euclidean2DPosition, Euclidean2DPosition>, i: Int) {
        val oldM = oldEdge.computeSlope()
        val newM = getEdge(i).computeSlope()
        if (oldM != newM && !(oldM.isNaN() && newM.isNaN())) {
            canEdgeAdvance.setDefault(i)
            growthDirections.setDefault(i)
            normals.setDefault(i)
        }
    }

    /*
     * Computes the normal unit vector of the given edge.
     *
     * Since every segment has two normal vectors, a little workaround is used
     * to determine which of the two is to be used. Both the normals are computed,
     * then one of the two is picked and normalized to have length equal to the step
     * parameter. The resulting vector is used to translate the medium point of the
     * edge: if step is > 0 (polygon is growing) and the translated point is contained
     * in the polygon, we need to pick the other vector, otherwise we will reduce the
     * region instead of extending it. Similarly, if step is < 0 (polygon is reducing)
     * and the translated point is not contained in the polygon, we need to change normal.
     * This is a NON-OPTIMAL method, in particular it fails when the step parameter is
     * bigger than the smallest side of the region (which is unlikely in the deaccon
     * algorithm). The trick is to be refined to be OPTIMAL (or replaced with a correct
     * method).
     */
    private fun computeNormal(e: Pair<Euclidean2DPosition, Euclidean2DPosition>, step: Double): Euclidean2DPosition {
        val dx = e.second.x - e.first.x
        val dy = e.second.y - e.first.y
        if (dx == 0.0 && dy == 0.0) {
            println("a")
        }
        require(dx != 0.0 || dy != 0.0) { "coincident points" }
        val normal1 = Euclidean2DPosition(-dy, dx)
        val normal2 = Euclidean2DPosition(dy, -dx)
        var normal = normal1.resize(step)
        val midPointT = e.midPoint().translate(normal)
        if ((step > 0.0 && containsOrLiesOnBoundary(midPointT))
                || (step < 0.0 && !containsOrLiesOnBoundary(midPointT))) {
            normal = normal2
        }
        return normal.normalize()
    }

    /*
     * Given a vector a, we want to resize it so that its scalar projection
     * on a second vector b is equal to a certain quantity q. In order to do
     * so, we need to know the length of the new vector a'. This method computes
     * this quantity.
     */
    private fun findLength(a: Euclidean2DPosition, bUnit: Euclidean2DPosition, q: Double): Double {
        return q / (a.x * bUnit.x + a.y * bUnit.y)
    }

    private fun isAdvancedCase(obstacle: Shape): Boolean {
        return obstacle.vertices().none { containsOrLiesOnBoundary(it) }
                && vertices.filter { obstacle.contains(it.toPoint()) }.size == 1
    }

    /*
     * Adjusts the growth directions in the advanced case. See [extend].
     */
    private fun adjustGrowth(obstacle: Shape, indexOfAdvancingEdge: Int, step: Double) {
        val indexOfIntrudingV = vertices.indexOfFirst { obstacle.contains(it.toPoint()) }
        // intersecting edges
        val e1 = getEdge(indexOfIntrudingV)
        val e2 = getEdge(circularPrev(indexOfIntrudingV))
        val obstacleEdge = findIntersectingEdge(obstacle, e1)
        check(obstacleEdge == findIntersectingEdge(obstacle, e2)) { "not in advanced case" }
        // intersecting points lying on polygon boundary
        val p1 = intersection(e1, obstacleEdge)
        val p2 = intersection(e2, obstacleEdge)
        // a new edge is going to be added, its vertices will grow following the intruded
        // obstacleEdge. In order to do so, their growth directions will be modified to be
        // parallel to such edge, but in opposite senses.
        val d1: Euclidean2DPosition
        val d2: Euclidean2DPosition
        if (p1.toPoint().distance(obstacleEdge.first.toPoint()) < p2.toPoint().distance(obstacleEdge.first.toPoint())) {
            d1 = obstacleEdge.first.minus(p1).normalize()
            d2 = obstacleEdge.second.minus(p2).normalize()
        } else {
            d1 = obstacleEdge.second.minus(p1).normalize()
            d2 = obstacleEdge.first.minus(p2).normalize()
        }
        advanceEdge(indexOfAdvancingEdge, -step)
        modifyGrowthDirection(indexOfIntrudingV, d1, true)
        addVertex(indexOfIntrudingV, vertices[indexOfIntrudingV].x, vertices[indexOfIntrudingV].y)
        canEdgeAdvance[indexOfIntrudingV] = false
        modifyGrowthDirection(circularPrev(indexOfIntrudingV), d2, false)
    }

    /*
     * Finds the edge of the obstacle intersecting with the given edge of the polygon.
     */
    private fun findIntersectingEdge(obstacle: Shape, e: Pair<Euclidean2DPosition, Euclidean2DPosition>): Pair<Euclidean2DPosition, Euclidean2DPosition> {
        val obsVertices = obstacle.vertices()
        for (i in obsVertices.indices) {
            val p1 = obsVertices[i]
            val p2 = obsVertices[(i + 1) % obsVertices.size]
            if (Line2D.Double(p1.toPoint(), p2.toPoint()).intersectsLine(e.first.x, e.first.y, e.second.x, e.second.y)) {
                return Pair(p1, p2)
            }
        }
        throw IllegalArgumentException("no edge of the obstacle is intersecting the given edge")
    }

    private fun modifyGrowthDirection(i: Int, newD: Euclidean2DPosition, first: Boolean) {
        val d = growthDirections[i]
        if (d == null) {
            growthDirections[i] = if (first) Pair(newD, null) else Pair(null, newD)
        } else {
            growthDirections[i] = if (first) d.copy(first = newD) else d.copy(second = newD)
        }
    }

    private fun circularPrev(index: Int): Int {
        return (index - 1 + canEdgeAdvance.size) % canEdgeAdvance.size
    }

    private fun Euclidean2DPosition.toPoint() = Point2D.Double(x, y)

}