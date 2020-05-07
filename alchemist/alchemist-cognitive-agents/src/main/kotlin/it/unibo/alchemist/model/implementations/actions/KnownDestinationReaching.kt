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
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.EnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.graph.Euclidean2DPassage
import org.jgrapht.alg.shortestpath.BFSShortestPath
import java.util.function.Supplier

/**
 * An [OrientingAction] allowing to reach a known (static) destination (see [Pursuing]).
 * The client can specify a list of known [destinations], the pedestrian will try to reach the
 * closest one for which a valid path leading there is known.
 * The difference between this behavior and [Pursuing] is that the latter assumes no route
 * leading to the destination is known, whereas this behavior tries to exploit the pedestrian's
 * cognitive map to obtain a route to follow.
 */
open class KnownDestinationReaching<T, N : Euclidean2DConvexShape, E>(
    environment: Euclidean2DEnvironmentWithGraph<*, T, ConvexPolygon, Euclidean2DPassage>,
    reaction: Reaction<T>,
    pedestrian: OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, N, E>,
    /**
     * Known destinations, one of these will be reached. This must not be empty.
     */
    private val destinations: List<Euclidean2DPosition>
) : RouteFollowing<T, N, E>(environment, reaction, pedestrian, Supplier {
    val currPos = environment.getPosition(pedestrian)
    val (closestDest, distanceToClosestDest) = destinations
        .asSequence()
        .map { it to it.distanceTo(currPos) }
        .minBy { it.second }
        ?: throw IllegalArgumentException("$destinations can't be empty")
    destinations.asSequence()
        .sortedBy { it.distanceTo(currPos) }
        .map { it to findKnownPathTo(it, pedestrian, environment) }
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
}) {

    constructor(
        environment: Euclidean2DEnvironmentWithGraph<*, T, ConvexPolygon, Euclidean2DPassage>,
        reaction: Reaction<T>,
        pedestrian: OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, N, E>,
        vararg destinations: Number
    ) : this(environment, reaction, pedestrian, destinations.toPositions())
}

/**
 * Finds a known path to the specified [destination].
 * The path is obtained from the [pedestrian]'s cognitive map and consists of a list
 * of landmarks:
 * - the path's end (= its last element) is the landmark closest to the destination
 * as the crow flies
 * - the path's start (= its first element) is the landmark connected to the path's
 * end which is closest to the pedestrian's current position as the crow flies.
 * Consecutive landmarks in the list are directly connected in the cognitive map,
 * but the pedestrian has no information regarding any path leading from its current
 * position to the first landmark, and from the last landmark to the [destination].
 * Note that the path can be empty if the cognitive map is empty as well, or can
 * feature a single element (namely, the landmark which is closest to the provided
 * destination) if the cognitive map features poor connections.
 * Note that, on average, if the path's start is farther from the current position
 * than the [destination] itself, it could be more convenient to just pursue the
 * latter.
 * The time complexity of this method in the worst case is O(|V| * (|V| + |E|)).
 * Its time complexity on average is O(|V| + |E|).
 */
fun <T, P, N : ConvexGeometricShape<P, *>> findKnownPathTo(
    destination: P,
    pedestrian: OrientingPedestrian<T, P, *, N, *>,
    environment: EnvironmentWithGraph<*, T, P, *, out ConvexGeometricShape<P, *>, *>
): List<N> where P : Position<P>, P : Vector<P> = pedestrian.cognitiveMap.let { cognitiveMap ->
    emptyList<N>().takeIf { cognitiveMap.vertexSet().isEmpty() } ?: run {
        val currPos = environment.getPosition(pedestrian)
        val closerLandmarks = cognitiveMap.vertexSet()
            .asSequence()
            .sortedByDistanceTo(currPos, environment)
        val destinationLandmark = cognitiveMap.vertexSet()
            .asSequence()
            .sortedByDistanceTo(destination, environment)
            .first()
        BFSShortestPath(cognitiveMap).let { shortestPathAlgorithm ->
            closerLandmarks.mapNotNull { sourceLandmark ->
                /*
                 * At present the cognitive map is a MST, so there's a single
                 * path between each pair of nodes. In the future, things may
                 * change and there could be more than one shortest path between
                 * two nodes. In this case, it may be preferable to choose a
                 * shortest path with the maximum number of nodes. The reason
                 * is that such path contains more detailed information
                 * regarding the route to follow.
                 */
                shortestPathAlgorithm.getPath(sourceLandmark, destinationLandmark)?.vertexList
            }.first()
        }
    }
}

/**
 * Given a sequence of landmarks, performs a triple [partition] into:
 * - landmarks located in the same [environment]'s area (= room) of the given [position],
 * - landmarks located in a room adjacent to the previous one,
 * - landmarks not located in any of the previous rooms.
 * These are then separately sorted by their distance as the crow flies to the given [position] and
 * combined in a new sequence (in which e.g. landmarks belonging to the first group will appear first).
 */
fun <P, N : ConvexGeometricShape<P, *>, M : ConvexGeometricShape<P, *>, F : Any?> Sequence<N>.sortedByDistanceTo(
    position: P,
    environment: EnvironmentWithGraph<*, *, P, *, M, F>
): Sequence<N> where P : Position<P>, P : Vector<P> {
    val customSort: Sequence<N>.() -> Sequence<N> = { sortedBy { position.distanceTo(it.centroid) } }
    return environment.graph.nodeContaining(position)?.let { positionRoom ->
        val adjacentRooms = environment.graph.neighborsReachableFrom(positionRoom)
        partition({
            positionRoom.contains(it.centroid)
        }, {
            adjacentRooms.any { room -> room.contains(it.centroid) }
        }).run {
            first.asSequence().customSort() + second.asSequence().customSort() + third.asSequence().customSort()
        }
    } ?: customSort()
}
