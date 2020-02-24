package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.implementations.geometry.*
import it.unibo.alchemist.model.implementations.geometry.graph.Euclidean2DCrossing
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.*
import it.unibo.alchemist.model.interfaces.geometry.graph.GraphEdge
import it.unibo.alchemist.model.interfaces.geometry.graph.NavigationGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.*
import it.unibo.alchemist.model.implementations.geometry.graph.GraphImpl
import it.unibo.alchemist.model.implementations.geometry.graph.builder.GraphBuilder
import it.unibo.alchemist.model.implementations.geometry.graph.builder.addEdge
import it.unibo.alchemist.model.implementations.geometry.graph.builder.addUndirectedEdge
import it.unibo.alchemist.model.implementations.geometry.graph.dijkstraShortestPath
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.graph.GraphEdgeWithData
import java.awt.Shape
import kotlin.math.abs
import kotlin.math.pow

/**
 * An [AbstractOrientingBehavior] in an euclidean bidimensional space.
 */
open class OrientingBehavior2D<T>(
    private val env: Environment<T, Euclidean2DPosition>,
    private val pedestrian: OrientingPedestrian<Euclidean2DPosition, Euclidean2DTransformation, ConvexEuclidean2DShape, out GraphEdge<ConvexEuclidean2DShape>, T>,
    timeDistribution: TimeDistribution<T>,
    private val envGraph: NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, ConvexPolygon, Euclidean2DCrossing>
) : AbstractOrientingBehavior<Euclidean2DPosition, Euclidean2DTransformation, ConvexPolygon, Euclidean2DCrossing, ConvexEuclidean2DShape, T>(env, pedestrian, timeDistribution, envGraph) {

    /*
     * We build a graph composed by the edges' midpoints, the room vertices and the provided
     * destination, then we compute the shortest paths between each midpoint and the final
     * destination and rank each edge consequently.
     *
     * Requires crossings to belong to sides of the polygon.
     */
    public override fun computeEdgeRankings(currRoom: ConvexPolygon, destination: Euclidean2DPosition): Map<GraphEdgeWithData<ConvexPolygon, Euclidean2DSegment>, Int> {
        val builder = GraphBuilder<Euclidean2DPosition, GraphEdge<Euclidean2DPosition>>()
        /*
         * Maps each edge's midpoint to the correspondent edge object
         */
        val edges = envGraph.edgesFrom(currRoom).map { it.data.midPoint() to it }
        /*
         * Fill adjacency list with connections
         */
        currRoom.vertices().indices
            .map { currRoom.getEdge(it) }
            .forEach { s ->
                mutableListOf(s.first, *edges
                    .map { it.first }
                    .filter { p -> s.contains(p) }
                    .sortedBy { it.getDistanceTo(s.first) }
                    .toTypedArray(),
                    s.second)
                    .zipWithNext()
                    .forEach { builder.addUndirectedEdge(it.first, it.second) }
            }
        builder.nodes().forEach {
            if (it != destination && !currRoom.intersectsBoundaryExcluded(Pair(it, destination))) {
                builder.addEdge(it, destination)
            }
        }
        val g = builder.build()
        /*
         * Compute rankings
         */
        val sorted = edges
            .sortedBy {
                g.dijkstraShortestPath(it.first, destination, {e -> e.from.getDistanceTo(e.to)})?.weight
            }
            .map { it.second }
        return envGraph.edgesFrom(currRoom).map { it to sorted.indexOf(it) + 1 }.toMap()
    }

    public override fun computeSubdestination(targetEdge: GraphEdgeWithData<ConvexPolygon, Euclidean2DSegment>): Euclidean2DPosition =
        with (targetEdge.data) {
            /*
             * The ideal movement the pedestrian would perform connects its current
             * position to the centroid of the next room.
             */
            val movement = Pair(targetEdge.to.centroid, env.getPosition(pedestrian))
            /*
             * The sub-destination is computed as the point belonging to the crossing
             * which is closest to the intersection between the lines defined by movement
             * and the crossing itself.
             */
            val subdestination = closestPointTo(intersectionLines(this, movement))
            /*
             * If the sub-destination is an end point of the crossing, we move it slightly
             * away from such end point to avoid behaviors in which the pedestrian always
             * moves remaining attached to the walls.
             */
            if (subdestination == first || subdestination == second) {
                val correction = if (subdestination == first) {
                    second - first
                } else {
                    first - second
                }.resize(toVector().magnitude() * 0.2)
                return subdestination + correction
            }
            return subdestination
        }

    /**
     * We add a factor taking into account the congestion of the rooms.
     */
    public override fun weight(e: Euclidean2DCrossing, rank: Int?): Double {
        /*
        if (currRoom != null) {
            val nPedestrian = env.nodes.filter { e.to.contains(env.getPosition(it)) }.count()
            val density = (pedestrian.shape.diameter.pow(2) * nPedestrian) / e.to.asAwtShape().area()
            return super.weight(e, rank) * ((density + 1.0) * 10)
        }
        return super.weight(e, rank)
         */
        val nPedestrian = env.nodes.filter { e.to.contains(env.getPosition(it)) }.count()
        val density = (pedestrian.shape.diameter.pow(2) * nPedestrian) / e.to.asAwtShape().area()
        return super.weight(e, rank) * ((density + 1.0) * 100)
    }

    /**
     */
    @Suppress("UNCHECKED_CAST")
    override fun cloneOnNewNode(n: Node<T>?, currentTime: Time?): Reaction<T> {
        try {
            n as OrientingPedestrian<Euclidean2DPosition, Euclidean2DTransformation, ConvexEuclidean2DShape, out GraphEdge<ConvexEuclidean2DShape>, T>
            return OrientingBehavior2D(env, n, timeDistribution, envGraph)
        } catch (e: ClassCastException) {
            throw IllegalArgumentException("node not compatible")
        }
    }

    /*
     * Checks if the provided segment intersects with the polygon, boundary excluded.
     */
    private fun ConvexPolygon.intersectsBoundaryExcluded(s: Euclidean2DSegment): Boolean =
         vertices().indices
            .map { intersection(getEdge(it), s) }
            .filter { it.type == SegmentsIntersectionTypes.POINT }
            .map { it.intersection.get() }
            .distinct()
            .size > 1

    /*
     * Computes the intersection of two lines (represented by segments) which are guaranteed to
     * intersect.
     */
    private fun intersectionLines(l1: Euclidean2DSegment, l2: Euclidean2DSegment): Euclidean2DPosition {
        require(!(l1.isDegenerate() || l2.isDegenerate())) { "degenerate lines" }
        val m1 = l1.slope()
        val m2 = l2.slope()
        val q1 = l1.first.y - m1 * l1.first.x
        val q2 = l2.first.y - m2 * l2.first.x
        if (m1.isInfinite()) {
            val x = l1.first.x
            val y = m2 * x + q2
            return Euclidean2DPosition(x, y)
        }
        if (m2.isInfinite()) {
            val x = l2.first.x
            val y = m1 * x + q1
            return Euclidean2DPosition(x, y)
        }
        val x = (q2 - q1) / (m1 - m2)
        val y = m1 * x + q1
        return Euclidean2DPosition(x, y)
    }

    /*
     * A rough esteem of the area of a shape.
     */
    private fun Shape.area(): Double =
        with(bounds2D) {
            abs(width * height)
        }
}
