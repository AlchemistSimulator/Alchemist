/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.OrientingAction
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Segment2D
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.graph.Euclidean2DPassage
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DefaultUndirectedWeightedGraph
import kotlin.math.pow

/**
 * An [OrientingAction] allowing to pursue a known (static) [destination] without knowing any
 * path leading there, this is also known as path searching.
 * In this context, knowing a destination means knowing its position, which, in turn, means
 * knowing two things:
 * - the direction that connects the destination and the current position as the crow flies
 * - an estimation of the distance between the destination and the current position.
 * In order to reach the [destination] without a route to follow, the weighting system used
 * in [Exploring] is extended so as to take into account the (estimated) suitability of each
 * door to reach the provided [destination], see [suitabilityFactor].
 */
open class Pursuing<T, N : Euclidean2DConvexShape, E>(
    environment: Euclidean2DEnvironmentWithGraph<*, T, ConvexPolygon, Euclidean2DPassage>,
    reaction: Reaction<T>,
    pedestrian: OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, N, E>,
    /**
     * The destination to pursue.
     */
    protected open val destination: Euclidean2DPosition
) : Exploring<T, N, E>(environment, reaction, pedestrian) {

    constructor(
        environment: Euclidean2DEnvironmentWithGraph<*, T, ConvexPolygon, Euclidean2DPassage>,
        reaction: Reaction<T>,
        pedestrian: OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, N, E>,
        vararg destinationCoords: Number
    ) : this(environment, reaction, pedestrian, environment.makePosition(*destinationCoords))

    private lateinit var doorsRankings: Map<Euclidean2DPassage, Int>

    override fun explore() {
        if (currentRoomOrFail().customContains(destination)) {
            moveToFinal(destination)
        } else {
            doorsRankings = computeDoorsRankings()
            super.explore()
        }
    }

    /**
     * Assigns a weight to a passage (= door). This weighting system is derived from the one by
     * [Andresen et al.](https://doi.org/10.1080/23249935.2018.1432717). The [suitabilityFactor]
     * is added to the factors of the super method (= [Exploring.weight]).
     */
    override fun weight(passage: Euclidean2DPassage): Double =
        super.weight(passage) * suitabilityFactor(doorsRankings[passage].orFail())

    /**
     * This factor takes into account the rank given to the passage when assessing its suitability
     * to reach the [destination] (see [computeDoorsRankings]). It is computed as 1 - 0.5^[rank].
     * This is derived from [Andresen's work](https://doi.org/10.1080/23249935.2018.1432717).
     */
    protected open fun suitabilityFactor(rank: Int): Double = 1.0 - 0.5.pow(rank)

    /**
     * Assign an integer rank to each [doorsInSight], a lower rank means the door is preferable to
     * reach the [destination]. This method allows the pedestrian to perform an educated guess of
     * which door to take in order to get closer to the [destination]. It works as follows:
     * the spatial structure of the [cachedCurrentRoom] is assumed to be entirely perceived, this is
     * the only spatial information taken into account a part from the [destination] itself.
     * The shortest paths (represented as polygonal chains) connecting the doors of the current room
     * and the [destination] are computed, the lengths of these polygonal chains are calculated
     * under the constraint that the room must not be traversed by them but has to be bypassed (the
     * polygon chains must not overlap with [cachedCurrentRoom]).
     * To do so, a graph composed by the doors' midpoints, the room vertices and the [destination]
     * is built, then the shortest path between each door and the [destination] is computed and
     * each door is ranked consequently (the shorter the path, the lower the rank).
     */
    protected open fun computeDoorsRankings(): Map<Euclidean2DPassage, Int> {
        val currRoom = currentRoomOrFail()
        val graph = DefaultUndirectedWeightedGraph<Euclidean2DPosition, DefaultEdge>(DefaultEdge::class.java)
        /*
         * Maps each door's midpoint to the correspondent door object
         */
        val doors = doorsInSight().map { it.passageShapeOnTail.midPoint to it }
        (currRoom.vertices() + doors.map { it.first } + destination).forEach { graph.addVertex(it) }
        currRoom.edges().forEach { side ->
            /*
             * The midpoints of the doors lying on the side being considered
             */
            val doorCenters = doors.map { it.first }
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
            if (it != destination && !currRoom.intersects(Segment2D(it, destination))) {
                graph.addEdge(it, destination)
                graph.setEdgeWeight(it, destination, it.distanceTo(destination))
            }
        }
        val dijkstra = DijkstraShortestPath(graph)
        val sorted = doors
            .sortedBy { (midPoint, _) -> dijkstra.getPath(midPoint, destination)?.weight }
            .map { it.second }
        return doorsInSight().map { it to sorted.indexOf(it) + 1 }.toMap()
    }
}
