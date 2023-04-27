/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.navigation

import it.unibo.alchemist.model.cognitive.NavigationStrategy
import it.unibo.alchemist.model.cognitive.actions.NavigationAction2D
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.navigationgraph.Euclidean2DPassage
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.util.Sequences.cartesianProduct
import org.jgrapht.Graphs
import org.jgrapht.alg.shortestpath.BFSShortestPath

/**
 * A [NavigationStrategy] allowing to reach a known (static) destination (see [Pursue]).
 * The client can specify a list of known [destinations], the pedestrian will try to reach the
 * closest one for which a valid path leading there is known.
 * The difference between this behavior and [Pursue] is that the latter assumes no route
 * leading to the destination is known, whereas this behavior tries to exploit the pedestrian's
 * cognitive map to obtain a route to follow.
 *
 * @param T the concentration type.
 * @param L the type of landmarks of the pedestrian's cognitive map.
 * @param R the type of edges of the pedestrian's cognitive map, representing the [R]elations between landmarks.
 */
open class ReachKnownDestination<T, L : Euclidean2DConvexShape, R>(
    action: NavigationAction2D<T, L, R, ConvexPolygon, Euclidean2DPassage>,
    /**
     * Known destinations (must not be empty).
     */
    private val destinations: List<Euclidean2DPosition>,
    /*
     * An empty list is passed to super method, because route is initialised in this class' init block.
     */
) : FollowRoute<T, L, R>(action, emptyList()) {

    final override val route: List<Euclidean2DPosition>

    init {
        route = emptyList<Euclidean2DPosition>().takeIf { destinations.isEmpty() } ?: with(action) {
            val currPos = environment.getPosition(navigatingNode)
            val (closestDest, distanceToClosestDest) = destinations
                .asSequence()
                .map { it to it.distanceTo(currPos) }
                .minByOrNull { it.second }
                ?: throw IllegalArgumentException("internal error: destinations can't be empty at this point")
            destinations.asSequence()
                .sortedBy { it.distanceTo(currPos) }
                .map { it to findKnownPathTo(it) }
                .filter { (_, path) ->
                    /*
                     * A path leading to a destination is considered "valid" when:
                     * - it's not empty
                     * - the path's start is closer to currPos than the closest destination (this
                     * because otherwise it's more convenient to just pursue the latter).
                     */
                    path.isNotEmpty() && currPos.distanceTo(path.first().centroid) < distanceToClosestDest
                }
                .map { (destination, path) -> path.map { it.centroid } + destination }
                .firstOrNull() ?: listOf(closestDest)
        }
        setDestination(route[0])
    }

    /**
     * Finds a known path to the specified [destination]. The path is obtained from the [node]'s cognitive
     * map and consists of a list of landmarks, it is computed so as to minimize the distance between the first
     * landmark and the pedestrian's position and the distance between the last landmark and the destination. In
     * spite of this, the first landmark may be far from the pedestrian's position and the same applies to the
     * last landmark and the destination. This means the pedestrian may not have information regarding any path
     * leading from its current position to the first landmark and from the last one to the destination (such a
     * path may not exist at all).
     * If the cognitive map is poor the returned path may be poor. On average when the first landmark is farther
     * from the pedestrian's position than the destination itself it's just more convenient to pursue the latter.
     * If the cognitive map is empty the path will be empty as well.
     */
    private fun findKnownPathTo(destination: Euclidean2DPosition): List<L> = with(orientingCapability.cognitiveMap) {
        emptyList<L>().takeIf { vertexSet().isEmpty() } ?: let {
            val currPos = environment.getPosition(node)
            val currRoom = environment.graph.nodeContaining(currPos)
            val destRoom = environment.graph.nodeContaining(destination)
            if (currRoom == null || destRoom == null) {
                return emptyList()
            }
            val sequence = buildSequence(currRoom, destRoom) +
                vertexSet().asSequence().let { landmarks ->
                    landmarks.cartesianProduct(landmarks).sortedBy { (start, end) ->
                        start.centroid.distanceTo(currPos) + end.centroid.distanceTo(destination)
                    }
                }
            sequence
                .mapNotNull { (start, end) ->
                    /*
                     * At present the cognitive map is a MST, so there's a single path between each pair of nodes,
                     * in the future things may change and a policy deciding which path to pick may (need to) be
                     * introduced.
                     */
                    BFSShortestPath(this).getPath(start, end)?.vertexList
                }
                .firstOrNull()
                .orEmpty()
        }
    }

    /**
     * @returns a sequence of pairs of landmarks, for each pair the first element is contained in [startRoom] or
     * in an adjacent one, and the second element is contained in [endRoom] or in adjacent one. The sequence is
     * sorted, pairs appear in the following order:
     * - pairs where the first landmark is inside [startRoom] and the second landmark is inside [endRoom],
     * - pairs where the first landmark is inside [startRoom] and the second landmark is adjacent to [endRoom],
     * - pairs where the first landmark is adjacent to [startRoom] and the second landmark is inside [endRoom],
     * - pairs where the first landmark is adjacent to [startRoom] and the second landmark is adjacent to [endRoom].
     */
    private fun buildSequence(startRoom: ConvexPolygon, endRoom: ConvexPolygon): Sequence<Pair<L, L>> {
        val landmarksIn: (room: ConvexPolygon) -> Sequence<L> = { room ->
            orientingCapability.cognitiveMap.vertexSet().asSequence().filter { room.contains(it.centroid) }
        }
        val landmarksInAny: (rooms: List<ConvexPolygon>) -> Sequence<L> = { rooms ->
            rooms.asSequence().flatMap(landmarksIn)
        }
        val landmarksInStartRoom = landmarksIn(startRoom)
        val landmarksInEndRoom = landmarksIn(endRoom)
        val landmarksAdjacentToStartRoom = landmarksInAny(Graphs.successorListOf(environment.graph, startRoom))
        val landmarksAdjacentToEndRoom = landmarksInAny(Graphs.predecessorListOf(environment.graph, endRoom))
        return landmarksInStartRoom.cartesianProduct(landmarksInEndRoom) +
            landmarksInStartRoom.cartesianProduct(landmarksAdjacentToEndRoom) +
            landmarksAdjacentToStartRoom.cartesianProduct(landmarksInEndRoom) +
            landmarksAdjacentToStartRoom.cartesianProduct(landmarksAdjacentToEndRoom)
    }

    /**
     * A [waypoint] is considered reached when inside [currentRoom] or in an adjacent room.
     */
    override fun isReached(waypoint: Euclidean2DPosition, currentRoom: ConvexPolygon): Boolean = with(action) {
        super.isReached(waypoint, currentRoom) || doorsInSight().map { it.head }.any { it.contains(waypoint) }
    }
}
