/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.geometry.linesIntersection
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Action
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Segment2D
import it.unibo.alchemist.model.interfaces.graph.Euclidean2DPassage
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DefaultUndirectedWeightedGraph
import java.awt.Shape
import kotlin.math.abs
import kotlin.math.pow

/**
 * An action representing the orienting behavior of a pedestrian in an euclidean
 * bidimensional space.
 * This class accepts an [Euclidean2DEnvironmentWithGraph] whose graph features
 * [ConvexPolygon]al nodes (or any subclass of it) and [Euclidean2DPassage]s
 * as edges.
 *
 * @param T the concentration type.
 * @param N the type of landmarks of the pedestrian's cognitive map.
 * @param E the type of edges of the pedestrian's cognitive map.
 * @param M the type of nodes of the navigation graph provided by the environment.
 */
open class Orienting<T, N : Euclidean2DConvexShape, E, M : ConvexPolygon>(
    override val environment: Euclidean2DEnvironmentWithGraph<*, T, M, Euclidean2DPassage>,
    reaction: Reaction<T>,
    pedestrian: OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, N, E>,
    /**
     * When computing the [crossingPoint] of a door (or passage), i.e. the point which
     * is more convenient to cross, it may happen this is one of the endpoints of the
     * segment describing the passage. In such case, to avoid unnatural movements where
     * the agent is too close to walls, we move the crossing point away from the endpoint
     * of a quantity equal to this factor * the width of the passage.
     */
    val wallRepulsionFactor: Double = 0.3
) : AbstractOrienting<T, Euclidean2DPosition, Euclidean2DTransformation, N, E, M, Euclidean2DPassage>(
    environment,
    reaction,
    pedestrian
) {

    @Suppress("UNCHECKED_CAST")
    override fun cloneAction(n: Node<T>, r: Reaction<T>): Action<T> {
        require(n as? OrientingPedestrian<
            T, Euclidean2DPosition, Euclidean2DTransformation, M, Euclidean2DPassage> != null) {
            "node not compatible, required: " + pedestrian.javaClass + ", found: " + n.javaClass
        }
        n as OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, M, Euclidean2DPassage>
        return Orienting(environment, r, n)
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
        currentRoom.edges().forEach { side ->
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
    override fun crossingPoint(targetDoor: Euclidean2DPassage): Euclidean2DPosition {
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
                }.resized(toVector().magnitude * wallRepulsionFactor)
                return subdestination + correction
            }
            return subdestination
        }
    }

    override fun interpolatePositions(
        current: Euclidean2DPosition,
        target: Euclidean2DPosition,
        maxWalk: Double
    ): Euclidean2DPosition =
        Seek2D(environment, reaction, pedestrian, *target.coordinates).interpolatePositions(current, target, maxWalk)

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
            .let { pedestrian.shape.diameter.pow(2) * it / room.asAwtShape().area() + 1 }

    /*
     * A rough estimation of the area of a shape.
     */
    private fun Shape.area(): Double =
        with(bounds2D) {
            abs(width * height)
        }
}
