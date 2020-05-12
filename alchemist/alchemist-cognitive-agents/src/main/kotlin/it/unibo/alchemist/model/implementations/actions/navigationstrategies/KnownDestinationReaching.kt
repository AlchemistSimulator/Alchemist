/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions.navigationstrategies

import it.unibo.alchemist.model.implementations.actions.cartesianProduct
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.EuclideanNavigationAction
import it.unibo.alchemist.model.interfaces.NavigationStrategy
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.environments.EnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.graph.Euclidean2DPassage
import org.jgrapht.Graphs
import org.jgrapht.alg.shortestpath.BFSShortestPath

/**
 * A [NavigationStrategy] allowing to reach a known (static) destination (see [Pursuing]).
 * The client can specify a list of known [destinations], the pedestrian will try to reach the
 * closest one for which a valid path leading there is known.
 * The difference between this behavior and [Pursuing] is that the latter assumes no route
 * leading to the destination is known, whereas this behavior tries to exploit the pedestrian's
 * cognitive map to obtain a route to follow.
 */
open class KnownDestinationReaching<T, N : Euclidean2DConvexShape, E>(
    action: EuclideanNavigationAction<T, N, E, ConvexPolygon, Euclidean2DPassage>,
    /**
     * Known destinations.
     */
    private val destinations: List<Euclidean2DPosition>
) : RouteFollowing<T, N, E>(
    action,
    emptyList<Euclidean2DPosition>().takeIf { destinations.isEmpty() } ?: with(action) {
        val currPos = environment.getPosition(pedestrian)
        val (closestDest, distanceToClosestDest) = destinations
            .asSequence()
            .map { it to it.distanceTo(currPos) }
            .minBy { it.second }
            ?: throw IllegalArgumentException("internal error: destinations can't be empty at this point")
        destinations.asSequence()
            .sortedBy { it.distanceTo(currPos) }
            .map { it to findKnownPath(it, pedestrian, environment) }
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
) {

    /**
     * This behavior considers a [waypoint] reached when it's inside [currentRoom] or in an adjacent room.
     */
    override fun isReached(waypoint: Euclidean2DPosition, currentRoom: ConvexPolygon): Boolean = with(action) {
        super.isReached(waypoint, currentRoom) || doorsInSight().map { it.head }.any { it.contains(waypoint) }
    }
}

/**
 * Finds a known path to the specified [destination]. The path is obtained from the [pedestrian]'s cognitive
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
fun <T, P, N : ConvexGeometricShape<P, *>, M : ConvexGeometricShape<P, *>> findKnownPath(
    destination: P,
    pedestrian: OrientingPedestrian<T, P, *, N, *>,
    environment: EnvironmentWithGraph<*, T, P, *, M, *>
): List<N> where P : Position<P>, P : Vector<P> = pedestrian.cognitiveMap.let { cognitiveMap ->
    emptyList<N>().takeIf { cognitiveMap.vertexSet().isEmpty() } ?: run {
        val currPos = environment.getPosition(pedestrian)
        val currRoom = environment.graph.nodeContaining(currPos)
        val destRoom = environment.graph.nodeContaining(destination)
        if (currRoom == null || destRoom == null) {
            return emptyList()
        }
        val landmarksIn: (room: M) -> Sequence<N> = { room ->
            cognitiveMap.vertexSet().asSequence().filter { room.contains(it.centroid) }
        }
        val landmarksInAny: (rooms: List<M>) -> Sequence<N> = { rooms -> rooms.asSequence().flatMap(landmarksIn) }
        val landmarksInCurrRoom = landmarksIn(currRoom)
        val landmarksInDestRoom = landmarksIn(destRoom)
        val landmarksAdjacentToCurrRoom = landmarksInAny(Graphs.successorListOf(environment.graph, currRoom))
        val landmarksAdjacentToDestRoom = landmarksInAny(Graphs.predecessorListOf(environment.graph, destRoom))
        /*
         * In order, we try to find a path in which:
         * - the first landmark is in current room and the last one in destination room
         * - the first landmark is in current room and the last one in a room adjacent to destination room
         * - the first landmark is in a room adjacent to current room and the last one in destination room
         * - the first landmark is in a room adjacent to current room and the last one in a room adjacent to destination
         * room
         * - landmarks may be located anywhere (and may coincide), but we try to minimise the distance between the first
         * landmark and currPos and the distance between the last landmark and destination
         */
        return (cartesianProduct(landmarksInCurrRoom, landmarksInDestRoom) +
            cartesianProduct(landmarksInCurrRoom, landmarksAdjacentToDestRoom) +
            cartesianProduct(landmarksAdjacentToCurrRoom, landmarksInDestRoom) +
            cartesianProduct(landmarksAdjacentToCurrRoom, landmarksAdjacentToDestRoom) +
            cognitiveMap.vertexSet().asSequence().let { landmarks ->
                cartesianProduct(landmarks, landmarks).sortedBy { (start, end) ->
                    start.centroid.distanceTo(currPos) + end.centroid.distanceTo(destination)
                }
            })
            .mapNotNull { (start, end) ->
                /*
                 * At present the cognitive map is a MST, so there's a single path between each pair of nodes,
                 * in the future things may change and a policy deciding which path to pick may (need to) be
                 * introduced.
                 */
                BFSShortestPath(cognitiveMap).getPath(start, end)?.vertexList
            }
            .firstOrNull() ?: emptyList()
    }
}
