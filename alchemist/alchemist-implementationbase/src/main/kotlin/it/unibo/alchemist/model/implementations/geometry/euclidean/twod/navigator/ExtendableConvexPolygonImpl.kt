package it.unibo.alchemist.model.implementations.geometry.euclidean.twod.navigator

import it.unibo.alchemist.model.implementations.geometry.isDegenerate
import it.unibo.alchemist.model.implementations.geometry.resize
import it.unibo.alchemist.model.implementations.geometry.vertices
import it.unibo.alchemist.model.implementations.geometry.isInBoundaries
import it.unibo.alchemist.model.implementations.geometry.slope
import it.unibo.alchemist.model.implementations.geometry.intersection
import it.unibo.alchemist.model.implementations.geometry.normalize
import it.unibo.alchemist.model.implementations.geometry.normal
import it.unibo.alchemist.model.implementations.geometry.zCross
import it.unibo.alchemist.model.implementations.geometry.times
import it.unibo.alchemist.model.implementations.geometry.toVector
import it.unibo.alchemist.model.implementations.geometry.dot
import it.unibo.alchemist.model.implementations.geometry.euclidean.twod.MutableConvexPolygonImpl
import it.unibo.alchemist.model.implementations.geometry.euclidean.twod.containsBoundaryIncluded
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DSegment
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.MutableConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.navigator.ExtendableConvexPolygon
import org.danilopianini.lang.MathUtils.fuzzyEquals
import java.awt.Shape
import java.awt.geom.Point2D
import java.awt.geom.Line2D

/**
 * Implementation of [ExtendableConvexPolygon].
 */
open class ExtendableConvexPolygonImpl(
    private val vertices: MutableList<Euclidean2DPosition>
) : MutableConvexPolygonImpl(vertices),
    ExtendableConvexPolygon {

    private var canEdgeAdvance: MutableList<Boolean> = MutableList(vertices.size) { true }
    /*
     * Caches the growth direction (expressed as a vector) of both of the
     * vertices of each edge. See [advanceEdge].
     */
    private var growthDirections: MutableList<Pair<Euclidean2DPosition?, Euclidean2DPosition?>?> =
        MutableList(vertices.size) { null }
    private var normals: MutableList<Euclidean2DPosition?> = MutableList(vertices.size) { null }

    override fun addVertex(index: Int, x: Double, y: Double): Boolean {
        val oldEdge = getEdge(circularPrev(index))
        if (super.addVertex(index, x, y)) {
            addCacheAt(index)
            voidCacheAt(circularPrev(index), oldEdge)
            return true
        }
        return false
    }

    override fun removeVertex(index: Int): Boolean {
        val oldEdge = getEdge(circularPrev(index))
        if (super.removeVertex(index)) {
            removeCacheAt(index)
            voidCacheAt(circularPrev(index), oldEdge)
            return true
        }
        return false
    }

    override fun moveVertex(index: Int, newX: Double, newY: Double): Boolean {
        val modifiedEdges = mutableListOf(circularPrev(index), index)
            .map { it to getEdge(it) }
        if (super.moveVertex(index, newX, newY)) {
            modifiedEdges.forEach { voidCacheAt(it.first, it.second) }
            return true
        }
        return false
    }

    override fun moveEdge(index: Int, newEdge: Euclidean2DSegment): Boolean {
        val modifiedEdges = mutableListOf(circularPrev(index), index, circularNext(index))
            .map { it to getEdge(it) }
        if (super.moveEdge(index, newEdge)) {
            modifiedEdges.forEach { voidCacheAt(it.first, it.second) }
            return true
        }
        return false
    }

    override fun mutateTo(p: MutableConvexPolygon) {
        super.mutateTo(p)
        canEdgeAdvance = MutableList(vertices.size) { true }
        growthDirections = MutableList(vertices.size) { null }
        normals = MutableList(vertices.size) { null }
    }

    /*
     * Edges only grow in their normal direction. Normally, an edge is advanced
     * by translating both of its vertices with the normal vector of the edge
     * (resized to have magnitude equal to the step parameter). However, vertices
     * may sometimes need to advance in different directions, for instance in order
     * to follow an oblique edge of an obstacle. In such cases we want to guarantee
     * that the advanced edge is always parallel to the old one (i.e. we want to
     * make sure that the edge advance in its normal direction). This may not be
     * trivial when the vertices of a given edge e have different directions of
     * growth. In order to preserve the parallelism with the old edge, we need to
     * resize the two directions of growth so that their component in the direction
     * normal to e is equal to step.
     */
    override fun advanceEdge(index: Int, step: Double): Boolean {
        if (step == 0.0) {
            return true
        }
        val e = getEdge(index)
        if (e.isDegenerate()) {
            return false
        }
        if (normals[index] == null) {
            normals[index] = e.computeNormal(index)
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
        val l1 = findLength(d1, n, step)
        val l2 = findLength(d2, n, step)
        require(!l1.isInfinite() && !l2.isInfinite()) { "invalid growth direction" }
        d1 = d1.resize(l1)
        d2 = d2.resize(l2)
        // super method is used in order to avoid voiding useful cache
        return super.moveEdge(index, Pair(e.first + d1, e.second + d2))
    }

    /*
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
    override fun extend(
        step: Double,
        obstacles: Collection<Shape>,
        origin: Euclidean2DPosition,
        width: Double,
        height: Double
    ): Boolean {
        var extended = false
        var i = 0
        while (i < vertices.size) {
            if (canEdgeAdvance[i]) {
                val hasAdvanced = advanceEdge(i, step)
                if (hasAdvanced && isInBoundaries(getEdge(i), origin, width, height)) {
                    val intersectedObs = obstacles.filter { intersects(it) }
                    // can be in the advanced case for at most 2 obstacle at a time
                    if (intersectedObs.size <= 2 && intersectedObs.all { isAdvancedCase(it, i, step) }) {
                        intersectedObs.forEach { adjustGrowth(it, i, step) }
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

    /*
     * The cache is set to some default value, the same it assumes when
     * it's voided.
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

    /*
     * Usually, cache related to an edge is voided (or better, set to default)
     * when the edge is somehow modified. However, different caches may apply
     * different policies for voiding (e.g. a cache memorizing the normal to
     * each edge is to be voided if the slope of the edge changes). This method
     * accepts the index of the modified edge and the old edge and applies
     * different policies to decide if each cache is to be voided.
     */
    private fun voidCacheAt(index: Int, old: Euclidean2DSegment) {
        val new = getEdge(index)
        canEdgeAdvance[index] = true
        if (!fuzzyEquals(old.slope(), new.slope()) && !(old.isDegenerate() || new.isDegenerate())) {
            growthDirections[index] = null
            normals[index] = null
        }
    }

    /*
     * Each vector has "two" normals: n and -n. This method is all about figuring
     * out which of the two is to be used to allow the polygon to extend.
     */
    private fun Euclidean2DSegment.computeNormal(index: Int): Euclidean2DPosition {
        val curr = toVector()
        val prev = getEdge(circularPrev(index)).toVector()
        val n = curr.normal().normalize()
        if (zCross(curr, n) > 0.0 != zCross(curr, prev) > 0.0) {
            return n.times(-1.0)
        }
        return n
    }

    /*
     * Given a vector a, we want to resize it so that its scalar projection
     * on a second vector b is equal to a certain quantity q. In order to do
     * so, we need to know the length of the new vector a'. This method computes
     * this quantity.
     */
    private fun findLength(a: Euclidean2DPosition, bUnit: Euclidean2DPosition, q: Double) = q / a.dot(bUnit)

    /*
     * Checks whether we are in advanced case. See [extend]. The index of the
     * growing edge and the step of growth should be provided as well.
     */
    private fun isAdvancedCase(obstacle: Shape, index: Int, step: Double) =
        obstacle.vertices().none { containsBoundaryIncluded(it) } &&
            vertices.filter { obstacle.contains(it.toPoint()) }.size == 1 &&
            !fuzzyEquals(findIntrudedEdge(obstacle, index, step).slope(), getEdge(index).slope())

    /*
     * During the advancement of an edge, multiple edges of an obstacle may be
     * intersected. This method allows to find the first intruded edge in the
     * advanced case (i.e., the one that the polygon first intruded during its
     * advancement). The index of the growing edge and the step of growth should
     * be provided as well.
     */
    private fun findIntrudedEdge(obstacle: Shape, index: Int, step: Double): Euclidean2DSegment {
        var intrudingVertex = getEdge(index).first
        var d = growthDirections[index]!!.first!!
        if (!obstacle.contains(intrudingVertex.toPoint())) {
            intrudingVertex = getEdge(index).second
            d = growthDirections[index]!!.second!!
        }
        // a segment going from the old position of the intruding vertex to the new one
        val movementSegment = Pair(intrudingVertex, intrudingVertex - d.resize(step))
        val intrudedEdges = findIntersectingEdges(obstacle, movementSegment)
        require(intrudedEdges.size == 1) { "vertex is not intruding" }
        return intrudedEdges.first()
    }

    /*
     * Finds the edges of the obstacle intersecting with the given edge of the polygon.
     */
    private fun findIntersectingEdges(obstacle: Shape, e: Euclidean2DSegment) =
        obstacle.vertices().run {
            mapIndexed { i, v -> Pair(v, this[(i + 1) % size]) }.filter { edgesIntersect(it, e) }
        }

    /*
     * Delegates the check to java.awt.geom.Line2D.
     */
    private fun edgesIntersect(e1: Euclidean2DSegment, e2: Euclidean2DSegment) =
        Line2D.Double(e1.first.toPoint(), e1.second.toPoint())
            .intersectsLine(e2.first.x, e2.first.y, e2.second.x, e2.second.y)

    /*
     * Adjusts the growth directions in the advanced case. See [extend].
     */
    private fun adjustGrowth(obstacle: Shape, indexOfAdvancingEdge: Int, step: Double) {
        val indexOfIntrudingV = vertices.indexOfFirst { obstacle.contains(it.toPoint()) }
        // intersecting edges
        val polygonEdge1 = getEdge(indexOfIntrudingV)
        val polygonEdge2 = getEdge(circularPrev(indexOfIntrudingV))
        val obstacleEdge = findIntrudedEdge(obstacle, indexOfAdvancingEdge, step)
        // intersecting points lying on polygon boundary
        val p1 = intersection(polygonEdge1, obstacleEdge).intersection.get()
        val p2 = intersection(polygonEdge2, obstacleEdge).intersection.get()
        // a new edge is going to be added, its vertices will grow following the intruded
        // obstacleEdge. In order to do so, their growth directions will be modified to be
        // parallel to such edge, but in opposite senses.
        val d1: Euclidean2DPosition
        val d2: Euclidean2DPosition
        if (p1.getDistanceTo(obstacleEdge.first) < p2.getDistanceTo(obstacleEdge.first)) {
            d1 = (obstacleEdge.first - p1).normalize()
            d2 = (obstacleEdge.second - p2).normalize()
        } else {
            d1 = (obstacleEdge.second - p1).normalize()
            d2 = (obstacleEdge.first - p2).normalize()
        }
        // since we intruded an obstacle we need to step back anyway
        advanceEdge(indexOfAdvancingEdge, -step)
        modifyGrowthDirection(indexOfIntrudingV, d1, true)
        addVertex(indexOfIntrudingV, vertices[indexOfIntrudingV].x, vertices[indexOfIntrudingV].y)
        canEdgeAdvance[indexOfIntrudingV] = false
        modifyGrowthDirection(circularPrev(indexOfIntrudingV), d2, false)
    }

    private fun modifyGrowthDirection(i: Int, newD: Euclidean2DPosition, first: Boolean) {
        val d = growthDirections[i]
        if (d == null) {
            growthDirections[i] = if (first) Pair(newD, null) else Pair(null, newD)
        } else {
            growthDirections[i] = if (first) d.copy(first = newD) else d.copy(second = newD)
        }
    }

    private fun Euclidean2DPosition.toPoint() = Point2D.Double(x, y)

    override fun equals(other: Any?) = super.equals(other)

    override fun hashCode() = super.hashCode()
}
