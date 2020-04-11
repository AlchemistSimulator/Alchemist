package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.implementations.actions.Seek
import it.unibo.alchemist.model.implementations.actions.Seek2D
import it.unibo.alchemist.model.implementations.geometry.intersection
import it.unibo.alchemist.model.implementations.geometry.SegmentsIntersectionType
import it.unibo.alchemist.model.implementations.geometry.linesIntersection
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.TimeDistribution
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Segment2D
import it.unibo.alchemist.model.interfaces.graph.Euclidean2DPassage
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DefaultUndirectedWeightedGraph
import java.awt.Shape
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow

/**
 * An [AbstractOrientingBehavior] in an euclidean bidimensional space.
 * This class accepts an [Euclidean2DEnvironmentWithGraph] whose graph features
 * [ConvexPolygon]al nodes (or any subclass of it) and [Euclidean2DPassage]s
 * as edges.
 *
 * @param T the concentration type.
 * @param N the type of landmarks of the pedestrian's cognitive map.
 * @param E the type of edges of the pedestrian's cognitive map.
 * @param M the type of nodes of the navigation graph provided by the environment.
 */
open class OrientingBehavior2D<T, N : Euclidean2DConvexShape, E, M : ConvexPolygon>(
    override val environment: Euclidean2DEnvironmentWithGraph<*, T, M, Euclidean2DPassage>,
    pedestrian: OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, N, E>,
    timeDistribution: TimeDistribution<T>
) : AbstractOrientingBehavior<T, Euclidean2DPosition, Euclidean2DTransformation, N, E, M, Euclidean2DPassage>(
    environment,
    pedestrian,
    timeDistribution
) {

    override fun moveTowards(target: Euclidean2DPosition, currentRoom: M?, targetDoor: Euclidean2DPassage) {
        if (currentRoom == null) {
            Seek2D(environment, this, pedestrian, *target.coordinates).execute()
        } else {
            val currPos = environment.getPosition(pedestrian)
            /*
             * Sophisticated seek is used to find the desired movement of the pedestrian.
             */
            val desiredMovement =
                Seek2D(environment, this, pedestrian, *target.coordinates).nextPosition
            var nextPos = currPos + desiredMovement
            /*
             * If such movement leads outside the current room and not through the desired
             * door, it is corrected.
             */
            if (!currentRoom.contains(nextPos) && !crosses(Segment2D(currPos, nextPos), targetDoor)) {
                nextPos = adjustMovement(currPos, nextPos, currentRoom)
            }
            /*
             * Normal seek is used to actually move, this because sophisticated seek may anyway
             * bring the pedestrian outside the current room crossing a door which is not the
             * target one and we don't want it.
             */
            Seek(environment, this, pedestrian, *nextPos.coordinates).execute()
        }
    }

    /*
     * We build a graph composed by the edges' midpoints, the room vertices and the provided
     * destination, then we compute the shortest paths between each midpoint and the final
     * destination and rank each edge consequently.
     */
    override fun computeEdgeRankings(currentRoom: M, destination: Euclidean2DPosition): Map<Euclidean2DPassage, Int> {
        val environmentGraph = environment.graph()
        val graph = DefaultUndirectedWeightedGraph<Euclidean2DPosition, DefaultEdge>(DefaultEdge::class.java)
        /*
         * Maps each edge's midpoint to the correspondent edge object
         */
        val edges = environmentGraph.outgoingEdgesOf(currentRoom).map { it.passageShape.midPoint to it }
        (currentRoom.vertices() + edges.map { it.first } + destination).forEach { graph.addVertex(it) }
        currentRoom.edges()
            .forEach { side ->
                /*
                 * The midpoints of the crossings lying on the side being considered
                 */
                val doorCenters = edges.map { it.first }
                    .filter { side.contains(it) }
                    .sortedBy { it.distanceTo(side.first) }
                    .toTypedArray()
                mutableListOf(side.first, *doorCenters, side.second)
                    .zipWithNext()
                    .forEach {
                        graph.addEdge(it.first, it.second)
                        graph.setEdgeWeight(it.first, it.second, it.first.distanceTo(it.second))
                    }
            }
        graph.vertexSet().forEach {
            if (it != destination && !currentRoom.intersectsBoundaryExcluded(Segment2D(it, destination))) {
                graph.addEdge(it, destination)
                graph.setEdgeWeight(it, destination, it.distanceTo(destination))
            }
        }
        val dijkstra = DijkstraShortestPath(graph)
        val sorted = edges
            .sortedBy { (midPoint, _) ->
                dijkstra.getPath(midPoint, destination)?.weight
            }
            .map { it.second }
        return environmentGraph.outgoingEdgesOf(currentRoom).map { it to sorted.indexOf(it) + 1 }.toMap()
    }

    /*
     * Each crossing (or door) is represented as an [Euclidean2DSegment] on the
     * boundary of the current room. This method finds the point of such segment
     * which is more convenient to cross.
     */
    override fun computeSubdestination(targetDoor: Euclidean2DPassage): Euclidean2DPosition {
        with(targetDoor.passageShape) {
            val nextRoom = environment.graph().getEdgeTarget(targetDoor)
            /*
             * The ideal movement the pedestrian would perform connects its current
             * position to the centroid of the next room.
             */
            val desiredMovement = Segment2D(environment.getPosition(pedestrian), nextRoom.centroid)
            /*
             * The sub-destination is computed as the point belonging to the door which
             * is closest to the intersection of the lines defined by the desired
             * movement and the crossing itself.
             */
            val subdestination = closestPointTo(linesIntersection(this, desiredMovement).point.get())
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
                }.resize(toVector().magnitude * wallRepulsionFactor)
                return subdestination + correction
            }
            return subdestination
        }
    }

    /**
     * The [congestionFactor] is added.
     */
    public override fun weight(edge: Euclidean2DPassage, rank: Int?) =
        super.weight(edge, rank) * congestionFactor(environment.graph().getEdgeTarget(edge))

    /*
     * This factor takes into account the congestion of the room the edge being weighted
     * leads to. It assumes the pedestrian can asses the level of congestion of such room
     * even if he's not located inside it.
     */
    private fun congestionFactor(room: M): Double =
        environment.nodes
            .filterIsInstance<Pedestrian<T>>()
            .filter { room.contains(environment.getPosition(it)) }
            .count()
            .let { (pedestrian.shape.diameter.pow(2) * it / room.asAwtShape().area()) + 1 }

    /**
     */
    @Suppress("UNCHECKED_CAST")
    override fun cloneOnNewNode(n: Node<T>?, currentTime: Time?): Reaction<T> {
        require(n as? OrientingPedestrian<
            T, Euclidean2DPosition, Euclidean2DTransformation, M, Euclidean2DPassage> != null) {
            "node not compatible, required: " + pedestrian.javaClass + ", found: " + n?.javaClass
        }
        n as OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, M, Euclidean2DPassage>
        return OrientingBehavior2D(environment, n, timeDistribution)
    }

    /*
     * If nextPos is outside of the current room, it is corrected to be contained in that
     * room. In particular, a new nextPos is found, whose distance from currPos is EQUAL
     * to the distance of the old nextPos from currPos. That is to say, the magnitude of the
     * pedestrian's movement isn't reduced.
     */
    private fun adjustMovement(
        currPos: Euclidean2DPosition,
        nextPos: Euclidean2DPosition,
        currRoom: M
    ): Euclidean2DPosition =
        currRoom.edges()
            .filter {
                intersection(Segment2D(currPos, nextPos), it).type == SegmentsIntersectionType.POINT
            }
            .map {
                intersection(it, currPos, nextPos.distanceTo(currPos))
            }
            .flatMap { mutableListOf(it.point1, it.point2) }
            .filter { it.isPresent }
            .map { it.get() }
            .filter { (it - currPos).angleBetween(nextPos - currPos) <= PI / 2 }
            .minBy { it.distanceTo(nextPos) } ?: nextPos

    /*
     * Checks whether the given segment intersects the given edge.
     */
    private fun crosses(segment: Segment2D<Euclidean2DPosition>, edge: Euclidean2DPassage) =
        intersection(segment, edge.passageShape).type == SegmentsIntersectionType.POINT

    /*
     * A rough estimation of the area of a shape.
     */
    private fun Shape.area(): Double =
        with(bounds2D) {
            abs(width * height)
        }

    companion object {
        private const val wallRepulsionFactor = 0.3
    }
}
